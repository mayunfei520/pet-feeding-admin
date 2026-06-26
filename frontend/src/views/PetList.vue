<template>
  <div>
    <h3>🐱 宠物管理</h3>

    <div class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>名字</th><th>主人ID</th><th>种类</th><th>品种</th><th>年龄</th><th>体重</th><th>医疗备注</th><th>创建时间</th><th style="width:80px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in pets" :key="p.id">
            <td>{{ p.id }}</td>
            <td>{{ p.name }}</td>
            <td>{{ p.userId }}</td>
            <td>{{ speciesMap[p.species] || p.species }}</td>
            <td>{{ p.breed || '-' }}</td>
            <td>{{ p.age != null ? p.age + '岁' : '-' }}</td>
            <td>{{ p.weight != null ? p.weight + 'kg' : '-' }}</td>
            <td class="memo">{{ p.medicalNotes || '-' }}</td>
            <td>{{ p.createdAt?.replace('T', ' ') }}</td>
            <td><button class="btn-sm danger" @click="handleDelete(p.id)">删除</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { petApi } from '@/utils/api'

const pets = ref([])
const speciesMap = { CAT: '🐱 猫', DOG: '🐶 狗', OTHER: '🐹 其他' }

onMounted(async () => {
  try {
    const res = await petApi.list()
    pets.value = res.data || []
  } catch (e) { /* */ }
})

async function handleDelete(id) {
  if (!confirm('确定删除该宠物吗？')) return
  try {
    await petApi.remove(id)
    pets.value = pets.value.filter(p => p.id !== id)
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
.memo { max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.btn-sm { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; }
.btn-sm:hover { background: #f0f2f5; }
.btn-sm.danger { color: #ef4444; border-color: #fca5a5; }
</style>
