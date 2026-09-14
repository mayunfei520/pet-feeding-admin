<template>
  <PageTable
    title="评价管理"
    desc="查看所有用户的喂养服务评价"
    :data="reviews"
    :columns="columns"
    :loading="loading"
  >
    <template #rating="{ item }">
      <span class="stars">{{ '★'.repeat(item.rating) }}{{ '☆'.repeat(5 - item.rating) }}</span>
    </template>
    <template #content="{ item }">
      <span class="truncate">{{ item.content || '-' }}</span>
    </template>
    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item.id)">删除</button>
    </template>
  </PageTable>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { reviewApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'
import { confirmDanger } from '../utils/confirm'

const reviews = ref([])
const loading = ref(false)

const columns = [
  { key: 'id', label: '编号', style: 'width:3.57rem' },
  { key: 'orderId', label: '订单编号', style: 'width:5.71rem' },
  { key: 'ownerId', label: '主人编号', style: 'width:5rem' },
  { key: 'feederId', label: '喂养员编号', style: 'width:5.71rem' },
  { key: 'rating', label: '评分' },
  { key: 'content', label: '内容' },
  { key: 'createdAt', label: '时间', format: v => v ? v.replace('T', ' ') : '-' },
]

onMounted(() => fetchReviews())

async function fetchReviews() {
  loading.value = true
  try { const res = await reviewApi.list(); reviews.value = res.data || [] }
  catch (e) { /* */ }
  finally { loading.value = false }
}

async function handleDelete(id) {
  if (!(await confirmDanger('确定删除该评价吗？'))) return
  try {
    await reviewApi.remove(id)
    reviews.value = reviews.value.filter(r => r.id !== id)
    ElMessage.success('删除成功')
  } catch (e) { /* */ }
}
</script>

<style scoped>
.truncate { max-width: 14.29rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
