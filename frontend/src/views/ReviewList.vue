<template>
  <div class="page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">⭐ {{ feederName }} 的评价</h2>
        <p class="page-desc">查看该喂养员收到的所有用户反馈</p>
      </div>
    </div>

    <div class="reviews-list" v-if="reviews.length > 0">
      <div v-for="r in reviews" :key="r.id" class="review-card animate-fade-in-up">
        <div class="review-header">
          <div class="review-meta">
            <span class="stars">{{ renderStars(r.rating) }}</span>
            <span class="date">{{ r.createdAt?.split('T')[0] }}</span>
          </div>
          <span class="order-ref">订单 #{{ r.orderId }}</span>
        </div>
        <div class="review-content">{{ r.content }}</div>
      </div>
    </div>

    <div v-else class="empty-state">
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
      </svg>
      <p>暂无评价</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { reviewApi } from '@/utils/api'

const route = useRoute()
const reviews = ref([])
const feederName = ref(`喂养员 #${route.params.id}`)

onMounted(async () => {
  try {
    const res = await reviewApi.list()
    const all = res.data || []
    reviews.value = all.filter(r => r.feederId == route.params.id)
    if (reviews.value.length) {
      feederName.value = `喂养员 #${route.params.id} (${reviews.value.length}条评价)`
    }
  } catch (e) { /* */ }
})

function renderStars(n) {
  return '★'.repeat(n) + '☆'.repeat(5 - n)
}
</script>

<style scoped>
.reviews-list { display: flex; flex-direction: column; gap: 12px; }

.review-card {
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  transition: all var(--transition-base);
}
.review-card:hover {
  box-shadow: var(--shadow-md);
  border-color: transparent;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.review-meta { display: flex; align-items: center; gap: 12px; }
.stars { color: #f59e0b; font-size: 14px; letter-spacing: 2px; }
.date { font-size: 12px; color: var(--neutral-400); }
.order-ref { font-size: 12px; color: var(--neutral-400); font-family: 'SF Mono', monospace; }

.review-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--neutral-700);
}

.empty-state {
  text-align: center;
  padding: 60px 24px;
  color: var(--neutral-400);
}
.empty-state svg { color: var(--neutral-200); margin-bottom: 12px; }
.empty-state p { font-size: 14px; }
</style>
