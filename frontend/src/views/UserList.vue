<template>
  <PageTable
    title="客户管理"
    desc="管理平台上的宠物主人账户"
    :data="users"
    :columns="columns"
    :loading="loading"
  >
    <template #filters>
      <el-select v-model="genderFilter" placeholder="性别筛选" clearable style="width:150px" @change="fetchUsers">
        <el-option label="全部性别" value="" />
        <el-option label="男" value="男" />
        <el-option label="女" value="女" />
      </el-select>
    </template>
    <template #username="{ item }">
      <div class="user-cell">
        <div class="user-avatar-sm">{{ item.username.charAt(0).toUpperCase() }}</div>
        <span>{{ item.username }}</span>
      </div>
    </template>
    <template #gender="{ item }">
      <span v-if="item.gender === '男'">👨 男</span>
      <span v-else-if="item.gender === '女'">👩 女</span>
      <span v-else class="text-muted">-</span>
    </template>
    <template #status="{ item }">
      <span class="tag" :class="item.status === 'ACTIVE' ? 'tag-active' : 'tag-disabled'">
        {{ item.status === 'ACTIVE' ? '正常' : '禁用' }}
      </span>
    </template>
    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="openEdit(item)">编辑</button>
      <button class="btn btn-sm btn-outline" @click="toggleStatus(item)" style="margin-left:4px">
        {{ item.status === 'ACTIVE' ? '禁用' : '启用' }}
      </button>
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
    </template>
  </PageTable>

  <el-dialog v-model="editVisible" title="编辑客户" width="420px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名">
        <el-input :model-value="form.username" disabled />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" placeholder="请选择" style="width:100%">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="手机号" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="邮箱" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const genderFilter = ref('')

const columns = [
  { key: 'id', label: '编号', style: 'width:60px' },
  { key: 'username', label: '用户名' },
  { key: 'phone', label: '手机号' },
  { key: 'email', label: '邮箱' },
  { key: 'gender', label: '性别' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '注册时间', format: v => v ? v.replace('T', ' ') : '-' },
]

const editVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, username: '', gender: '', phone: '', email: '' })

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await userApi.listByRole('OWNER', genderFilter.value)
    users.value = res.data || []
  } catch (e) { /* */ }
  finally { loading.value = false }
}

function openEdit(u) {
  form.id = u.id
  form.username = u.username
  form.gender = u.gender || ''
  form.phone = u.phone || ''
  form.email = u.email || ''
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await userApi.update(form.id, {
      gender: form.gender || null,
      phone: form.phone,
      email: form.email
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    await fetchUsers()
  } catch (e) { /* */ }
  finally { saving.value = false }
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
.text-muted { color: #9ca3af; }
</style>
