<template>
  <div class="layout">
    <!-- ===== Sidebar ===== -->
    <aside class="sidebar animate-fade-in">
      <div class="sidebar-header" @click="$router.push('/')">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 5.172C10 3.782 8.423 2.679 6.5 3c-2.823.47-4.113 6.006-4 7 .08.703 4.134 5.47 6 8 2.19 3.042 6 3 6 3s3.81.042 6-3c1.87-2.53 5.92-7.297 6-8 .088-.993-1.177-6.584-4-7-1.923-.321-3.5 1.782-3.5 3.172v.172"/>
          </svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">PetFeeding</span>
          <span class="logo-sub">管理平台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <template v-for="group in menuGroups" :key="group.name">
          <div class="nav-group">
            <div class="nav-group-title">{{ group.title }}</div>
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="nav-item"
              :class="{ active: isActive(item.path) }"
            >
              <span class="nav-icon">{{ item.icon }}</span>
              <span class="nav-label">{{ item.label }}</span>
            </router-link>
          </div>
        </template>
      </nav>
    </aside>

    <!-- ===== Main ===== -->
    <div class="main">
      <!-- Header -->
      <header class="header">
        <div class="header-left">
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <div class="user-menu">
            <div class="user-avatar">
              {{ username.charAt(0).toUpperCase() }}
            </div>
            <div class="user-info">
              <span class="user-name">{{ username }}</span>
              <span class="user-role">{{ roleLabel }}</span>
            </div>
          </div>
          <button class="logout-btn" @click="logout" title="退出登录">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </div>
      </header>

      <!-- Content -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const username = ref(localStorage.getItem('username') || '管理员')
const role = ref(localStorage.getItem('role') || '')

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', SUPER_ADMIN: '超级管理员', OWNER: '客户', FEEDER: '喂养员' }
  return map[role.value] || role.value
})

const menuGroups = [
  {
    name: 'overview',
    title: '概览',
    items: [
      { path: '/', label: '仪表盘', icon: 'data' }
    ]
  },
  {
    name: 'business',
    title: '业务管理',
    items: [
      { path: '/users', label: '客户管理', icon: 'people' },
      { path: '/pets', label: '宠物管理', icon: 'pet' },
      { path: '/feeders', label: '喂养员管理', icon: 'feeder' },
      { path: '/orders', label: '订单管理', icon: 'order' },
      { path: '/reviews', label: '评价管理', icon: 'review' },
      { path: '/payments', label: '支付记录', icon: 'payment' }
    ]
  },
  {
    name: 'system',
    title: '系统管理',
    items: [
      { path: '/admins', label: '管理员管理', icon: 'shield' }
    ]
  }
]

const iconMap = {
  data: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>`,
  people: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
  pet: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 5.172C10 3.782 8.423 2.679 6.5 3c-2.823.47-4.113 6.006-4 7 .08.703 4.134 5.47 6 8 2.19 3.042 6 3 6 3s3.81.042 6-3c1.87-2.53 5.92-7.297 6-8 .088-.993-1.177-6.584-4-7-1.923-.321-3.5 1.782-3.5 3.172v.172"/><path d="M14 11c0 1.1-.9 2-2 2s-2-.9-2-2"/><path d="M16 9.5c.5.5 1 .5 1.5 0"/></svg>`,
  feeder: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  order: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`,
  review: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
  payment: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>`,
  shield: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`
}

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const pageTitle = computed(() => {
  for (const group of menuGroups) {
    for (const item of group.items) {
      if (item.path === route.path || (item.path !== '/' && route.path.startsWith(item.path))) {
        return item.label
      }
    }
  }
  return '仪表盘'
})

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* ===== Sidebar ===== */
.sidebar {
  width: 240px;
  background: var(--surface-sidebar);
  color: #fff;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  cursor: pointer;
  transition: opacity var(--transition-fast);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.sidebar-header:hover { opacity: 0.85; }

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--brand-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logo-icon svg {
  width: 20px;
  height: 20px;
  color: #fff;
}

.logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.logo-title {
  font-family: var(--font-display);
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.3px;
  color: #fff;
}
.logo-sub {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 0.5px;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}

.nav-group {
  margin-top: 8px;
}
.nav-group:first-child { margin-top: 4px; }

.nav-group-title {
  padding: 12px 20px 6px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.3);
  letter-spacing: 0.8px;
  text-transform: uppercase;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  margin: 2px 8px;
  color: rgba(255, 255, 255, 0.55);
  text-decoration: none;
  font-size: 14px;
  font-weight: 400;
  border-radius: 8px;
  transition: all var(--transition-fast);
  position: relative;
}
.nav-item:hover {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.06);
}
.nav-item.active {
  color: #fff;
  background: var(--brand-primary);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
}
.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  flex-shrink: 0;
}
.nav-icon svg {
  width: 18px;
  height: 18px;
}
.nav-label {
  white-space: nowrap;
}

/* ===== Main ===== */
.main {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Header */
.header {
  height: 64px;
  padding: 0 28px;
  background: var(--surface-header);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--neutral-200);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 600;
  color: var(--neutral-900);
  letter-spacing: -0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--brand-gradient);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--neutral-800);
}
.user-role {
  font-size: 11px;
  color: var(--neutral-400);
}

.logout-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--neutral-400);
  cursor: pointer;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}
.logout-btn:hover {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

/* Content */
.content {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
}
</style>
