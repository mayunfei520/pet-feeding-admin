<template>
  <PageTable
    title="管理员管理"
    desc="管理平台管理员账户"
    :data="admins"
    :columns="columns"
    :loading="loading"
  >
    <template #header-actions>
      <button class="btn btn-primary" @click="showAdd = true">+ 添加管理员</button>
    </template>
    <template #status="{ item }">
      <span class="tag" :class="item.status === 'ACTIVE' ? 'tag-active' : 'tag-disabled'">
        {{ item.status === 'ACTIVE' ? '正常' : '禁用' }}
      </span>
    </template>
    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="toggleStatus(item)">
        {{ item.status === 'ACTIVE' ? '禁用' : '启用' }}
      </button>
      <button class="btn btn-sm btn-outline" @click="handleReset(item)" style="margin-left:4px">重置密码</button>
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
    </template>
  </PageTable>

  <!-- Add Modal -->
  <div class="modal-overlay" v-if="showAdd" @click.self="showAdd = false">
    <div class="modal animate-fade-in-up">
      <div class="modal-header">
        <span class="modal-title">添加管理员</span>
        <button class="modal-close" @click="showAdd = false">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>用户名 <span class="req">*</span></label>
          <input v-model="form.username" type="text" placeholder="至少3个字符" maxlength="20" class="input" />
        </div>
        <div class="form-group">
          <label>密码 <span class="req">*</span></label>
          <input v-model="form.password" type="password" placeholder="至少6个字符" maxlength="20" class="input" />
        </div>
        <div class="error" v-if="error">{{ error }}</div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showAdd = false">取消</button>
        <button class="btn btn-primary" @click="handleAdd" :disabled="adding">
          {{ adding ? '添加中...' : '确认添加' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'
import { confirmDanger, confirmAction } from '../utils/confirm'

const admins = ref([])
const loading = ref(false)
const showAdd = ref(false)
const adding = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '' })

const columns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'username', label: '用户名' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '注册时间', format: v => v ? v.replace('T', ' ') : '-' },
]

onMounted(() => fetchAdmins())

async function fetchAdmins() {
  loading.value = true
  try {
    const res = await userApi.listByRole('ADMIN')
    admins.value = res.data || []
  } catch (e) { /* */ }
  finally { loading.value = false }
}

async function handleAdd() {
  error.value = ''
  if (!form.username || !form.password) { error.value = '用户名和密码为必填项'; return }
  if (form.username.length < 3) { error.value = '用户名长度至少3个字符'; return }
  if (form.password.length < 6) { error.value = '密码长度至少6个字符'; return }
  adding.value = true
  try {
    const uname = form.username
    const pwd = form.password
    await userApi.register({ ...form, role: 'ADMIN' })
    showAdd.value = false
    Object.assign(form, { username: '', password: '' })
    await fetchAdmins()
    ElMessage.success(`管理员创建成功\n账号：${uname}  密码：${pwd}`)
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || '添加失败'
  } finally { adding.value = false }
}

async function handleDelete(u) {
  if (!(await confirmDanger(`确定删除管理员「${u.username}」吗？删除后不可恢复。`))) return
  try {
    await userApi.remove(u.id)
    admins.value = admins.value.filter(a => a.id !== u.id)
    ElMessage.success('删除成功')
  } catch (e) { /* */ }
}

async function handleReset(u) {
  if (!(await confirmAction(`确定重置管理员「${u.username}」的密码吗？`, '重置密码'))) return
  try {
    const res = await userApi.resetPassword(u.id)
    ElMessage.success(`密码已重置\n账号：${u.username}\n新密码：${res.data}`)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '重置失败')
  }
}

async function toggleStatus(u) {
  const newStatus = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'
  if (!(await confirmAction(`确定${action}该管理员吗？`, `${action}管理员`))) return
  try {
    await userApi.updateStatus(u.id, newStatus)
    u.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (e) { /* */ }
}
</script>

<style scoped>
.req { color: var(--color-danger); }
.error { color: var(--color-danger); font-size: 13px; margin-top: 12px; padding: 8px 12px; background: var(--color-danger-bg); border-radius: 6px; border: 1px solid rgba(239,68,68,0.15); }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.55);
  display: flex; align-items: center; justify-content: center; z-index: 200;
  backdrop-filter: blur(4px);
}
.modal {
  width: 420px;
  background: linear-gradient(160deg, #0f1a2e 0%, #0d1526 100%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl), 0 0 0 1px rgba(125, 211, 252, 0.06);
  overflow: hidden;
}
.modal-header {
  padding: 18px 24px; border-bottom: 1px solid var(--border-soft);
  display: flex; align-items: center; justify-content: space-between;
}
.modal-title { font-size: 16px; font-weight: 600; color: var(--neutral-900); }
.modal-close { border: none; background: none; font-size: 18px; color: var(--neutral-400); cursor: pointer; padding: 4px; }
.modal-close:hover { color: var(--ice); }
.modal-body { padding: 20px 24px; }
.modal-footer {
  padding: 14px 24px; border-top: 1px solid var(--border-soft);
  display: flex; justify-content: flex-end; gap: 8px;
}
.form-group { margin-bottom: 14px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 13px; color: var(--neutral-600); font-weight: 500; }
.form-group .input { width: 100%; height: 38px; padding: 0 12px; }
</style>
