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
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="stat.iconPath" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stat.displayValue }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="charts-row animate-fade-in-up" :style="{ animationDelay: '0.25s' }">
      <div class="chart-card">
        <h3 class="section-title">近 7 天订单趋势</h3>
        <div class="chart-wrap">
          <canvas ref="trendCanvas"></canvas>
        </div>
      </div>
      <div class="chart-card">
        <h3 class="section-title">订单状态分布</h3>
        <div class="chart-wrap donut-wrap">
          <canvas ref="statusCanvas"></canvas>
        </div>
      </div>
    </div>

    <!-- Feeds Row -->
    <div class="feeds-row animate-fade-in-up" :style="{ animationDelay: '0.35s' }">
      <div class="feed-card">
        <h3 class="section-title">最近订单</h3>
        <div class="feed-list" v-if="recentOrders.length">
          <div class="feed-item" v-for="o in recentOrders" :key="o.id">
            <div class="feed-dot" :class="statusDotClass(o.status)"></div>
            <div class="feed-body">
              <span class="feed-title">{{ o.orderNo }}</span>
              <span class="feed-meta">{{ orderStatusLabel(o.status) }}</span>
            </div>
            <div class="feed-right">
              <span class="feed-price">¥{{ o.price }}</span>
              <span class="feed-time">{{ formatTimeAgo(o.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div v-else class="feed-empty">暂无订单</div>
      </div>
      <div class="feed-card">
        <h3 class="section-title">最近注册</h3>
        <div class="feed-list" v-if="recentUsers.length">
          <div class="feed-item" v-for="u in recentUsers" :key="u.id">
            <div class="feed-avatar" :class="u.role === 'ADMIN' ? 'admin' : ''">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            </div>
            <div class="feed-body">
              <span class="feed-title">{{ u.nickname || u.username }}</span>
              <span class="feed-meta">{{ u.role === 'ADMIN' ? '管理员' : '宠物主人' }}</span>
            </div>
            <span class="feed-time">{{ formatTimeAgo(u.createdAt) }}</span>
          </div>
        </div>
        <div v-else class="feed-empty">暂无新用户</div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions animate-fade-in-up" :style="{ animationDelay: '0.45s' }">
      <h3 class="section-title">快捷操作</h3>
      <div class="action-grid">
        <router-link v-for="action in actions" :key="action.to" :to="action.to" class="action-card">
          <div class="action-icon" :class="action.color">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="action.icon" />
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
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { dashboardApi } from '@/utils/api'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const stats = ref({ owners: 0, admins: 0, pets: 0, feeders: 0, orders: 0, pendingOrders: 0, revenue: 0 })
const orderTrend = ref([])
const orderStatusBreakdown = ref({})
const recentOrders = ref([])
const recentUsers = ref([])
const currentTime = ref('')

function updateTime() {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}年${pad(now.getMonth()+1)}月${pad(now.getDate())}日 ${pad(now.getHours())}:${pad(now.getMinutes())}`
}

const trendCanvas = ref(null)
const statusCanvas = ref(null)
let trendChart = null
let statusChart = null

onMounted(async () => {
  try {
    const res = await dashboardApi.stats()
    if (res.data) {
      stats.value = res.data
      orderTrend.value = res.data.orderTrend || []
      orderStatusBreakdown.value = res.data.orderStatusBreakdown || {}
      recentOrders.value = res.data.recentOrders || []
      recentUsers.value = res.data.recentUsers || []
      await nextTick()
      buildCharts()
    }
  } catch (e) { /* */ }
  updateTime()
  timer = setInterval(updateTime, 30000)
})

let timer
onUnmounted(() => {
  clearInterval(timer)
  if (trendChart) trendChart.destroy()
  if (statusChart) statusChart.destroy()
})

function buildCharts() {
  // 订单趋势折线图
  if (trendCanvas.value && orderTrend.value.length) {
    const labels = orderTrend.value.map(d => d.date.slice(5)) // MM-DD
    const data = orderTrend.value.map(d => d.count)
    const gradient = trendCanvas.value.getContext('2d').createLinearGradient(0, 0, 0, 200)
    gradient.addColorStop(0, 'rgba(99,102,241,0.15)')
    gradient.addColorStop(1, 'rgba(99,102,241,0)')
    trendChart = new Chart(trendCanvas.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: '订单数',
          data,
          borderColor: '#6366f1',
          backgroundColor: gradient,
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointBackgroundColor: '#6366f1',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 6,
          borderWidth: 2,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          x: { grid: { display: false }, ticks: { font: { size: 11 }, color: '#9ca3af' } },
          y: { beginAtZero: true, ticks: { stepSize: 1, font: { size: 11 }, color: '#9ca3af' }, grid: { color: '#f3f4f6' } }
        }
      }
    })
  }

  // 订单状态饼图
  if (statusCanvas.value && Object.keys(orderStatusBreakdown.value).length) {
    const statusMap = {
      PENDING: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消'
    }
    const colorMap = {
      PENDING: '#f59e0b', ACCEPTED: '#3b82f6', IN_PROGRESS: '#8b5cf6', COMPLETED: '#10b981', CANCELLED: '#ef4444'
    }
    const entries = Object.entries(orderStatusBreakdown.value)
    const labels = entries.map(([k]) => statusMap[k] || k)
    const data = entries.map(([, v]) => v)
    const colors = entries.map(([k]) => colorMap[k] || '#d1d5db')
    statusChart = new Chart(statusCanvas.value, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{ data, backgroundColor: colors, borderWidth: 2, borderColor: '#fff' }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 16, usePointStyle: true, pointStyleWidth: 8, font: { size: 11 }, color: '#6b7280' }
          }
        }
      }
    })
  }
}

// 图标 SVG paths
const iconPaths = {
  People: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
  Heart: '<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>',
  Star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>',
  FileText: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>',
  Clock: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  TrendingUp: '<polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>',
  CheckSquare: '<path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
  Truck: '<rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>',
  Users: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
}

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
    iconPath: iconPaths[def.icon]
  }))
}

watch(stats, buildStatCards, { immediate: true })

const actions = [
  { to: '/feeders', title: '喂养员审核', desc: '查看待审核的喂养员申请', color: 'blue', icon: '<path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>' },
  { to: '/orders', title: '订单管理', desc: '分配、处理和管理订单', color: 'purple', icon: '<rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>' },
  { to: '/users', title: '客户管理', desc: '维护客户信息和状态', color: 'emerald', icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>' },
]

function orderStatusLabel(status) {
  const map = { PENDING: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

function statusDotClass(status) {
  const map = { PENDING: 'dot-amber', ACCEPTED: 'dot-blue', IN_PROGRESS: 'dot-purple', COMPLETED: 'dot-green', CANCELLED: 'dot-red' }
  return map[status] || ''
}

function formatTimeAgo(iso) {
  if (!iso) return ''
  const now = Date.now()
  const then = new Date(iso).getTime()
  const diff = Math.floor((now - then) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`
  return iso.slice(0, 10)
}
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
  margin-bottom: 24px;
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

/* Charts Row */
.charts-row {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 14px;
  margin-bottom: 24px;
}
.chart-card {
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 20px 24px;
}
.chart-wrap {
  height: 220px;
  position: relative;
}
.donut-wrap {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Feeds */
.feeds-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 32px;
}
.feed-card {
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 20px 24px;
}

.feed-list {
  display: flex;
  flex-direction: column;
}
.feed-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--neutral-100);
}
.feed-item:last-child { border-bottom: none; }

.feed-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-amber { background: #f59e0b; }
.dot-blue { background: #3b82f6; }
.dot-purple { background: #8b5cf6; }
.dot-green { background: #10b981; }
.dot-red { background: #ef4444; }

.feed-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--brand-gradient-subtle);
  color: var(--brand-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.feed-avatar.admin {
  background: rgba(139,92,246,0.1);
  color: #8b5cf6;
}

.feed-body {
  flex: 1;
  min-width: 0;
}
.feed-title {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--neutral-800);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.feed-meta {
  font-size: 11px;
  color: var(--neutral-400);
}
.feed-right {
  text-align: right;
  flex-shrink: 0;
}
.feed-price {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--brand-primary);
}
.feed-time {
  font-size: 11px;
  color: var(--neutral-400);
}

.feed-empty {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: var(--neutral-400);
}

/* Section Title */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--neutral-700);
  margin-bottom: 14px;
}

/* Quick Actions */
.quick-actions { }
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

@media (max-width: 768px) {
  .charts-row, .feeds-row { grid-template-columns: 1fr; }
}
</style>
