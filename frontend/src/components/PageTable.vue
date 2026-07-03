<template>
  <div class="page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ title }}</h2>
        <p class="page-desc">{{ desc }}</p>
      </div>
      <div class="header-actions">
        <slot name="actions" />
      </div>
    </div>

    <!-- Filters -->
    <div class="filters" v-if="$slots.filters">
      <slot name="filters" />
    </div>

    <!-- Table -->
    <div class="table-card">
      <table class="table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key" :style="col.style">
              {{ col.label }}
            </th>
            <th v-if="showActions" style="width:200px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td :colspan="columnSpan" class="loading-cell">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </td>
          </tr>
          <tr v-else-if="!data.length">
            <td :colspan="columnSpan" class="empty-cell">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <path d="M3 9h18M9 3v18"/>
              </svg>
              <span>暂无数据</span>
            </td>
          </tr>
          <tr v-else v-for="(item, idx) in data" :key="item.id" :class="{ 'row-alt': idx % 2 === 0 }">
            <td v-for="col in columns" :key="col.key" :style="col.style">
              <slot :name="col.slot || col.key" :item="item">{{ renderCell(col, item) }}</slot>
            </td>
            <td v-if="showActions">
              <slot name="actions" :item="item" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  desc: { type: String, default: '' },
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showActions: { type: Boolean, default: true },
})

const columnSpan = computed(() => props.columns.length + (props.showActions ? 1 : 0))

function renderCell(col, item) {
  const val = item[col.key]
  if (col.format) return col.format(val, item)
  if (val === null || val === undefined || val === '') return '-'
  return String(val)
}
</script>

<style scoped>
.page { max-width: 1400px; }

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}
.header-left { min-width: 0; }
.page-title {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--neutral-900);
  letter-spacing: -0.3px;
  margin-bottom: 2px;
}
.page-desc {
  font-size: 13px;
  color: var(--neutral-400);
}
.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

/* Table Card */
.table-card {
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-radius: var(--radius-md);
  overflow: auto;
  box-shadow: var(--shadow-sm);
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.table th, .table td {
  padding: 12px 14px;
  text-align: left;
  border-bottom: 1px solid var(--neutral-100);
  white-space: nowrap;
}
.table thead th {
  background: var(--neutral-50);
  color: var(--neutral-500);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  position: sticky;
  top: 0;
  z-index: 1;
}
.table tbody tr {
  transition: background var(--transition-fast);
}
.table tbody tr:hover {
  background: var(--neutral-50);
}
.table tbody tr.row-alt {
  background: rgba(99, 102, 241, 0.015);
}
.table tbody tr:last-child td {
  border-bottom: none;
}

.loading-cell, .empty-cell {
  text-align: center !important;
  padding: 48px 24px !important;
  color: var(--neutral-400);
  font-size: 14px;
}
.empty-cell svg {
  display: block;
  margin: 0 auto 12px;
  color: var(--neutral-300);
}

/* Loading Spinner */
.loading-cell {
  display: flex !important;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--neutral-200);
  border-top-color: var(--brand-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== Reusable Components ===== */

/* Tags */
.tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}
.tag-active { background: var(--color-success-bg); color: #059669; }
.tag-disabled { background: var(--color-danger-bg); color: #dc2626; }
.tag-pending { background: var(--color-warning-bg); color: #d97706; }
.tag-info { background: var(--color-info-bg); color: #2563eb; }
.tag-purple { background: rgba(139,92,246,0.08); color: #7c3aed; }

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 7px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
  white-space: nowrap;
}
.btn-primary {
  background: var(--brand-primary);
  color: #fff;
  box-shadow: 0 1px 3px rgba(99,102,241,0.2);
}
.btn-primary:hover { background: var(--brand-primary-dark); transform: translateY(-1px); box-shadow: var(--shadow-brand); }
.btn-outline {
  background: #fff;
  color: var(--neutral-600);
  border-color: var(--neutral-200);
}
.btn-outline:hover { background: var(--neutral-50); color: var(--neutral-800); }
.btn-danger-outline {
  background: #fff;
  color: var(--color-danger);
  border-color: #fecaca;
}
.btn-danger-outline:hover { background: var(--color-danger-bg); }
.btn-sm { padding: 4px 10px; font-size: 12px; border-radius: 6px; }

/* Select / Input */
.select, .input {
  padding: 7px 12px;
  border: 1px solid var(--neutral-200);
  border-radius: 8px;
  font-size: 13px;
  color: var(--neutral-700);
  background: #fff;
  outline: none;
  transition: all var(--transition-fast);
}
.select:focus, .input:focus {
  border-color: var(--brand-primary);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
}

/* Badge for stars */
.stars { color: #f59e0b; letter-spacing: 1px; }
</style>
