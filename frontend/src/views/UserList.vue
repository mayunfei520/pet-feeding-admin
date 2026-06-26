<template>
  <div>
    <h3>👥 客户管理</h3>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>用户名</th><th>手机号</th><th>邮箱</th><th>状态</th><th>注册时间</th><th>宠物数量</th><th>消费次数</th><th style="width:100px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.phone || '-' }}</td>
            <td>{{ u.email || '-' }}</td>
            <td>
              <span class="tag" :class="u.status === 'ACTIVE' ? 'active' : 'disabled'">
                {{ u.status === 'ACTIVE' ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ u.createdAt?.replace('T', ' ') }}</td>
            <td>{{ u.petCount ?? '-' }}</td>
            <td>{{ u.orderCount ?? '-' }}</td>
            <td>
              <button v-if="u.status === 'ACTIVE'" class="btn-sm danger" @click="toggleStatus(u)">禁用</button>
              <button v-else class="btn-sm primary" @click="toggleStatus(u)">启用</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/utils/api'

const users = ref([])

onMounted(() => fetchUsers())

async function fetchUsers() {
  try {
    const res = await userApi.listByRole('OWNER')
    users.value = res.data || []
  } catch (e) { /* */ }
}

async function toggleStatus(u) {
  const newStatus = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  if (!confirm(`确定${newStatus === 'DISABLED' ? '禁用' : '启用'}该客户吗？`)) return
  try {
    await userApi.updateStatus(u.id, newStatus)
    u.status = newStatus
  } catch (e) { /* */ }
}
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; font-size: 18px; }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.tag { padding: 2px 10px; border-radius: 3px; font-size: 13px; }
.tag.active { background: #d1fae5; color: #065f46; }
.tag.disabled { background: #fee2e2; color: #991b1b; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
.btn-sm.primary { color: #3b82f6; border-color: #93c5fd; }
</style>
