<template>
  <PageTable
    title="订单管理"
    desc="管理所有上门喂养订单"
    :data="filteredOrders"
    :columns="columns"
    :loading="loading"
  >
    <template #filters>
      <select class="select select-filter" v-model="filterStatus">
        <option value="">全部状态</option>
        <option v-for="(label, key) in statusLabels" :key="key" :value="key">{{ label }}</option>
      </select>
    </template>

    <template #orderNo="{ item }">
      <span class="mono">{{ item.orderNo }}</span>
    </template>
    <template #serviceDate="{ item }">
      <span>{{ item.serviceDate }}</span>
    </template>
    <template #servicePeriod="{ item }">
      {{ periodLabels[item.servicePeriod] }}
    </template>
    <template #price="{ item }">
      <span class="price">¥{{ item.price }}</span>
    </template>
    <template #status="{ item }">
      <span class="tag" :class="statusTagClass[item.status]">{{ statusLabels[item.status] }}</span>
    </template>
    <template #row-actions="{ item }">
      <button v-if="item.status === 'PENDING'" class="btn btn-sm btn-primary" @click="showAssign(item)">分配</button>
      <button v-if="item.status === 'PENDING'" class="btn btn-sm btn-danger-outline" @click="handleCancel(item.id)" style="margin-left:4px">取消</button>
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item)" style="margin-left:4px">删除</button>
    </template>
  </PageTable>

  <!-- Assign Modal -->
  <div class="modal-overlay" v-if="assignShow" @click.self="assignShow = false">
    <div class="modal animate-fade-in-up">
      <div class="modal-header">
        <span class="modal-title">📨 分配喂养员</span>
        <button class="modal-close" @click="assignShow = false">✕</button>
      </div>
      <div class="modal-body">
        <div class="info-grid">
          <div class="info-item"><span class="info-label">订单编号</span><span class="info-val mono">{{ assignOrder?.orderNo }}</span></div>
          <div class="info-item"><span class="info-label">服务日期</span><span class="info-val">{{ assignOrder?.serviceDate }} {{ periodLabels[assignOrder?.servicePeriod] }}</span></div>
          <div class="info-item"><span class="info-label">服务地址</span><span class="info-val">{{ assignOrder?.address }}</span></div>
        </div>
        <div class="form-group" style="margin-top:16px">
          <label>选择喂养员 <span class="req">*</span></label>
          <select v-model="selectedFeederId" class="input" style="height:38px;width:100%">
            <option value="">请选择喂养员</option>
            <option v-for="f in feeders" :key="f.id" :value="f.id">
              {{ f.realName }} - {{ f.serviceArea }} ⭐{{ f.rating || '5.0' }}
            </option>
          </select>
        </div>
        <div v-if="selectedFeeder" class="sms-box">
          <div class="sms-label">📱 短信预览</div>
          <div class="sms-content">
            【宠物喂养平台】您好<strong>{{ selectedFeeder.realName }}</strong>，您有一条新的上门喂养订单：服务日期<strong>{{ assignOrder?.serviceDate }}</strong>，时段<strong>{{ periodLabels[assignOrder?.servicePeriod] }}</strong>，地址<strong>{{ assignOrder?.address }}</strong>。请登录平台查看详情。
          </div>
        </div>
        <div class="error" v-if="error">{{ error }}</div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="assignShow = false">取消</button>
        <button class="btn btn-primary" @click="handleAssign" :disabled="assigning">
          {{ assigning ? '分配中...' : '确认分配' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { orderApi, feederApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'

const orders = ref([])
const feeders = ref([])
const filterStatus = ref('')
const loading = ref(false)
const assignShow = ref(false)
const assignOrder = ref(null)
const selectedFeederId = ref('')
const assigning = ref(false)
const error = ref('')

const periodLabels = { AM: '上午', PM: '下午', EVENING: '晚上', MORNING: '上午', AFTERNOON: '下午' }
const statusLabels = { PENDING: '待接单', ACCEPTED: '已接单', IN_PROGRESS: '进行中', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusTagClass = {
  PENDING: 'tag-pending',
  ACCEPTED: 'tag-info',
  IN_PROGRESS: 'tag-purple',
  CONFIRMED: 'tag-active',
  COMPLETED: 'tag-active',
  CANCELLED: 'tag-disabled'
}

const columns = [
  { key: 'orderNo', label: '订单编号' },
  { key: 'ownerId', label: '主人编号', style: 'width:70px' },
  { key: 'feederId', label: '喂养员编号', format: v => v || '-' },
  { key: 'petId', label: '宠物编号', style: 'width:70px' },
  { key: 'serviceDate', label: '服务日期' },
  { key: 'servicePeriod', label: '时段' },
  { key: 'address', label: '地址' },
  { key: 'price', label: '金额' },
  { key: 'status', label: '状态' },
  { key: 'createdAt', label: '创建时间', format: v => v ? v.replace('T', ' ') : '-' },
]

const filteredOrders = computed(() => {
  if (!filterStatus.value) return orders.value
  return orders.value.filter(o => o.status === filterStatus.value)
})

const selectedFeeder = computed(() => {
  return feeders.value.find(f => f.id == selectedFeederId.value) || null
})

onMounted(() => { fetchOrders(); fetchFeeders() })

async function fetchOrders() {
  loading.value = true
  try { const res = await orderApi.list(); orders.value = res.data || [] }
  catch (e) { /* */ }
  finally { loading.value = false }
}

async function fetchFeeders() {
  try { const res = await feederApi.list(); feeders.value = res.data || [] }
  catch (e) { /* */ }
}

async function handleCancel(id) {
  if (!confirm('确定取消该订单吗？')) return
  try { await orderApi.cancel(id); await fetchOrders(); ElMessage.success('订单已取消') }
  catch (e) { /* */ }
}

async function handleDelete(order) {
  if (!confirm(`确定删除订单「${order.orderNo}」吗？`)) return
  try { await orderApi.remove(order.id); await fetchOrders(); ElMessage.success('删除成功') }
  catch (e) { /* */ }
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
    ElMessage.success('分配成功！短信已发送')
    await fetchOrders()
  } catch (e) { error.value = e?.message || '分配失败' }
  finally { assigning.value = false }
}
</script>

<style scoped>
.mono { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; color: var(--brand-primary); font-weight: 500; }
.price { font-weight: 600; color: var(--neutral-800); }

/* Select filter — uses global .select styles */
.select-filter { width: 130px; }

/* Info Grid */
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 16px; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 11px; color: var(--neutral-400); text-transform: uppercase; letter-spacing: 0.5px; }
.info-val { font-size: 14px; color: var(--neutral-800); font-weight: 500; }

/* SMS Box */
.sms-box { margin-top: 16px; background: var(--brand-gradient-subtle); border: 1px solid rgba(99,102,241,0.15); border-radius: var(--radius-sm); padding: 12px; }
.sms-label { font-size: 12px; color: var(--brand-primary); font-weight: 500; margin-bottom: 6px; }
.sms-content { font-size: 13px; line-height: 1.7; color: var(--neutral-700); }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 200;
  backdrop-filter: blur(4px);
}
.modal {
  width: 540px; background: #fff; border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl); overflow: hidden;
}
.modal-header {
  padding: 18px 24px; border-bottom: 1px solid var(--neutral-100);
  display: flex; align-items: center; justify-content: space-between;
}
.modal-title { font-size: 16px; font-weight: 600; color: var(--neutral-900); }
.modal-close { border: none; background: none; font-size: 18px; color: var(--neutral-400); cursor: pointer; }
.modal-close:hover { color: var(--neutral-700); }
.modal-body { padding: 20px 24px; max-height: 70vh; overflow-y: auto; }
.modal-footer {
  padding: 14px 24px; border-top: 1px solid var(--neutral-100);
  display: flex; justify-content: flex-end; gap: 8px;
}
.form-group label { display: block; margin-bottom: 5px; font-size: 13px; color: var(--neutral-600); font-weight: 500; }
.req { color: var(--color-danger); }
.error { color: var(--color-danger); font-size: 13px; margin-top: 12px; padding: 8px 12px; background: var(--color-danger-bg); border-radius: 6px; }
</style>
