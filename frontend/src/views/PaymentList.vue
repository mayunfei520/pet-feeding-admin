<template>
  <PageTable
    title="支付记录"
    desc="查看平台的支付流水记录"
    :data="payments"
    :columns="columns"
    :loading="loading"
    :show-actions="false"
  >
    <template #payStatus="{ item }">
      <span class="tag" :class="statusClasses[item.payStatus]">
        {{ statusLabels[item.payStatus] || item.payStatus }}
      </span>
    </template>
  </PageTable>
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
  { key: 'id', label: '编号', style: 'width:50px' },
  { key: 'orderId', label: '订单编号', style: 'width:80px' },
  { key: 'userId', label: '用户编号', style: 'width:70px' },
  { key: 'amount', label: '金额', format: v => `¥${v}` },
  { key: 'payMethod', label: '支付方式', format: v => methodLabels[v] || v },
  { key: 'payStatus', label: '状态' },
  { key: 'transactionId', label: '交易号', format: v => v ? truncate(v, 16) : '-' },
  { key: 'createdAt', label: '时间', format: v => v ? v.replace('T', ' ') : '-' },
]

onMounted(() => fetchPayments())

async function fetchPayments() {
  loading.value = true
  try { const res = await paymentApi.list(); payments.value = res.data || [] }
  catch (e) { /* */ }
  finally { loading.value = false }
}

function truncate(str, len) {
  if (!str || str.length <= len) return str || '-'
  return str.slice(0, len) + '...'
}
</script>
