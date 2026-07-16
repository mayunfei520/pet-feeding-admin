<template>
  <PageTable
    title="喂养员管理"
    desc="管理喂养员的审核与评价"
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
      </div>
    </template>

    <template #realName="{ item }">
      <strong>{{ item.realName }}</strong>
    </template>

    <template #experience="{ item }">
      <span class="truncate">{{ item.experience || '-' }}</span>
    </template>

    <template #description="{ item }">
      <span class="truncate">{{ item.description || '-' }}</span>
    </template>

    <template #rating="{ item }">
      <span class="stars">★ {{ item.rating || '5.0' }}</span>
    </template>

    <template #row-actions="{ item }">
      <template v-if="activeTab === 'pending'">
        <button class="btn btn-sm btn-primary" @click="handleApprove(item.id)">通过</button>
        <button class="btn btn-sm btn-danger-outline" @click="handleReject(item.id)" style="margin-left:4px">拒绝</button>
        <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
      </template>
      <template v-else>
        <router-link :to="`/feeders/${item.id}/reviews`" class="btn btn-sm btn-outline">查看评价</router-link>
        <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
      </template>
    </template>
  </PageTable>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { feederApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'
import { confirmDanger, confirmAction } from '../utils/confirm'

const loading = ref(false)
const activeTab = ref('pending')
const pending = ref([])
const approved = ref([])

const pendingColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'realName', label: '姓名' },
  { key: 'userId', label: '用户编号' },
  { key: 'serviceArea', label: '服务区域' },
  { key: 'experience', label: '经验' },
  { key: 'description', label: '自我介绍' },
]

const approvedColumns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'realName', label: '姓名' },
  { key: 'serviceArea', label: '服务区域' },
  { key: 'experience', label: '经验' },
  { key: 'rating', label: '评分' },
]

const tableData = computed(() => activeTab.value === 'pending' ? pending.value : approved.value)
const tableColumns = computed(() => activeTab.value === 'pending' ? pendingColumns : approvedColumns)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const [pr, ar] = await Promise.all([feederApi.pending(), feederApi.list()])
    pending.value = pr.data || []
    approved.value = (ar.data || []).filter(f => f.status === 'APPROVED')
  } catch (e) { /* */ }
  finally { loading.value = false }
}

async function handleApprove(id) {
  try {
    await feederApi.approve(id)
    ElMessage.success('审核通过')
    await fetchData()
  } catch (e) { /* */ }
}

async function handleReject(id) {
  if (!(await confirmAction('确定拒绝该申请吗？', '审核'))) return
  try {
    await feederApi.reject(id)
    ElMessage.success('已拒绝')
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
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.tab {
  padding: 7px 16px;
  border: none;
  background: #fff;
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
  border-left: 1px solid var(--neutral-200);
}
.tab:hover {
  background: var(--neutral-50);
  color: var(--neutral-700);
}
.tab.active {
  background: var(--brand-primary);
  color: #fff;
}
.tab.active .tab-badge {
  background: rgba(255,255,255,0.25);
  color: #fff;
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
</style>
