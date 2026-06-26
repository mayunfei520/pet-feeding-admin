<template>
  <div>
    <h3>⭐ 评价管理</h3>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>订单ID</th><th>主人ID</th><th>喂养员ID</th><th>评分</th><th>内容</th><th>时间</th><th style="width:80px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in reviews" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.orderId }}</td>
            <td>{{ r.ownerId }}</td>
            <td>{{ r.feederId }}</td>
            <td>{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</td>
            <td class="memo">{{ r.content || '-' }}</td>
            <td>{{ r.createdAt?.replace('T', ' ') }}</td>
            <td>
              <button class="btn-sm danger" @click="handleDelete(r.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reviewApi } from '@/utils/api'

const reviews = ref([])

onMounted(async () => {
  try {
    const res = await reviewApi.list()
    reviews.value = res.data || []
  } catch (e) { /* */ }
})

async function handleDelete(id) {
  if (!confirm('确定删除该评价吗？')) return
  try {
    await reviewApi.remove(id)
    reviews.value = reviews.value.filter(r => r.id !== id)
  } catch (e) { /* */ }
}
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; font-size: 18px; }
.table-wrap { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; overflow: auto; }
.table { width: 100%; border-collapse: collapse; font-size: 14px; }
.table th, .table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #f3f4f6; white-space: nowrap; }
.table th { background: #f9fafb; color: #6b7280; font-weight: 600; font-size: 13px; }
.table tbody tr:hover { background: #f9fafb; }
.memo { max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
</style>
