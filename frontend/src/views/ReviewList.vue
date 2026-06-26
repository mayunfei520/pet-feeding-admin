<template>
  <div>
    <h3>⭐ {{ feederName }} 的评价</h3>

    <div v-if="reviews.length === 0" class="empty">暂无评价</div>

    <div v-for="r in reviews" :key="r.id" class="review-card">
      <div class="top">
        <span class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
        <span class="date">{{ r.createdAt?.slice(0, 10) }}</span>
      </div>
      <div class="body">{{ r.content }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { reviewApi } from '@/utils/api'

const route = useRoute()
const reviews = ref([])
const feederName = ref('')

onMounted(async () => {
  try {
    const res = await reviewApi.list()
    const all = res.data || []
    reviews.value = all.filter(r => r.feederId == route.params.id)
    feederName.value = `喂养员#${route.params.id}`
  } catch (e) { /* */ }
})
</script>

<style scoped>
h3 { margin: 0 0 12px; color: #111827; }
.empty { text-align: center; padding: 40px; color: #9ca3af; }
.review-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 6px; padding: 14px 16px; margin-bottom: 10px; }
.top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.stars { color: #f59e0b; }
.date { color: #9ca3af; font-size: 12px; }
.body { color: #374151; font-size: 14px; line-height: 1.7; }
</style>
