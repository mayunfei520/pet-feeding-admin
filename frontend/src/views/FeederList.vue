<template>
  <PageTable
    title="喂养员管理"
    desc="管理喂养员的入驻审核"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
  >
    <template #header-actions>
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
          已拒绝
          <span class="tab-badge">{{ rejected.length }}</span>
        </button>
      </div>
    </template>

    <template #realName="{ item }">
      <strong>{{ item.realName }}</strong>
    </template>

    <template #idCard="{ item }">
      <span class="mono">{{ item.idCard || '-' }}</span>
    </template>

    <template #experience="{ item }">
      <span class="truncate">{{ item.experience || '-' }}</span>
    </template>

    <template #description="{ item }">
      <span class="truncate">{{ item.description || '-' }}</span>
    </template>

    <template #rejectReason="{ item }">
      <span class="reason">{{ item.rejectReason || '-' }}</span>
    </template>

    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="openDetail(item)">详情</button>
      <template v-if="activeTab === 'pending'">
        <button class="btn btn-sm btn-primary" @click="handleApprove(item.id)" style="margin-left:4px">通过</button>
        <button class="btn btn-sm btn-danger-outline" @click="handleReject(item.id)" style="margin-left:4px">拒绝</button>
        <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
      </template>
      <template v-else-if="activeTab === 'approved'">
        <router-link :to="`/feeders/${item.id}/reviews`" class="btn btn-sm btn-outline" style="margin-left:4px">查看评价</router-link>
        <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
      </template>
      <template v-else>
        <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
      </template>
    </template>
  </PageTable>

  <el-drawer
    v-model="drawerVisible"
    :title="detailTitle"
    direction="rtl"
    size="420px"
    class="feeder-drawer"
  >
    <div class="fd-body" v-if="detail">
      <div class="fd-hero">
        <div class="fd-avatar">{{ (detail.realName || '?').slice(0, 1) }}</div>
        <div class="fd-hero-info">
          <div class="fd-name">{{ detail.realName }}</div>
          <span class="fd-badge" :class="'fd-' + statusKey(detail.status)">{{ statusLabel(detail.status) }}</span>
        </div>
      </div>

      <div class="fd-section">
        <div class="fd-row"><span class="fd-label">编号</span><span class="fd-value mono">{{ detail.id }}</span></div>
        <div class="fd-row"><span class="fd-label">身份证号</span><span class="fd-value mono">{{ detail.idCard || '-' }}</span></div>
        <div class="fd-row"><span class="fd-label">用户编号</span><span class="fd-value mono">{{ detail.userId || '-' }}</span></div>
        <div class="fd-row"><span class="fd-label">服务区域</span><span class="fd-value">{{ detail.serviceArea || '-' }}</span></div>
      </div>

      <div class="fd-section">
        <div class="fd-field"><span class="fd-label">经验</span><p class="fd-text">{{ detail.experience || '暂无' }}</p></div>
        <div class="fd-field"><span class="fd-label">自我介绍</span><p class="fd-text">{{ detail.description || '暂无' }}</p></div>
        <div class="fd-field">
          <span class="fd-label">资质证书</span>
          <p class="fd-text" v-if="detail.certification"><a :href="detail.certification" target="_blank" rel="noopener">{{ detail.certification }}</a></p>
          <p class="fd-text" v-else>无</p>
        </div>
      </div>

      <div class="fd-section fd-reject" v-if="detail.status === 'REJECTED'">
        <span class="fd-label">拒绝原因</span>
        <p class="fd-text">{{ detail.rejectReason || '（未填写）' }}</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feederApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmDanger } from '../utils/confirm'

const loading = ref(false)
const activeTab = ref('pending')
const pending = ref([])
const approved = ref([])
const rejected = ref([])

const pendingColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'realName', label: '姓名' },
  { key: 'idCard', label: '身份证号', style: 'width:172px' },
  { key: 'userId', label: '用户编号' },
  { key: 'serviceArea', label: '服务区域' },
  { key: 'experience', label: '经验' },
  { key: 'description', label: '自我介绍' },
]

const approvedColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'realName', label: '姓名' },
  { key: 'idCard', label: '身份证号', style: 'width:172px' },
  { key: 'serviceArea', label: '服务区域' },
  { key: 'experience', label: '经验' },
]

const rejectedColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'realName', label: '姓名' },
  { key: 'idCard', label: '身份证号', style: 'width:172px' },
  { key: 'serviceArea', label: '服务区域' },
  { key: 'rejectReason', label: '拒绝原因' },
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

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const [pr, ar, rr] = await Promise.all([
      feederApi.pending(),
      feederApi.list(),
      feederApi.rejected(),
    ])
    pending.value = pr.data || []
    approved.value = (ar.data || []).filter((f) => f.status === 'APPROVED')
    rejected.value = rr.data || []
  } catch (e) { /* */ }
  finally { loading.value = false }
}

/* 详情抽屉 */
const drawerVisible = ref(false)
const detail = ref(null)
const detailTitle = computed(() => (detail.value ? `喂养员 #${detail.value.id}` : '喂养员详情'))
function openDetail(item) {
  detail.value = item
  drawerVisible.value = true
}
function statusKey(s) {
  return s === 'APPROVED' ? 'ok' : s === 'REJECTED' ? 'no' : 'wait'
}
function statusLabel(s) {
  return s === 'APPROVED' ? '已通过' : s === 'REJECTED' ? '已拒绝' : '待审核'
}

async function handleApprove(id) {
  try {
    await feederApi.approve(id)
    ElMessage.success('审核通过')
    await fetchData()
  } catch (e) { /* */ }
}

async function handleReject(id) {
  let reason
  try {
    const res = await ElMessageBox.prompt('请填写拒绝原因（将记录并告知申请人）', '拒绝申请', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '例如：身份证信息不清晰 / 服务区域超出范围',
      inputValidator: (v) => (v && v.trim()) ? true : '拒绝原因不能为空',
      customClass: 'pf-confirm pf-confirm--danger',
    })
    reason = res.value.trim()
  } catch (e) {
    return // 用户取消
  }
  try {
    await feederApi.reject(id, reason)
    ElMessage.success('已拒绝并记录原因')
    await fetchData()
  } catch (e) { /* */ }
}

async function handleDelete(f) {
  if (!(await confirmDanger(`确定删除喂养员「${f.realName}」吗？删除后不可恢复。`))) return
  try {
    await feederApi.remove(f.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) { /* */ }
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

.truncate {
  display: inline-block;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  letter-spacing: 0.5px;
  color: var(--neutral-600);
}

.reason {
  color: var(--color-danger);
  font-size: 12px;
}
</style>
