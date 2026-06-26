<template>
  <div>
    <div class="page-header">
      <h3>🛡️ 管理员管理</h3>
      <button class="btn-add" @click="showAdd = true">+ 添加管理员</button>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>用户名</th><th>手机号</th><th>邮箱</th><th>状态</th><th>注册时间</th><th style="width:160px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in admins" :key="u.id">
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
            <td>
              <button v-if="u.status === 'ACTIVE'" class="btn-sm danger" @click="toggleStatus(u)">禁用</button>
              <button v-else class="btn-sm primary" @click="toggleStatus(u)">启用</button>
              <button class="btn-sm danger delete" @click="handleDelete(u)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加管理员弹窗 -->
    <div class="modal-overlay" v-if="showAdd" @click.self="showAdd = false">
      <div class="modal">
        <div class="modal-header">
          <span>添加管理员</span>
          <button class="modal-close" @click="showAdd = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-item">
            <label>用户名 <span class="required">*</span></label>
            <input v-model="form.username" type="text" placeholder="3-20个字符" maxlength="20" />
          </div>
          <div class="form-item">
            <label>密码 <span class="required">*</span></label>
            <input v-model="form.password" type="password" placeholder="6-20个字符" maxlength="20" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="form.phone" type="text" placeholder="选填" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="form.email" type="text" placeholder="选填" />
          </div>
          <div class="error" v-if="error">{{ error }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showAdd = false">取消</button>
          <button class="btn-confirm" @click="handleAdd" :disabled="adding">
            {{ adding ? '添加中...' : '确认添加' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/utils/api'

const admins = ref([])
const showAdd = ref(false)
const adding = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '', phone: '', email: '' })

onMounted(() => fetchAdmins())

async function fetchAdmins() {
  try {
    const res = await userApi.listByRole('ADMIN')
    admins.value = res.data || []
  } catch (e) { /* */ }
}

async function handleAdd() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '用户名和密码为必填项'
    return
  }
  if (form.username.length < 3) {
    error.value = '用户名长度至少3个字符'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码长度至少6个字符'
    return
  }
  adding.value = true
  try {
    await userApi.register({ ...form, role: 'ADMIN' })
    showAdd.value = false
    Object.assign(form, { username: '', password: '', phone: '', email: '' })
    fetchAdmins()
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || '添加失败'
  } finally {
    adding.value = false
  }
}

async function handleDelete(u) {
  if (!confirm(`确定删除管理员「${u.username}」吗？删除后不可恢复。`)) return
  try {
    await userApi.remove(u.id)
    admins.value = admins.value.filter(a => a.id !== u.id)
  } catch (e) { /* */ }
}

async function toggleStatus(u) {
  const newStatus = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  if (!confirm(`确定${newStatus === 'DISABLED' ? '禁用' : '启用'}该管理员吗？`)) return
  try {
    await userApi.updateStatus(u.id, newStatus)
    u.status = newStatus
  } catch (e) { /* */ }
}
</script>

<style scoped>
h3 { margin: 0; color: #111827; font-size: 18px; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }

.btn-add {
  padding: 8px 20px; border: none; border-radius: 6px;
  background: #3b82f6; color: #fff; font-size: 14px; cursor: pointer;
}
.btn-add:hover { background: #2563eb; }

.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.tag { padding: 2px 10px; border-radius: 3px; font-size: 13px; }
.tag.active { background: #d1fae5; color: #065f46; }
.tag.disabled { background: #fee2e2; color: #991b1b; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; margin-right: 6px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
.btn-sm.primary { color: #3b82f6; border-color: #93c5fd; }
.btn-sm.delete { color: #dc2626; border-color: #fca5a5; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal {
  width: 420px; background: #fff; border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.25);
}
.modal-header {
  padding: 16px 20px; border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; color: #111827;
}
.modal-close { border: none; background: none; font-size: 18px; color: #9ca3af; cursor: pointer; }
.modal-close:hover { color: #374151; }
.modal-body { padding: 20px; }
.modal-footer {
  padding: 12px 20px; border-top: 1px solid #e5e7eb;
  display: flex; justify-content: flex-end; gap: 10px;
}
.form-item { margin-bottom: 14px; }
.form-item label { display: block; margin-bottom: 4px; font-size: 13px; color: #374151; }
.form-item input {
  width: 100%; height: 38px; padding: 0 12px; border: 1px solid #d1d5db; border-radius: 6px;
  font-size: 14px; outline: none; box-sizing: border-box;
}
.form-item input:focus { border-color: #3b82f6; }
.required { color: #ef4444; }
.error { color: #ef4444; font-size: 13px; margin-top: 4px; }

.btn-cancel {
  padding: 8px 20px; border: 1px solid #d1d5db; border-radius: 6px;
  background: #fff; color: #374151; font-size: 14px; cursor: pointer;
}
.btn-cancel:hover { background: #f3f4f6; }
.btn-confirm {
  padding: 8px 20px; border: none; border-radius: 6px;
  background: #3b82f6; color: #fff; font-size: 14px; cursor: pointer;
}
.btn-confirm:hover { background: #2563eb; }
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
