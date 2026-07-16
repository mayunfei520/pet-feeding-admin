<template>
  <div class="dashboard">
    <!-- ========== 加载骨架屏 ========== -->
    <template v-if="loading && !hasLoadedOnce">
      <div class="kpi-grid">
        <div v-for="n in 6" :key="'sk'+n" class="kpi-card skeleton glass">
          <div class="kpi-icon-sk shimmer"></div>
          <div class="kpi-body-sk">
            <div class="kpi-value-sk shimmer"></div>
            <div class="kpi-label-sk shimmer"></div>
          </div>
        </div>
      </div>
      <div class="charts-row">
        <div class="chart-card skeleton-card glass"><div class="chart-sk shimmer" style="height:220px"></div></div>
        <div class="chart-card skeleton-card glass"><div class="chart-sk shimmer" style="height:220px"></div></div>
        <div class="chart-card skeleton-card glass"><div class="chart-sk shimmer" style="height:220px"></div></div>
      </div>
    </template>

    <!-- ========== 错误状态 ========== -->
    <div v-else-if="loadError && !hasData" class="error-block">
      <div class="error-icon">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      </div>
      <p class="error-msg">数据加载失败，请检查网络连接</p>
      <button class="btn btn-primary" @click="fetchData">重新加载</button>
    </div>

    <!-- ========== 主内容 ========== -->
    <template v-else>
      <div v-if="loadError && hasData" class="soft-alert">
        数据刷新失败，当前展示上一次成功加载的数据
        <button class="soft-link" @click="fetchData">重试</button>
      </div>

      <!-- KPI 指标卡 -->
      <div class="kpi-grid">
        <div
          v-for="(card, i) in kpiCards"
          :key="card.key"
          class="kpi-card glass animate-fade-in-up"
          :class="card.accent"
          :style="{ animationDelay: `${0.05 + i * 0.06}s` }"
          :ref="el => { if (el) kpiCardRefs[card.key] = el }"
          @mousemove="onCardMove($event, card.key)"
          @mouseleave="onCardLeave(card.key)"
        >
          <div class="kpi-icon" :class="card.accent">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" v-html="card.icon" />
          </div>
          <div class="kpi-body">
            <div class="kpi-label">{{ card.label }}</div>
            <div class="kpi-value">
              <span class="count-up" :ref="el => { if (el) countRefs[card.key] = el }">0</span>
              <span v-if="card.suffix" class="kpi-suffix">{{ card.suffix }}</span>
            </div>
            <div v-if="card.sub" class="kpi-sub">{{ card.sub }}</div>
          </div>
          <div v-if="card.key === 'pendingOrders' && kpiRaw.pendingOrders > 0" class="kpi-alert-dot"></div>
        </div>
      </div>

      <div class="charts-row animate-fade-in-up" :style="{ animationDelay: '0.4s' }">
        <div class="chart-card glass">
          <div class="chart-header">
            <h3 class="chart-title">近 7 天订单趋势</h3>
            <span class="chart-legend">
              <span class="legend-dot"></span> 订单量
            </span>
          </div>
          <div class="chart-body">
            <template v-if="orderTrend.length && orderTrend.some(d => d.count > 0)">
              <canvas ref="trendCanvas"></canvas>
            </template>
            <div v-else class="chart-empty">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>
              <span>暂无订单数据</span>
            </div>
          </div>
        </div>
        <div class="chart-card glass">
          <div class="chart-header">
            <h3 class="chart-title">订单状态分布</h3>
          </div>
          <div class="chart-body donut-body">
            <template v-if="orderStatusTotal > 0">
              <canvas ref="statusCanvas"></canvas>
              <div class="donut-center">
                <div class="dc-val">{{ orderStatusTotal }}</div>
                <div class="dc-label">总订单</div>
              </div>
            </template>
            <div v-else class="chart-empty">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/></svg>
              <span>暂无订单数据</span>
            </div>
          </div>
        </div>
        <div class="chart-card glass">
          <div class="chart-header">
            <h3 class="chart-title">客户性别分布</h3>
          </div>
          <div class="chart-body donut-body">
            <template v-if="ownerGenderTotal > 0">
              <canvas ref="genderCanvas"></canvas>
              <div class="donut-center">
                <div class="dc-val">{{ ownerGenderTotal }}</div>
                <div class="dc-label">客户数</div>
              </div>
            </template>
            <div v-else class="chart-empty">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              <span>暂无客户数据</span>
            </div>
          </div>
        </div>
      </div>

      <div class="feeds-row animate-fade-in-up" :style="{ animationDelay: '0.5s' }">
        <div class="feed-card glass">
          <div class="chart-header">
            <h3 class="chart-title">最近订单</h3>
            <router-link to="/orders" class="feed-more">查看全部 &rarr;</router-link>
          </div>
          <div class="feed-list" v-if="recentOrders.length">
            <div class="feed-item" v-for="o in recentOrders" :key="o.id">
              <div class="feed-dot" :class="statusDotClass(o.status)"></div>
              <div class="feed-info">
                <span class="feed-name">{{ o.orderNo }}</span>
                <span class="feed-tag" :class="statusTagClass(o.status)">{{ orderStatusLabel(o.status) }}</span>
              </div>
              <div class="feed-meta">
                <span class="feed-price">¥{{ fmtPrice(o.price) }}</span>
                <span class="feed-time">{{ fmtRelative(o.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="feed-empty">暂无订单记录</div>
        </div>
        <div class="feed-card glass">
          <div class="chart-header">
            <h3 class="chart-title">最近注册</h3>
            <router-link to="/users" class="feed-more">查看全部 &rarr;</router-link>
          </div>
          <div class="feed-list" v-if="recentUsers.length">
            <div class="feed-item" v-for="u in recentUsers" :key="u.id">
              <div class="feed-avatar" :class="u.role === 'ADMIN' ? 'avatar-admin' : ''">
                {{ (u.nickname || u.username || '?').charAt(0).toUpperCase() }}
              </div>
              <div class="feed-info">
                <span class="feed-name">{{ u.nickname || u.username }}</span>
                <span class="feed-tag" :class="u.role === 'ADMIN' ? 'tag-admin' : 'tag-owner'">
                  {{ u.role === 'ADMIN' ? '管理员' : '宠物主人' }}
                </span>
              </div>
              <span class="feed-time">{{ fmtRelative(u.createdAt) }}</span>
            </div>
          </div>
          <div v-else class="feed-empty">暂无新用户</div>
        </div>
      </div>

      <div class="quick-bar animate-fade-in-up" :style="{ animationDelay: '0.55s' }">
        <router-link v-for="act in quickActions" :key="act.to" :to="act.to" class="quick-card glass">
          <div class="quick-icon" :class="act.color">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="act.icon" />
          </div>
          <div class="quick-text">
            <span class="quick-title">{{ act.title }}</span>
            <span class="quick-desc">{{ act.desc }}</span>
          </div>
          <span class="quick-arrow">&rarr;</span>
        </router-link>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { dashboardApi } from '@/utils/api'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const loading = ref(true)
const loadError = ref(false)
const hasLoadedOnce = ref(false)
const kpiRaw = reactive({
  owners: 0, admins: 0, pets: 0, feeders: 0,
  orders: 0, pendingOrders: 0, revenue: 0
})
const orderTrend = ref([])
const payStatusBreakdown = ref({})
const recentOrders = ref([])
const recentUsers = ref([])
const ownerGenderBreakdown = ref({})

const orderStatusTotal = computed(() =>
  Object.values(payStatusBreakdown.value).reduce((a, b) => a + (b || 0), 0)
)
const ownerGenderTotal = computed(() =>
  Object.values(ownerGenderBreakdown.value).reduce((a, b) => a + (b || 0), 0)
)

const hasData = computed(() => {
  return Boolean(
    kpiRaw.owners || kpiRaw.admins || kpiRaw.pets || kpiRaw.feeders ||
    kpiRaw.orders || kpiRaw.pendingOrders || kpiRaw.revenue ||
    recentOrders.value.length || recentUsers.value.length || orderTrend.value.length
  )
})

const icons = {
  people: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
  heart: '<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>',
  star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>',
  file: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>',
  clock: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>',
  trending: '<polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/>',
  check: '<path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
  truck: '<rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>',
  users: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
}

const kpiCards = [
  { key: 'owners', label: '客户总数', icon: icons.people, accent: 'indigo' },
  { key: 'pets', label: '宠物总数', icon: icons.heart, accent: 'purple' },
  { key: 'feeders', label: '喂养员', icon: icons.star, accent: 'amber' },
  { key: 'orders', label: '订单总数', icon: icons.file, accent: 'blue' },
  { key: 'pendingOrders', label: '待处理订单', icon: icons.clock, accent: 'rose', suffix: '' },
  { key: 'revenue', label: '总营收', icon: icons.trending, accent: 'emerald', prefix: '¥' },
]

const quickActions = [
  { to: '/feeders', title: '喂养员审核', desc: '审核喂养员申请', color: 'blue', icon: icons.check },
  { to: '/orders', title: '订单管理', desc: '分配与处理订单', color: 'purple', icon: icons.truck },
  { to: '/users', title: '客户管理', desc: '维护客户信息', color: 'emerald', icon: icons.users },
]

const trendCanvas = ref(null)
const statusCanvas = ref(null)
const genderCanvas = ref(null)
const countRefs = reactive({})
const kpiCardRefs = reactive({})
let trendChart = null
let statusChart = null
let genderChart = null
let chartTimer = null

function destroyCharts() {
  if (chartTimer) { clearTimeout(chartTimer); chartTimer = null }
  if (trendChart) { trendChart.destroy(); trendChart = null }
  if (statusChart) { statusChart.destroy(); statusChart = null }
  if (genderChart) { genderChart.destroy(); genderChart = null }
}

/* 3D 倾斜：鼠标跟随 */
function onCardMove(e, key) {
  const el = kpiCardRefs[key]
  if (!el) return
  const r = el.getBoundingClientRect()
  const px = (e.clientX - r.left) / r.width - 0.5
  const py = (e.clientY - r.top) / r.height - 0.5
  el.style.transform = `perspective(900px) rotateX(${ -py * 9 }deg) rotateY(${ px * 9 }deg) translateY(-6px)`
}
function onCardLeave(key) {
  const el = kpiCardRefs[key]
  if (el) el.style.transform = ''
}

function fmtPrice(val) {
  const n = Number(val)
  if (isNaN(n)) return '0.00'
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fmtRelative(iso) {
  if (!iso) return ''
  const diff = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`
  return iso.slice(0, 10)
}

function orderStatusLabel(s) {
  return { PENDING: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
}
function statusDotClass(s) {
  return { PENDING: 'dot-amber', ACCEPTED: 'dot-blue', IN_PROGRESS: 'dot-purple', COMPLETED: 'dot-green', CANCELLED: 'dot-red' }[s] || ''
}
function statusTagClass(s) {
  return { PENDING: 'tag-amber', ACCEPTED: 'tag-blue', IN_PROGRESS: 'tag-purple', COMPLETED: 'tag-green', CANCELLED: 'tag-red' }[s] || ''
}

function animateNumbers() {
  const targets = {
    owners: kpiRaw.owners || 0,
    pets: kpiRaw.pets || 0,
    feeders: kpiRaw.feeders || 0,
    orders: kpiRaw.orders || 0,
    pendingOrders: kpiRaw.pendingOrders || 0,
    revenue: Number(kpiRaw.revenue) || 0,
  }
  const duration = 1400
  const start = performance.now()
  function frame(now) {
    const p = Math.min((now - start) / duration, 1)
    const ease = 1 - Math.pow(1 - p, 3)
    Object.entries(targets).forEach(([key, target]) => {
      const el = countRefs[key]
      if (!el) return
      const current = target * ease
      el.textContent = key === 'revenue'
        ? '¥' + Math.floor(current).toLocaleString('zh-CN')
        : Math.floor(current).toLocaleString('zh-CN')
    })
    if (p < 1) requestAnimationFrame(frame)
    else Object.entries(targets).forEach(([key, target]) => {
      const el = countRefs[key]
      if (!el) return
      el.textContent = key === 'revenue'
        ? '¥' + target.toLocaleString('zh-CN')
        : target.toLocaleString('zh-CN')
    })
  }
  requestAnimationFrame(frame)
}

function renderTrendChart() {
  if (!trendCanvas.value) return
  const data = orderTrend.value
  if (!data.length || !data.some(d => d.count > 0)) return
  const ctx = trendCanvas.value.getContext('2d')
  const gradient = ctx.createLinearGradient(0, 0, 0, 240)
  gradient.addColorStop(0, 'rgba(56,189,248,0.32)')
  gradient.addColorStop(1, 'rgba(56,189,248,0)')

  trendChart = new Chart(trendCanvas.value, {
    type: 'line',
    data: {
      labels: data.map(d => d.date.slice(5)),
      datasets: [{
        label: '订单数',
        data: data.map(d => d.count),
        borderColor: '#38bdf8',
        backgroundColor: gradient,
        fill: true,
        tension: 0.4,
        pointRadius: 5,
        pointHoverRadius: 7,
        pointBackgroundColor: '#7dd3fc',
        pointBorderColor: '#0f1a2e',
        pointBorderWidth: 2.5,
        borderWidth: 2.5,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { intersect: false, mode: 'index' },
      plugins: { legend: { display: false }, tooltip: {
        backgroundColor: '#0f1a2e',
        titleColor: '#e3ebf5',
        bodyColor: '#cdd9ec',
        borderColor: 'rgba(125,211,252,0.25)',
        borderWidth: 1,
        titleFont: { size: 13 },
        bodyFont: { size: 12 },
        padding: 10,
        cornerRadius: 10,
        displayColors: false,
      }},
      scales: {
        x: { grid: { display: false }, ticks: { font: { size: 11 }, color: '#6b7d99' } },
        y: { beginAtZero: true, ticks: { stepSize: 1, font: { size: 11 }, color: '#6b7d99' }, grid: { color: 'rgba(125,211,252,0.07)' } }
      }
    }
  })
}

function renderStatusChart() {
  if (!statusCanvas.value) return
  const data = payStatusBreakdown.value
  if (!Object.values(data).some(v => v > 0)) return
  const labelMap = { '成功': '成功', '待支付': '待支付', '付款': '付款', '取消': '取消' }
  const colorMap = { '成功': '#38bdf8', '待支付': '#fbbf24', '付款': '#a78bfa', '取消': '#f87171' }
  const entries = Object.entries(data).filter(([, v]) => v > 0)

  statusChart = new Chart(statusCanvas.value, {
    type: 'doughnut',
    data: {
      labels: entries.map(([k]) => labelMap[k] || k),
      datasets: [{
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map(([k]) => colorMap[k] || '#33455f'),
        borderColor: '#0f1a2e',
        borderWidth: 3,
        hoverBorderWidth: 4,
        hoverOffset: 8,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 18,
            usePointStyle: true,
            pointStyleWidth: 10,
            font: { size: 12 },
            color: '#8aa0bd',
            generateLabels(chart) {
              const ds = chart.data.datasets[0]
              return chart.data.labels.map((label, i) => ({
                text: `${label}  ${ds.data[i]}`,
                fillStyle: ds.backgroundColor[i],
                strokeStyle: ds.backgroundColor[i],
                pointStyle: 'circle',
                index: i,
              }))
            }
          }
        }
      }
    }
  })
}

function renderGenderChart() {
  if (!genderCanvas.value) return
  const data = ownerGenderBreakdown.value
  if (!Object.values(data).some(v => v > 0)) return
  const labelMap = { '男': '男', '女': '女', '未填': '未填' }
  const colorMap = { '男': '#38bdf8', '女': '#a78bfa', '未填': '#33455f' }
  const entries = Object.entries(data).filter(([, v]) => v > 0)

  genderChart = new Chart(genderCanvas.value, {
    type: 'doughnut',
    data: {
      labels: entries.map(([k]) => labelMap[k] || k),
      datasets: [{
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map(([k]) => colorMap[k] || '#33455f'),
        borderColor: '#0f1a2e',
        borderWidth: 3,
        hoverBorderWidth: 4,
        hoverOffset: 8,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 18,
            usePointStyle: true,
            pointStyleWidth: 10,
            font: { size: 12 },
            color: '#8aa0bd',
            generateLabels(chart) {
              const ds = chart.data.datasets[0]
              return chart.data.labels.map((label, i) => ({
                text: `${label}  ${ds.data[i]}`,
                fillStyle: ds.backgroundColor[i],
                strokeStyle: ds.backgroundColor[i],
                pointStyle: 'circle',
                index: i,
              }))
            }
          }
        }
      }
    }
  })
}

async function fetchData() {
  loading.value = true
  loadError.value = false
  try {
    const res = await dashboardApi.stats()
    const d = res.data
    if (d) {
      Object.assign(kpiRaw, {
        owners: d.owners ?? 0,
        admins: d.admins ?? 0,
        pets: d.pets ?? 0,
        feeders: d.feeders ?? 0,
        orders: d.orders ?? 0,
        pendingOrders: d.pendingOrders ?? 0,
        revenue: d.revenue ?? 0,
      })
      orderTrend.value = d.orderTrend || []
      payStatusBreakdown.value = d.payStatusBreakdown || {}
      ownerGenderBreakdown.value = d.ownerGenderBreakdown || {}
      recentOrders.value = d.recentOrders || []
      recentUsers.value = d.recentUsers || []
      hasLoadedOnce.value = true

      await nextTick()
      destroyCharts()
      animateNumbers()
      chartTimer = setTimeout(() => {
        renderTrendChart()
        renderStatusChart()
        renderGenderChart()
      }, 100)
    }
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
onUnmounted(() => { destroyCharts() })
</script>

<style scoped>
.dashboard {
  width: 100%;
  position: relative;
}

.soft-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 10px 14px;
  border: 1px solid rgba(251, 191, 36, 0.25);
  background: rgba(251, 191, 36, 0.10);
  color: #fcd34d;
  border-radius: 10px;
  font-size: 13px;
}
.soft-link {
  border: none;
  background: transparent;
  color: #fbbf24;
  font-weight: 600;
  cursor: pointer;
}
.soft-link:hover { text-decoration: underline; }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  border-radius: var(--radius-md);
  transition: transform 0.25s ease, box-shadow var(--transition-base), border-color var(--transition-base);
  cursor: default;
  overflow: hidden;
  transform-style: preserve-3d;
  will-change: transform;
}
.kpi-card:hover {
  border-color: var(--glass-border);
  box-shadow: var(--shadow-glow-ice);
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon.indigo { background: rgba(56,189,248,0.14); color: #7dd3fc; }
.kpi-icon.purple { background: rgba(167,139,250,0.16); color: #c4b5fd; }
.kpi-icon.amber  { background: rgba(251,191,36,0.14); color: #fbbf24; }
.kpi-icon.blue   { background: rgba(96,165,250,0.14); color: #93c5fd; }
.kpi-icon.rose   { background: rgba(248,113,113,0.14); color: #fca5a5; }
.kpi-icon.emerald{ background: rgba(52,211,153,0.14); color: #6ee7b7; }

.kpi-body { min-width: 0; transform: translateZ(20px); }
.kpi-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--neutral-400);
  text-transform: uppercase;
  letter-spacing: 0.4px;
  margin-bottom: 4px;
}
.kpi-value {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--neutral-800);
  letter-spacing: -0.5px;
  line-height: 1.15;
  font-variant-numeric: tabular-nums;
  text-shadow: 0 0 20px rgba(125, 211, 252, 0.18);
}
.count-up { display: inline-block; }
.kpi-suffix {
  font-size: 14px;
  font-weight: 500;
  color: var(--neutral-400);
  margin-left: 2px;
}
.kpi-sub {
  font-size: 11px;
  color: var(--neutral-400);
  margin-top: 2px;
}

.kpi-alert-dot {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f87171;
  box-shadow: 0 0 0 4px rgba(248, 113, 113, 0.2);
  animation: pulse-dot 2s ease-in-out infinite;
}
@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 4px rgba(248, 113, 113, 0.2); }
  50% { box-shadow: 0 0 0 10px rgba(248, 113, 113, 0); }
}

.charts-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  position: relative;
  border-radius: var(--radius-md);
  padding: 22px 24px;
  overflow: hidden;
}
/* 顶部流光 */
.chart-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #7dd3fc, #a78bfa, transparent);
  background-size: 200% 100%;
  animation: slideGlow 4.5s linear infinite;
  opacity: 0.75;
}
@keyframes slideGlow {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--neutral-800);
}
.chart-legend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--neutral-400);
}
.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #38bdf8;
}

.chart-body {
  position: relative;
  height: 240px;
}
.donut-body {
  display: flex;
  align-items: center;
  justify-content: center;
}
.donut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.dc-val {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 700;
  color: var(--ice-bright);
  text-shadow: 0 0 18px rgba(56, 189, 248, 0.35);
  line-height: 1;
}
.dc-label {
  font-size: 12px;
  color: var(--neutral-400);
  margin-top: 4px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--neutral-300);
  font-size: 13px;
}

.feeds-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.feed-card {
  border-radius: var(--radius-md);
  padding: 22px 24px;
}
.feed-more {
  font-size: 12px;
  font-weight: 500;
  color: var(--ice-bright);
  text-decoration: none;
}
.feed-more:hover { color: var(--ice); }

.feed-list { display: flex; flex-direction: column; }
.feed-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(125, 211, 252, 0.08);
}
.feed-item:last-child { border-bottom: none; padding-bottom: 0; }
.feed-item:first-child { padding-top: 0; }

.feed-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 10px currentColor;
}
.dot-amber  { background: #fbbf24; color: #fbbf24; }
.dot-blue   { background: #60a5fa; color: #60a5fa; }
.dot-purple { background: #a78bfa; color: #a78bfa; }
.dot-green  { background: #34d399; color: #34d399; }
.dot-red    { background: #f87171; color: #f87171; }

.feed-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: rgba(56,189,248,0.14);
  color: #7dd3fc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.feed-avatar.avatar-admin {
  background: rgba(167,139,250,0.16);
  color: #c4b5fd;
}

.feed-info {
  flex: 1; min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.feed-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--neutral-700);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.feed-tag {
  display: inline-block;
  width: fit-content;
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.tag-amber  { background: rgba(251,191,36,0.14); color: #fcd34d; }
.tag-blue   { background: rgba(96,165,250,0.14); color: #93c5fd; }
.tag-purple { background: rgba(167,139,250,0.16); color: #c4b5fd; }
.tag-green  { background: rgba(52,211,153,0.14); color: #6ee7b7; }
.tag-red    { background: rgba(248,113,113,0.14); color: #fca5a5; }
.tag-admin  { background: rgba(167,139,250,0.16); color: #c4b5fd; }
.tag-owner  { background: rgba(56,189,248,0.12); color: #7dd3fc; }

.feed-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.feed-price {
  font-size: 13px;
  font-weight: 600;
  color: var(--ice-bright);
}
.feed-time {
  font-size: 11px;
  color: var(--neutral-400);
}

.feed-empty {
  padding: 28px 0 8px;
  text-align: center;
  font-size: 13px;
  color: var(--neutral-400);
}

.quick-bar {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.quick-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: inherit;
  transition: transform var(--transition-base), box-shadow var(--transition-base), border-color var(--transition-base);
}
.quick-card:hover {
  border-color: var(--glass-border);
  box-shadow: var(--shadow-glow-violet);
  transform: translateY(-2px);
}
.quick-icon {
  width: 40px; height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #061018;
}
.quick-icon.blue    { background: linear-gradient(135deg, #38bdf8, #60a5fa); }
.quick-icon.purple  { background: linear-gradient(135deg, #a78bfa, #c4b5fd); }
.quick-icon.emerald { background: linear-gradient(135deg, #34d399, #6ee7b7); }

.quick-text { min-width: 0; }
.quick-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--neutral-800);
}
.quick-desc {
  font-size: 12px;
  color: var(--neutral-400);
  margin-top: 1px;
}
.quick-arrow {
  margin-left: auto;
  color: var(--ice-bright);
  font-size: 18px;
  transition: transform var(--transition-base);
}
.quick-card:hover .quick-arrow { transform: translateX(6px); }

.kpi-card.skeleton { pointer-events: none; }
.kpi-icon-sk {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: var(--neutral-100);
}
.kpi-body-sk { flex: 1; display: flex; flex-direction: column; gap: 10px; }
.kpi-value-sk {
  height: 28px; width: 80%;
  border-radius: 6px;
  background: var(--neutral-100);
}
.kpi-label-sk {
  height: 14px; width: 50%;
  border-radius: 4px;
  background: var(--neutral-100);
}
.skeleton-card { pointer-events: none; }
.chart-sk { border-radius: 8px; background: var(--neutral-100); }

.shimmer {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-block {
  text-align: center;
  padding: 80px 20px;
  color: var(--neutral-400);
}
.error-icon {
  margin-bottom: 16px;
  color: var(--neutral-300);
}
.error-msg {
  font-size: 15px;
  margin-bottom: 20px;
  color: var(--neutral-500);
}

@media (max-width: 1024px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
  .quick-bar { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .kpi-grid { grid-template-columns: 1fr; }
  .feeds-row { grid-template-columns: 1fr; }
  .quick-bar { grid-template-columns: 1fr; }
  .kpi-value { font-size: 24px; }
  .kpi-icon { width: 40px; height: 40px; border-radius: 10px; }
}
</style>
