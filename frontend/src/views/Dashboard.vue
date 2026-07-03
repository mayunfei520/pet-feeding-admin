<template>
  <div class="dashboard">
    <div class="dash-header animate-fade-in-up">
      <div>
        <h2 class="dash-title">数据概览</h2>
        <p class="dash-desc">实时掌握平台运营状况</p>
      </div>
      <div class="dash-time">{{ currentTime }}</div>
    </div>

    <!-- Stat Cards -->
    <div class="stats-grid">
      <div
        v-for="(stat, index) in statCards"
        :key="stat.label"
        class="stat-card animate-fade-in-up"
        :class="stat.variant"
        :style="{ animationDelay: `${index * 0.06}s` }"
      >
        <div class="stat-icon-wrap" :class="stat.variant">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <component :is="'_' + stat.icon" />
          </svg>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stat.displayValue }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions animate-fade-in-up" :style="{ animationDelay: '0.3s' }">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-grid">
        <router-link v-for="action in actions" :key="action.to" :to="action.to" class="action-card">
          <div class="action-icon" :class="action.color">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </div>
          <div class="action-content">
            <div class="action-title">{{ action.title }}</div>
            <div class="action-desc">{{ action.desc }}</div>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { dashboardApi } from '@/utils/api'

const stats = ref({ owners: 0, admins: 0, pets: 0, feeders: 0, orders: 0, pendingOrders: 0, revenue: 0 })
const currentTime = ref('')

function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}年${pad(now.getMonth()+1)}月${pad(now.getDate())}日 ${pad(now.getHours())}:${pad(now.getMinutes())}`
}

let timer
onMounted(async () => {
  try {
    const res = await dashboardApi.stats()
    if (res.data) stats.value = res.data
  } catch (e) { /* */ }
  updateTime()
  timer = setInterval(updateTime, 30000)
})
onUnmounted(() => clearInterval(timer))

const statCards = ref([])

const statDefs = [
  { label: '客户总数', key: 'owners', icon: 'People', variant: 'primary', prefix: '' },
  { label: '宠物总数', key: 'pets', icon: 'Heart', variant: 'purple', prefix: '' },
  { label: '喂养员', key: 'feeders', icon: 'Star', variant: 'amber', prefix: '' },
  { label: '订单总数', key: 'orders', icon: 'FileText', variant: 'blue', prefix: '' },
  { label: '待处理订单', key: 'pendingOrders', icon: 'Clock', variant: 'rose', prefix: '' },
  { label: '总营收', key: 'revenue', icon: 'TrendingUp', variant: 'emerald', prefix: '¥' },
]

function buildStatCards() {
  statCards.value = statDefs.map(def => ({
    label: def.label,
    displayValue: `${def.prefix}${stats.value[def.key] ?? 0}`,
    variant: def.variant,
    icon: def.icon
  }))
}

// Watch stats changes
watch(stats, buildStatCards, { immediate: true })

const actions = [
  { to: '/feeders', title: '喂养员审核', desc: '查看待审核的喂养员申请', color: 'blue' },
  { to: '/orders', title: '订单管理', desc: '分配、处理和管理订单', color: 'purple' },
  { to: '/users', title: '客户管理', desc: '维护客户信息和状态', color: 'emerald' },
]
</script>

<style scoped>
.dashboard { max-width: 1200px; }

.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
}
.dash-title {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  color: var(--neutral-900);
  letter-spacing: -0.3px;
}
.dash-desc {
  font-size: 13px;
  color: var(--neutral-400);
  margin-top: 2px;
}
.dash-time {
  font-size: 13px;
  color: var(--neutral-400);
  font-variant-numeric: tabular-nums;
}

/* Stat Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}

.stat-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon-wrap svg { width: 20px; height: 20px; }

.stat-icon-wrap.primary { background: rgba(99, 102, 241, 0.1); color: var(--brand-primary); }
.stat-icon-wrap.purple { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; }
.stat-icon-wrap.amber { background: rgba(245, 158, 11, 0.1); color: var(--color-warning); }
.stat-icon-wrap.blue { background: var(--color-info-bg); color: var(--color-info); }
.stat-icon-wrap.rose { background: rgba(244, 63, 94, 0.1); color: #f43f5e; }
.stat-icon-wrap.emerald { background: var(--color-success-bg); color: var(--color-success); }

.stat-body { min-width: 0; }
.stat-value {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--neutral-900);
  letter-spacing: -0.5px;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--neutral-400);
  margin-top: 2px;
}

/* Quick Actions */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--neutral-700);
  margin-bottom: 14px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  text-decoration: none;
  color: inherit;
  transition: all var(--transition-base);
}
.action-card:hover {
  border-color: transparent;
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.action-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}
.action-icon.blue { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.action-icon.purple { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }
.action-icon.emerald { background: linear-gradient(135deg, #10b981, #34d399); }
.action-icon svg { width: 18px; height: 18px; }

.action-content { min-width: 0; }
.action-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-800);
}
.action-desc {
  font-size: 12px;
  color: var(--neutral-400);
  margin-top: 1px;
}
</style>
