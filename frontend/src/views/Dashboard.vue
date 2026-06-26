<template>
  <div>
    <h3>📊 仪表盘</h3>

    <div class="stats">
      <div class="stat-card">
        <div class="stat-num">{{ stats.users }}</div>
        <div class="stat-label">用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.pets }}</div>
        <div class="stat-label">宠物总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.feeders }}</div>
        <div class="stat-label">喂养员</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.orders }}</div>
        <div class="stat-label">订单总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.pendingOrders }}</div>
        <div class="stat-label">待处理订单</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">¥{{ stats.revenue }}</div>
        <div class="stat-label">总营收</div>
      </div>
    </div>

    <div class="quick-links">
      <router-link to="/feeders" class="qlink">
        <span class="ql-icon">📋</span>
        <span>待审核喂养员</span>
      </router-link>
      <router-link to="/orders" class="qlink">
        <span class="ql-icon">📦</span>
        <span>查看所有订单</span>
      </router-link>
      <router-link to="/users" class="qlink">
        <span class="ql-icon">👥</span>
        <span>管理客户</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/utils/api'

const stats = ref({ users: 0, pets: 0, feeders: 0, orders: 0, pendingOrders: 0, revenue: 0 })

onMounted(async () => {
  try {
    const res = await dashboardApi.stats()
    if (res.data) stats.value = res.data
  } catch (e) { /* */ }
})
</script>

<style scoped>
h3 { margin: 0 0 16px; color: #111827; font-size: 18px; }
.stats { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; margin-bottom: 24px; }
.stat-card {
  background: #fff; padding: 20px; border-radius: 6px; border: 1px solid #e5e7eb;
}
.stat-num { font-size: 32px; font-weight: 700; color: #111827; }
.stat-label { margin-top: 6px; font-size: 15px; color: #6b7280; }
.quick-links { display: flex; gap: 12px; }
.qlink {
  flex: 1; display: flex; align-items: center; gap: 8px; padding: 16px;
  background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; text-decoration: none;
  color: #374151; font-size: 15px;
}
.qlink:hover { background: #f9fafb; }
.ql-icon { font-size: 22px; }
</style>
