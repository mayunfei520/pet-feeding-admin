<template>
  <PageTable
    title="宠物审核"
    desc="审核客户发布的宠物，通过后方可被下单喂养"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :empty-text="emptyText"
  >
    <template #header-actions>
      <button class="btn btn-sm btn-outline refresh-btn" @click="fetchData" :disabled="loading">
        <span class="refresh-icon" :class="{ spinning: loading }">↻</span> 刷新
      </button>
      <div class="tabs">
        <button
          class="tab"
          :class="{ active: activeTab === 'pending' }"
          @click="activeTab = 'pending'"
        >
          待审核
          <span class="tab-badge">{{ pending.length }}</span>
        </button>
        <button
          class="tab"
          :class="{ active: activeTab === 'approved' }"
          @click="activeTab = 'approved'"
        >
          已通过
          <span class="tab-badge">{{ approved.length }}</span>
        </button>
        <button
          class="tab"
          :class="{ active: activeTab === 'rejected' }"
          @click="activeTab = 'rejected'"
        >
          已驳回
          <span class="tab-badge">{{ rejected.length }}</span>
        </button>
      </div>
    </template>

    <template #image="{ item }">
      <img v-if="thumb(item)" :src="thumb(item)" class="pet-thumb" alt="pet" />
      <span v-else class="muted">无图</span>
    </template>

    <template #name="{ item }">
      <strong>{{ item.name }}</strong>
    </template>

    <template #species="{ item }">
      <span class="tag">{{ speciesLabel(item.species) }}</span>
    </template>

    <template #ownerName="{ item }">
      <span>{{ ownerName(item.userId) || '-' }}</span>
    </template>

    <template #createdAt="{ item }">
      <span class="mono">{{ fmt(item.createdAt) }}</span>
    </template>

    <template #status="{ item }">
      <span class="pet-badge" :class="'pet-' + statusKey(item.status)">{{ statusLabel(item.status) }}</span>
    </template>

    <template #rejectReason="{ item }">
      <span class="reason">{{ item.rejectReason || '-' }}</span>
    </template>

    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="openDetail(item)">详情</button>
      <template v-if="activeTab === 'pending'">
        <button class="btn btn-sm btn-primary" @click="handleApprove(item.id)" style="margin-left:4px">通过</button>
        <button class="btn btn-sm btn-danger-outline" @click="handleReject(item.id)" style="margin-left:4px">驳回</button>
      </template>
    </template>
  </PageTable>

  <el-drawer
    v-model="drawerVisible"
    :title="detailTitle"
    direction="rtl"
    size="440px"
    class="pet-drawer"
  >
    <div class="pd-body" v-if="detail">
      <div class="pd-hero">
        <img v-if="thumb(detail)" :src="thumb(detail)" class="pd-avatar" alt="pet" />
        <div v-else class="pd-avatar pd-avatar--empty">🐾</div>
        <div class="pd-hero-info">
          <div class="pd-name">{{ detail.name }}</div>
          <span class="pet-badge" :class="'pet-' + statusKey(detail.status)">{{ statusLabel(detail.status) }}</span>
        </div>
      </div>

      <div class="pd-section">
        <div class="pd-row"><span class="pd-label">编号</span><span class="pd-value mono">{{ detail.id }}</span></div>
        <div class="pd-row"><span class="pd-label">种类</span><span class="pd-value">{{ speciesLabel(detail.species) }}</span></div>
        <div class="pd-row"><span class="pd-label">品种</span><span class="pd-value">{{ detail.breed || '-' }}</span></div>
        <div class="pd-row"><span class="pd-label">年龄</span><span class="pd-value">{{ detail.age != null ? detail.age + ' 岁' : '-' }}</span></div>
        <div class="pd-row"><span class="pd-label">体重</span><span class="pd-value">{{ detail.weight != null ? detail.weight + ' kg' : '-' }}</span></div>
        <div class="pd-row"><span class="pd-label">已打疫苗</span><span class="pd-value">{{ detail.vaccinated ? '是' : '否' }}</span></div>
        <div class="pd-row"><span class="pd-label">主人</span><span class="pd-value">{{ ownerName(detail.userId) || '-' }}</span></div>
        <div class="pd-row"><span class="pd-label">提交时间</span><span class="pd-value mono">{{ fmt(detail.createdAt) }}</span></div>
      </div>

      <div class="pd-section">
        <div class="pd-field"><span class="pd-label">医疗备注</span><p class="pd-text">{{ detail.medicalNotes || '暂无' }}</p></div>
      </div>

      <div class="pd-section" v-if="gallery(detail.image).length">
        <span class="pd-label">宠物照片</span>
        <div class="pd-gallery">
          <img v-for="(u, i) in gallery(detail.image)" :key="i" :src="u" class="pd-photo" alt="pet" />
        </div>
      </div>

      <div class="pd-section pd-reject" v-if="detail.status === 'REJECTED'">
        <span class="pd-label">驳回原因</span>
        <p class="pd-text">{{ detail.rejectReason || '（未填写）' }}</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { adminPetApi, userApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const activeTab = ref('pending')
const pending = ref([])
const approved = ref([])
const rejected = ref([])
const usersMap = ref({})

const pendingColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'image', label: '照片', style: 'width:64px' },
  { key: 'name', label: '宠物名' },
  { key: 'species', label: '种类' },
  { key: 'breed', label: '品种' },
  { key: 'ownerName', label: '主人' },
  { key: 'createdAt', label: '提交时间' },
  { key: 'status', label: '状态' },
]
const approvedColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'image', label: '照片', style: 'width:64px' },
  { key: 'name', label: '宠物名' },
  { key: 'species', label: '种类' },
  { key: 'breed', label: '品种' },
  { key: 'ownerName', label: '主人' },
  { key: 'createdAt', label: '提交时间' },
]
const rejectedColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'image', label: '照片', style: 'width:64px' },
  { key: 'name', label: '宠物名' },
  { key: 'species', label: '种类' },
  { key: 'breed', label: '品种' },
  { key: 'ownerName', label: '主人' },
  { key: 'createdAt', label: '提交时间' },
  { key: 'rejectReason', label: '驳回原因' },
]

const tableData = computed(() => {
  if (activeTab.value === 'pending') return pending.value
  if (activeTab.value === 'rejected') return rejected.value
  return approved.value
})
const tableColumns = computed(() => {
  if (activeTab.value === 'pending') return pendingColumns
  if (activeTab.value === 'rejected') return rejectedColumns
  return approvedColumns
})
const emptyText = computed(() => {
  if (activeTab.value === 'pending') return '暂无待审核宠物'
  if (activeTab.value === 'rejected') return '暂无已驳回宠物'
  return '暂无已通过宠物'
})

onMounted(() => fetchData())
watch(activeTab, () => fetchData())

async function reload() {
  const [pr, ar, rr, ul] = await Promise.all([
    adminPetApi.reviewQueue('PENDING'),
    adminPetApi.reviewQueue('APPROVED'),
    adminPetApi.reviewQueue('REJECTED'),
    userApi.list(),
  ])
  pending.value = pr.data || []
  approved.value = ar.data || []
  rejected.value = rr.data || []
  const map = {}
  ;(ul.data || []).forEach((u) => { map[u.id] = u })
  usersMap.value = map
}

async function fetchData() {
  loading.value = true
  try {
    await reload()
  } catch (e) { /* */ }
  finally { loading.value = false }
}

// 待审核队列轻轮询（每 20s 静默刷新）
let pollTimer = null
onMounted(() => {
  pollTimer = setInterval(() => {
    if (activeTab.value === 'pending' && !loading.value) reload()
  }, 20000)
})
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

/* 详情抽屉 */
const drawerVisible = ref(false)
const detail = ref(null)
const detailTitle = computed(() => (detail.value ? `宠物 #${detail.value.id}` : '宠物详情'))
function openDetail(item) {
  detail.value = item
  drawerVisible.value = true
}

function statusKey(s) {
  return s === 'APPROVED' ? 'ok' : s === 'REJECTED' ? 'no' : 'wait'
}
function statusLabel(s) {
  return s === 'APPROVED' ? '已通过' : s === 'REJECTED' ? '已驳回' : '待审核'
}
function speciesLabel(s) {
  return s === 'CAT' ? '猫' : s === 'DOG' ? '狗' : (s || '-')
}
function ownerName(userId) {
  const u = usersMap.value[userId]
  if (!u) return ''
  return u.realName || u.username || String(userId)
}
function fmt(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 19)
}
function gallery(img) {
  if (!img) return []
  return String(img).split(',').map((s) => s.trim()).filter(Boolean)
}
function thumb(item) {
  const g = gallery(item.image)
  return g.length ? g[0] : null
}

async function handleApprove(id) {
  loading.value = true
  try {
    await adminPetApi.approve(id)
    ElMessage.success('审核通过，宠物已可下单')
    await reload()
  } catch (e) { /* */ }
  finally { loading.value = false }
}

async function handleReject(id) {
  let reason
  try {
    const res = await ElMessageBox.prompt('请填写驳回原因（将记录并告知客户）', '驳回宠物', {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：品种信息不清 / 照片不符合要求',
      inputValidator: (v) => (v && v.trim()) ? true : '驳回原因不能为空',
      customClass: 'pf-confirm pf-confirm--danger',
    })
    reason = res.value.trim()
  } catch (e) {
    return // 用户取消
  }
  loading.value = true
  try {
    await adminPetApi.reject(id, reason)
    ElMessage.success('已驳回并记录原因')
    await reload()
  } catch (e) { /* */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 0;
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.tab {
  padding: 7px 16px;
  border: 1px solid transparent;
  background: rgba(12, 20, 36, 0.6);
  font-size: 13px;
  color: var(--neutral-500);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  position: relative;
}
.tab + .tab {
  border-left: 1px solid var(--border-soft);
}
.tab:hover {
  background: rgba(125, 211, 252, 0.08);
  color: var(--neutral-700);
}
.tab.active {
  background: var(--brand-gradient);
  color: #061018;
  font-weight: 600;
  border-color: transparent;
}
.tab.active .tab-badge {
  background: rgba(255, 255, 255, 0.28);
  color: #061018;
}
.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--neutral-100);
  color: var(--neutral-400);
  font-size: 11px;
  font-weight: 600;
  transition: all var(--transition-fast);
}

.mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  letter-spacing: 0.5px;
  color: var(--neutral-600);
}
.muted {
  color: var(--neutral-400);
  font-size: 12px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(125, 211, 252, 0.14);
  color: var(--ice-bright);
  font-size: 12px;
  font-weight: 600;
}

.pet-thumb {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--border-soft);
}

.pet-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.pet-ok { background: rgba(52, 211, 153, 0.16); color: #34d399; }
.pet-no { background: var(--color-danger-bg); color: var(--color-danger); }
.pet-wait { background: rgba(251, 191, 36, 0.16); color: #fbbf24; }

.reason {
  color: var(--color-danger);
  font-size: 12px;
}

/* 刷新按钮 */
.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.refresh-icon {
  display: inline-block;
  font-size: 15px;
  line-height: 1;
  transition: transform 0.3s ease;
}
.refresh-icon.spinning {
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 详情抽屉 */
.pd-body { color: var(--neutral-700); }
.pd-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-soft);
  margin-bottom: 16px;
}
.pd-avatar {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  object-fit: cover;
  border: 1px solid var(--border-soft);
}
.pd-avatar--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  background: rgba(125, 211, 252, 0.1);
}
.pd-hero-info { display: flex; flex-direction: column; gap: 6px; }
.pd-name { font-family: var(--font-display); font-size: 18px; font-weight: 700; color: var(--neutral-900); }

.pd-section { margin-bottom: 18px; }
.pd-row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  border-bottom: 1px dashed var(--border-soft);
  font-size: 13px;
}
.pd-label { color: var(--neutral-400); }
.pd-value { color: var(--neutral-700); font-weight: 500; }
.pd-field { margin-top: 8px; }
.pd-field .pd-label { display: block; margin-bottom: 4px; }
.pd-text { margin: 0; font-size: 13px; line-height: 1.6; color: var(--neutral-700); }

.pd-gallery { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.pd-photo {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid var(--border-soft);
}

.pd-reject {
  background: var(--color-danger-bg);
  border-radius: var(--radius-sm);
  padding: 12px 14px;
}
.pd-reject .pd-label { color: var(--color-danger); }
.pd-reject .pd-text { color: var(--color-danger); }
</style>
