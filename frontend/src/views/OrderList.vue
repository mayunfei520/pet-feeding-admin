<template>
  <div>
    <h3>📋 订单管理</h3>

    <div class="filter-bar">
      <select v-model="filterStatus" @change="applyFilter">
        <option value="">全部状态</option>
        <option value="PENDING">待接单</option>
        <option value="ACCEPTED">已接单</option>
        <option value="COMPLETED">已完成</option>
        <option value="CANCELLED">已取消</option>
      </select>
    </div>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>订单编号</th><th>主人ID</th><th>喂养员ID</th><th>宠物ID</th><th>服务日期</th><th>时段</th><th>地址</th><th>金额</th><th>状态</th><th>创建时间</th><th style="width:120px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in filteredOrders" :key="o.id">
            <td>{{ o.orderNo }}</td>
            <td>{{ o.ownerId }}</td>
            <td>{{ o.feederId || '-' }}</td>
            <td>{{ o.petId }}</td>
            <td>{{ o.serviceDate }}</td>
            <td>{{ periodMap[o.servicePeriod] }}</td>
            <td class="memo">{{ o.address }}</td>
            <td>¥{{ o.price }}</td>
            <td><span class="tag" :class="o.status">{{ statusMap[o.status] }}</span></td>
            <td>{{ o.createdAt?.replace('T', ' ') }}</td>
            <td>
              <button v-if="o.status === 'PENDING'" class="btn-sm primary" @click="showAssign(o)">分配</button>
              <button v-if="o.status === 'PENDING'" class="btn-sm danger" @click="handleCancel(o.id)">取消</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分配弹窗 -->
    <div class="modal" v-if="assignShow" @click.self="assignShow = false">
      <div class="modal-box">
        <h4>📨 分配喂养员并发送短信</h4>
        <div class="order-info">
          <p>订单编号：<strong>{{ assignOrder?.orderNo }}</strong></p>
          <p>服务日期：<strong>{{ assignOrder?.serviceDate }}</strong> {{ periodMap[assignOrder?.servicePeriod] }}</p>
          <p>服务地址：<strong>{{ assignOrder?.address }}</strong></p>
        </div>

        <div class="form-item">
          <label>选择喂养员 <span class="required">*</span></label>
          <select v-model="selectedFeederId">
            <option value="">请选择喂养员</option>
            <option v-for="f in feeders" :key="f.id" :value="f.id">
              {{ f.realName }} - {{ f.serviceArea }} ⭐{{ f.rating || '5.0' }}
            </option>
          </select>
        </div>

        <div class="sms-preview" v-if="selectedFeeder">
          <label>📱 短信预览</label>
          <div class="sms-content">
            【宠物喂养平台】您好<strong>{{ selectedFeeder.realName }}</strong>，
            您有一条新的上门喂养订单：
            服务日期<strong>{{ assignOrder?.serviceDate }}</strong>，
            时段<strong>{{ periodMap[assignOrder?.servicePeriod] }}</strong>，
            地址<strong>{{ assignOrder?.address }}</strong>。
            请登录平台查看详情并联系客户确认。
          </div>
        </div>

        <div class="error" v-if="error">{{ error }}</div>
        <div class="modal-btns">
          <button class="btn" @click="assignShow = false">取消</button>
          <button class="btn primary" @click="handleAssign" :disabled="assigning">
            {{ assigning ? '分配中...' : '确认分配并发送短信' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi, feederApi } from '@/utils/api'

const orders = ref([])
const feeders = ref([])
const filterStatus = ref('')
const assignShow = ref(false)
const assignOrder = ref(null)
const selectedFeederId = ref('')
const assigning = ref(false)
const error = ref('')

const periodMap = { AM: '上午', PM: '下午', EVENING: '晚上' }
const statusMap = { PENDING: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', COMPLETED: '已完成', CANCELLED: '已取消' }

const filteredOrders = computed(() => {
  if (!filterStatus.value) return orders.value
  return orders.value.filter(o => o.status === filterStatus.value)
})

const selectedFeeder = computed(() => {
  return feeders.value.find(f => f.id == selectedFeederId.value) || null
})

onMounted(() => { fetchOrders(); fetchFeeders() })

function applyFilter() { /* computed 自动响应 */ }

async function fetchOrders() {
  try { const res = await orderApi.list(); orders.value = res.data || [] } catch (e) { /* */ }
}

async function fetchFeeders() {
  try { const res = await feederApi.list(); feeders.value = res.data || [] } catch (e) { /* */ }
}

async function handleCancel(id) {
  if (!confirm('确定取消该订单吗？')) return
  try { await orderApi.cancel(id); fetchOrders() } catch (e) { /* */ }
}

function showAssign(order) {
  assignOrder.value = order
  selectedFeederId.value = ''
  error.value = ''
  assignShow.value = true
}

async function handleAssign() {
  error.value = ''
  if (!selectedFeederId.value) { error.value = '请选择喂养员'; return }
  assigning.value = true
  try {
    await orderApi.assign(assignOrder.value.id, selectedFeederId.value)
    assignShow.value = false
    alert('分配成功！短信已发送给喂养员（模拟模式，查看后端日志确认）')
    fetchOrders()
  } catch (e) { error.value = e.message || '分配失败' }
  finally { assigning.value = false }
}
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; font-size: 18px; }
.filter-bar { margin-bottom: 12px; }
.filter-bar select { padding: 7px 12px; border: 1px solid #d1d5db; border-radius: 5px; font-size: 14px; }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.memo { max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tag { padding: 2px 10px; border-radius: 3px; font-size: 13px; white-space: nowrap; }
.tag.PENDING { background: #e0e7ff; color: #3730a3; }
.tag.ACCEPTED { background: #fef3c7; color: #92400e; }
.tag.COMPLETED { background: #d1fae5; color: #065f46; }
.tag.CANCELLED { background: #fee2e2; color: #991b1b; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; margin-right: 4px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
.btn-sm.primary { color: #3b82f6; border-color: #93c5fd; }
.btn-sm.primary:hover { background: #eff6ff; }

/* 弹窗 */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-box { background: #fff; border-radius: 8px; padding: 24px; width: 520px; max-height: 80vh; overflow-y: auto; }
.modal-box h4 { margin: 0 0 16px; color: #111827; font-size: 17px; }
.order-info { background: #f9fafb; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-size: 14px; line-height: 1.8; }
.form-item { margin-bottom: 14px; }
.form-item label { display: block; margin-bottom: 4px; font-size: 14px; color: #374151; }
.required { color: #ef4444; }
.form-item select {
  width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 5px; font-size: 14px; box-sizing: border-box;
}
.sms-preview { margin-bottom: 16px; }
.sms-preview label { display: block; font-size: 14px; color: #374151; margin-bottom: 6px; }
.sms-content { background: #eff6ff; padding: 12px; border-radius: 5px; font-size: 14px; line-height: 1.8; color: #1e3a5f; }
.error { color: #ef4444; font-size: 14px; margin-bottom: 10px; }
.modal-btns { display: flex; justify-content: flex-end; gap: 8px; }
.btn { padding: 8px 18px; border: 1px solid #d1d5db; border-radius: 5px; background: #fff; cursor: pointer; font-size: 14px; }
.btn.primary { background: #3b82f6; color: #fff; border-color: #3b82f6; }
.btn.primary:hover { background: #2563eb; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
