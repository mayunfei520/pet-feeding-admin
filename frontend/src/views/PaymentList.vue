<template>
  <div>
    <h3>💰 支付记录</h3>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr><th>ID</th><th>订单ID</th><th>用户ID</th><th>金额</th><th>支付方式</th><th>状态</th><th>交易号</th><th>时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in payments" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.orderId }}</td>
            <td>{{ p.userId }}</td>
            <td>¥{{ p.amount }}</td>
            <td>{{ methodMap[p.payMethod] || p.payMethod }}</td>
            <td><span class="tag" :class="p.payStatus">{{ statusMap[p.payStatus] }}</span></td>
            <td class="memo">{{ p.transactionId || '-' }}</td>
            <td>{{ p.createdAt?.replace('T', ' ') }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { paymentApi } from '@/utils/api'

const payments = ref([])
const methodMap = { WECHAT: '微信', ALIPAY: '支付宝' }
const statusMap = { UNPAID: '未支付', PAID: '已支付', REFUNDED: '已退款' }

onMounted(async () => {
  try {
    const res = await paymentApi.list()
    payments.value = res.data || []
  } catch (e) { /* */ }
})
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; font-size: 18px; }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.memo { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tag { padding: 2px 10px; border-radius: 3px; font-size: 13px; }
.tag.UNPAID { background: #fef3c7; color: #92400e; }
.tag.PAID { background: #d1fae5; color: #065f46; }
.tag.REFUNDED { background: #e5e7eb; color: #6b7280; }
</style>
