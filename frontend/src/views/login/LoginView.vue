<template>
  <div class="login-page">
    <!-- Animated background blobs -->
    <div class="bg-blobs">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="login-card animate-fade-in-up">
      <div class="card-header">
        <div class="card-logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="logo-paw">
            <path d="M10 5.172C10 3.782 8.423 2.679 6.5 3c-2.823.47-4.113 6.006-4 7 .08.703 4.134 5.47 6 8 2.19 3.042 6 3 6 3s3.81.042 6-3c1.87-2.53 5.92-7.297 6-8 .088-.993-1.177-6.584-4-7-1.923-.321-3.5 1.782-3.5 3.172v.172"/>
          </svg>
        </div>
        <h1>宠物喂养平台</h1>
        <p class="card-subtitle">宠物上门喂养管理平台</p>
      </div>

      <form class="form" @submit.prevent="handleLogin">
        <div class="form-item">
          <label>用户名</label>
          <div class="input-wrapper">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <input v-model="form.username" type="text" placeholder="请输入用户名" autocomplete="username" />
          </div>
        </div>
        <div class="form-item">
          <label>密码</label>
          <div class="input-wrapper">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="input-icon">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" autocomplete="current-password" />
          </div>
        </div>

        <div class="error" v-if="error">{{ error }}</div>

        <button class="btn-submit" type="submit" :disabled="loading">
          <svg v-if="loading" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" class="spin">
            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
          </svg>
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

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
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f0f1a;
  overflow: hidden;
  position: relative;
}

/* Animated background blobs */
.bg-blobs {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.blob-1 {
  width: 400px; height: 400px;
  background: #6366f1;
  top: -10%; left: -5%;
  animation-delay: 0s;
}
.blob-2 {
  width: 350px; height: 350px;
  background: #8b5cf6;
  bottom: -10%; right: -5%;
  animation-delay: -3s;
}
.blob-3 {
  width: 300px; height: 300px;
  background: #a78bfa;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
  opacity: 0.3;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* Card */
.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  color: #fff;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}
.card-logo {
  width: 52px;
  height: 52px;
  margin: 0 auto 16px;
  background: var(--brand-gradient);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-paw {
  width: 26px;
  height: 26px;
  color: #fff;
}
.card-header h1 {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.3px;
  margin-bottom: 4px;
}
.card-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
}

/* Form */
.form { margin-bottom: 24px; }
.form-item { margin-bottom: 16px; }
.form-item label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 6px;
}
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 12px;
  color: rgba(255, 255, 255, 0.3);
  pointer-events: none;
  transition: color var(--transition-fast);
}
.input-wrapper input {
  width: 100%;
  height: 42px;
  padding: 0 12px 0 38px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all var(--transition-fast);
}
.input-wrapper input::placeholder { color: rgba(255, 255, 255, 0.25); }
.input-wrapper input:focus {
  border-color: var(--brand-primary);
  background: rgba(99, 102, 241, 0.08);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}
.input-wrapper input:focus + .input-icon,
.input-wrapper:focus-within .input-icon {
  color: var(--brand-primary-light);
}

.error {
  color: #fca5a5;
  font-size: 13px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.btn-submit {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: var(--brand-gradient);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}
.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
}
.btn-submit:active:not(:disabled) { transform: translateY(0); }
.btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

</style>
