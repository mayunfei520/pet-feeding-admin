<template>
  <div>
    <h3>👤 喂养员管理</h3>

    <div class="section">
      <div class="section-title">⏳ 待审核</div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>用户ID</th><th>姓名</th><th>身份证</th><th>服务区域</th><th>经验</th><th>自我介绍</th><th style="width:120px">操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="f in pending" :key="f.id">
              <td>{{ f.id }}</td>
              <td>{{ f.userId }}</td>
              <td>{{ f.realName }}</td>
              <td>{{ f.idCard }}</td>
              <td>{{ f.serviceArea }}</td>
              <td class="memo">{{ f.experience || '-' }}</td>
              <td class="memo">{{ f.description || '-' }}</td>
              <td>
                <button class="btn-sm primary" @click="handleApprove(f.id)">通过</button>
                <button class="btn-sm danger" @click="handleReject(f.id)">拒绝</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="section">
      <div class="section-title">✅ 已通过</div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>姓名</th><th>服务区域</th><th>经验</th><th>自我介绍</th><th>评分</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="f in approved" :key="f.id">
              <td>{{ f.id }}</td>
              <td>{{ f.realName }}</td>
              <td>{{ f.serviceArea }}</td>
              <td class="memo">{{ f.experience || '-' }}</td>
              <td class="memo">{{ f.description || '-' }}</td>
              <td>⭐ {{ f.rating || '5.0' }}</td>
              <td>
                <button class="btn-sm" @click="$router.push(`/feeders/${f.id}/reviews`)">查看评价</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feederApi } from '@/utils/api'

const pending = ref([])
const approved = ref([])

onMounted(() => fetchData())

async function fetchData() {
  try {
    const [pr, ar] = await Promise.all([feederApi.pending(), feederApi.list()])
    pending.value = pr.data || []
    approved.value = (ar.data || []).filter(f => f.status === 'APPROVED')
  } catch (e) { /* */ }
}

async function handleApprove(id) {
  try { await feederApi.approve(id); fetchData() } catch (e) { /* */ }
}
async function handleReject(id) {
  if (!confirm('确定拒绝该申请吗？')) return
  try { await feederApi.reject(id); fetchData() } catch (e) { /* */ }
}
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; font-size: 18px; }
.section { margin-bottom: 24px; }
.section-title { font-size: 16px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.memo { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; margin-right: 4px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.primary { color: #3b82f6; border-color: #93c5fd; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
</style>
