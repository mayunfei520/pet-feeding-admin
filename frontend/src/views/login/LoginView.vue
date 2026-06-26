<template>
  <div class="login-page">
    <div class="login-card">
      <h2>🐾 宠物喂养平台</h2>
      <p class="subtitle">后台管理系统</p>

      <div class="form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" @keyup.enter="handleLogin" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </div>
        <div class="error" v-if="error">{{ error }}</div>
        <button class="btn" @click="handleLogin" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const res = await userApi.login(form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role || '')
    router.push('/')
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: #1f2937;
}
.login-card {
  width: 380px; padding: 36px; background: #fff; border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}
h2 { text-align: center; color: #111827; margin-bottom: 4px; }
.subtitle { text-align: center; color: #9ca3af; font-size: 13px; margin-bottom: 24px; }
.form-item { margin-bottom: 16px; }
.form-item label { display: block; margin-bottom: 4px; font-size: 13px; color: #374151; }
.form-item input {
  width: 100%; height: 40px; padding: 0 12px; border: 1px solid #d1d5db; border-radius: 6px;
  font-size: 14px; box-sizing: border-box; outline: none;
}
.form-item input:focus { border-color: #3b82f6; }
.error { color: #ef4444; font-size: 13px; margin-bottom: 12px; }
.btn {
  width: 100%; height: 42px; border: none; border-radius: 6px;
  background: #1f2937; color: #fff; font-size: 15px; cursor: pointer;
}
.btn:hover { background: #374151; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
