<template>
  <PageTable
    title="喂养员管理"
    desc="管理喂养员的审核与评价"
    :data="[]"
    :columns="[]"
    :loading="loading"
    :show-actions="false"
  >
    <template #actions>
      <div class="tabs">
        <button class="tab" :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">
          ⏳ 待审核 ({{ pending.length }})
        </button>
        <button class="tab" :class="{ active: activeTab === 'approved' }" @click="activeTab = 'approved'">
          ✅ 已通过 ({{ approved.length }})
        </button>
      </div>
    </template>

    <template #table-content>
      <!-- Pending Table -->
      <div v-if="activeTab === 'pending' && pending.length > 0" class="sub-table">
        <table class="table">
          <thead>
            <tr>
              <th style="width:50px">ID</th>
              <th>姓名</th>
              <th>用户ID</th>
              <th>服务区域</th>
              <th>经验</th>
              <th>自我介绍</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in pending" :key="f.id">
              <td>{{ f.id }}</td>
              <td><strong>{{ f.realName }}</strong></td>
              <td>{{ f.userId }}</td>
              <td>{{ f.serviceArea }}</td>
              <td class="truncate">{{ f.experience || '-' }}</td>
              <td class="truncate">{{ f.description || '-' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" @click="handleApprove(f.id)">通过</button>
                <button class="btn btn-sm btn-danger-outline" @click="handleReject(f.id)" style="margin-left:4px">拒绝</button>
                <button class="btn btn-sm btn-danger-outline" @click="handleDelete(f)" style="margin-left:4px">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="activeTab === 'pending'" class="empty-state">暂无待审核的喂养员</div>

      <!-- Approved Table -->
      <div v-else-if="activeTab === 'approved' && approved.length > 0" class="sub-table">
        <table class="table">
          <thead>
            <tr>
              <th style="width:50px">ID</th>
              <th>姓名</th>
              <th>服务区域</th>
              <th>经验</th>
              <th>评分</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in approved" :key="f.id">
              <td>{{ f.id }}</td>
              <td><strong>{{ f.realName }}</strong></td>
              <td>{{ f.serviceArea }}</td>
              <td class="truncate">{{ f.experience || '-' }}</td>
              <td><span class="stars">⭐ {{ f.rating || '5.0' }}</span></td>
              <td>
                <router-link :to="`/feeders/${f.id}/reviews`" class="btn btn-sm btn-outline">查看评价</router-link>
                <button class="btn btn-sm btn-danger-outline" @click="handleDelete(f)" style="margin-left:4px">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else-if="activeTab === 'approved'" class="empty-state">暂无已通过的喂养员</div>
    </template>
  </PageTable>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feederApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const activeTab = ref('pending')
const pending = ref([])
const approved = ref([])

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
  if (!confirm('确定拒绝该申请吗？')) return
  try {
    await feederApi.reject(id)
    ElMessage.success('已拒绝')
    await fetchData()
  } catch (e) { /* */ }
}

async function handleDelete(f) {
  if (!confirm(`确定删除喂养员「${f.realName}」吗？删除后不可恢复。`)) return
  try {
    await feederApi.remove(f.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (e) { /* */ }
}
</script>

<style scoped>
.tabs { display: flex; gap: 4px; }
.tab {
  padding: 7px 16px; border: 1px solid var(--neutral-200); border-radius: 8px;
  background: #fff; font-size: 13px; color: var(--neutral-600); cursor: pointer;
  transition: all var(--transition-fast);
}
.tab:hover { background: var(--neutral-50); }
.tab.active { background: var(--brand-primary); color: #fff; border-color: var(--brand-primary); }

.sub-table { margin-top: 4px; }
.truncate { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-state { text-align: center; padding: 40px; color: var(--neutral-400); font-size: 14px; }
</style>
