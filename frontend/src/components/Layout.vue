<template>
  <div class="layout">
    <div class="sidebar">
      <div class="logo" @click="$router.push('/')">🐾 喂养管理后台</div>
      <div class="menu">
        <!-- 概览 -->
        <div class="menu-group">
          <div class="menu-group-title">概览</div>
          <router-link to="/" class="menu-item" :class="{ active: $route.path === '/' }">📊 仪表盘</router-link>
        </div>

        <!-- 业务管理 -->
        <div class="menu-group">
          <div class="menu-group-title">业务管理</div>
          <router-link to="/users" class="menu-item" :class="{ active: $route.path === '/users' }">👥 客户管理</router-link>
          <router-link to="/pets" class="menu-item" :class="{ active: $route.path === '/pets' }">🐱 宠物管理</router-link>
          <router-link to="/feeders" class="menu-item" :class="{ active: $route.path.startsWith('/feeders') }">👤 喂养员管理</router-link>
          <router-link to="/orders" class="menu-item" :class="{ active: $route.path.startsWith('/orders') }">📋 订单管理</router-link>
          <router-link to="/reviews" class="menu-item" :class="{ active: $route.path.startsWith('/reviews') }">⭐ 评价管理</router-link>
          <router-link to="/payments" class="menu-item" :class="{ active: $route.path === '/payments' }">💰 支付记录</router-link>
        </div>

        <!-- 系统管理 -->
        <div class="menu-group">
          <div class="menu-group-title">系统管理</div>
          <router-link to="/admins" class="menu-item" :class="{ active: $route.path === '/admins' }">🛡️ 管理员管理</router-link>
        </div>
      </div>
    </div>
    <div class="main">
      <div class="header">
        <span class="title">宠物上门喂养 · 管理后台</span>
        <div class="header-right">
          <span class="avatar">👤</span>
          <span class="username">{{ username }}</span>
          <span v-if="role" class="role-tag">{{ roleLabel }}</span>
          <button class="logout-btn" @click="logout">退出</button>
        </div>
      </div>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '管理员')
const role = ref(localStorage.getItem('role') || '')

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', SUPER_ADMIN: '超级管理员' }
  return map[role.value] || role.value
})

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout { display: flex; min-height: 100vh; }
.sidebar {
  width: 200px; background: #1f2937; color: #fff; flex-shrink: 0;
}
.logo {
  height: 56px; line-height: 56px; font-size: 17px; font-weight: bold; text-align: center;
  border-bottom: 1px solid rgba(255,255,255,0.08); cursor: pointer;
}
.menu { padding: 8px 0; }
.menu-group { margin-top: 4px; }
.menu-group:first-child { margin-top: 0; }
.menu-group-title {
  padding: 12px 20px 4px;
  font-size: 12px;
  color: #6b7280;
  letter-spacing: 0.5px;
}
.menu-item {
  display: block; padding: 12px 20px; color: #9ca3af; text-decoration: none; font-size: 15px;
  transition: all 0.15s;
}
.menu-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.menu-item.active { color: #fff; background: rgba(59,130,246,0.3); border-right: 3px solid #3b82f6; }
.main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.header {
  height: 48px; padding: 0 20px; background: #fff; border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: center; justify-content: space-between;
}
.title { font-size: 16px; color: #374151; font-weight: 500; }
.header-right { display: flex; align-items: center; gap: 10px; font-size: 15px; color: #6b7280; }
.avatar { font-size: 18px; }
.username { color: #374151; font-weight: 500; }
.role-tag {
  font-size: 12px; padding: 2px 8px; border-radius: 4px;
  background: #eff6ff; color: #3b82f6; border: 1px solid #bfdbfe;
}
.logout-btn { border: none; background: none; color: #ef4444; cursor: pointer; font-size: 15px; }
.content { flex: 1; padding: 20px; background: #f3f4f6; overflow-y: auto; }
</style>
