<template>
  <PageTable
    title="客户管理"
    desc="管理平台上的宠物主人账户"
    :data="users"
    :columns="columns"
    :loading="loading"
  >
    <template #username="{ item }">
      <div class="user-cell">
        <div class="user-avatar-sm">{{ item.username.charAt(0).toUpperCase() }}</div>
        <span>{{ item.username }}</span>
      </div>
    </template>
    <template #status="{ item }">
      <span class="tag" :class="item.status === 'ACTIVE' ? 'tag-active' : 'tag-disabled'">
        {{ item.status === 'ACTIVE' ? '正常' : '禁用' }}
      </span>
    </template>
    <template #actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="toggleStatus(item)">
        {{ item.status === 'ACTIVE' ? '禁用' : '启用' }}
      </button>
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
    </template>
  </PageTable>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)

const columns = [
  { key: 'id', label: 'ID', style: 'width:60px' },
  { key: 'username', label: '用户名' },
  { key: 'phone', label: '手机号' },
  { key: 'email', label: '邮箱' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '注册时间', format: v => v ? v.replace('T', ' ') : '-' },
]

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await userApi.listByRole('OWNER')
    users.value = res.data || []
  } catch (e) { /* */ }
  finally { loading.value = false }
}

async function toggleStatus(u) {
  const action = u.status === 'ACTIVE' ? '禁用' : '启用'
  if (!confirm(`确定${action}客户「${u.username}」吗？`)) return
  try {
    await userApi.updateStatus(u.id, u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE')
    u.status = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.success(`${action}成功`)
  } catch (e) { /* */ }
}

async function handleDelete(u) {
  if (!confirm(`确定删除客户「${u.username}」吗？删除后不可恢复。`)) return
  try {
    await userApi.remove(u.id)
    users.value = users.value.filter(item => item.id !== u.id)
    ElMessage.success('删除成功')
  } catch (e) { /* */ }
}
</script>

<style scoped>
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 26px; height: 26px; border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 600; flex-shrink: 0;
}
</style>
