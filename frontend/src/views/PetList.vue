<template>
  <PageTable
    title="宠物管理"
    desc="管理所有宠物的档案信息"
    :data="pets"
    :columns="columns"
    :loading="loading"
  >
    <template #species="{ item }">
      <span class="species-badge">{{ speciesEmoji[item.species] }}</span>
      <span>{{ speciesLabel[item.species] || item.species }}</span>
    </template>
    <template #actions="{ item }">
      <button class="btn btn-sm btn-danger-outline" @click="handleDelete(item.id)">删除</button>
    </template>
  </PageTable>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { petApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'
import { ElMessage } from 'element-plus'

const pets = ref([])
const loading = ref(false)

const speciesEmoji = { CAT: '🐱', DOG: '🐶', OTHER: '🐹' }
const speciesLabel = { CAT: '猫', DOG: '狗', OTHER: '其他' }

const columns = [
  { key: 'id', label: 'ID', style: 'width:50px' },
  { key: 'name', label: '名字' },
  { key: 'userId', label: '主人ID', style: 'width:70px' },
  { key: 'species', label: '种类' },
  { key: 'breed', label: '品种' },
  { key: 'age', label: '年龄', format: v => v != null ? `${v}岁` : '-' },
  { key: 'weight', label: '体重', format: v => v != null ? `${v}kg` : '-' },
  { key: 'medicalNotes', label: '医疗备注' },
  { key: 'createdAt', label: '创建时间', format: v => v ? v.split('T')[0] : '-' },
]

onMounted(() => fetchPets())

async function fetchPets() {
  loading.value = true
  try {
    const res = await petApi.list()
    pets.value = res.data || []
  } catch (e) { /* */ }
  finally { loading.value = false }
}

async function handleDelete(id) {
  if (!confirm('确定删除该宠物吗？')) return
  try {
    await petApi.remove(id)
    pets.value = pets.value.filter(p => p.id !== id)
    ElMessage.success('删除成功')
  } catch (e) { /* */ }
}
</script>

<style scoped>
.species-badge {
  display: inline-block;
  margin-right: 4px;
  font-size: 15px;
}
</style>
