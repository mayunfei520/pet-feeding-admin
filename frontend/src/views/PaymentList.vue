<template>
  <PageTable
    title="支付记录"
    desc="查看平台的支付流水记录"
    :data="payments"
    :columns="columns"
    :loading="loading"
    :show-actions="false"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { paymentApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'

const payments = ref([])
const loading = ref(false)

const methodLabels = { WECHAT: '微信', ALIPAY: '支付宝' }
const statusLabels = { UNPAID: '未支付', PAID: '已支付', REFUNDED: '已退款' }
const statusClasses = { UNPAID: 'tag-pending', PAID: 'tag-active', REFUNDED: 'tag-disabled' }

const columns = [
  { key: 'id', label: 'ID', style: 'width:50px' },
  { key: 'orderId', label: '订单ID', style: 'width:80px' },
  { key: 'userId', label: '用户ID', style: 'width:70px' },
  { key: 'amount', label: '金额', format: v => `¥${v}` },
  { key: 'payMethod', label: '支付方式', format: v => methodLabels[v] || v },
  { key: 'payStatus', label: '状态', format: v => `<span class="tag ${statusClasses[v]}">${statusLabels[v]}</span>` },
  { key: 'transactionId', label: '交易号', format: v => v ? v.slice(0, 16) + (v.length > 16 ? '...' : '') : '-' },
  { key: 'createdAt', label: '时间', format: v => v ? v.replace('T', ' ') : '-' },
]

onMounted(() => fetchPayments())

async function fetchPayments() {
  loading.value = true
  try { const res = await paymentApi.list(); payments.value = res.data || [] }
  catch (e) { /* */ }
  finally { loading.value = false }
}
</script>
