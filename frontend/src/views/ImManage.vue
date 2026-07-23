<template>
  <PageTable
    title="会话管理"
    desc="查看客户与喂养员之间的聊天记录（只读，仅供纠纷取证与客服介入）"
    :data="tableData"
    :columns="tableColumns"
    :loading="loading"
    :empty-text="emptyText"
  >
    <template #header-actions>
      <div class="search-box">
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索客户 / 喂养员 / 订单号"
          @keyup.enter="fetchData"
        />
        <button class="btn btn-sm btn-outline" @click="fetchData">搜索</button>
      </div>
      <button class="btn btn-sm btn-outline refresh-btn" @click="fetchData" :disabled="loading">
        <span class="refresh-icon" :class="{ spinning: loading }">↻</span> 刷新
      </button>
    </template>

    <template #unread="{ item }">
      <span v-if="item.ownerUnread || item.feederUnread" class="unread-badge">
        客 {{ item.ownerUnread || 0 }} · 喂 {{ item.feederUnread || 0 }}
      </span>
      <span v-else class="muted">-</span>
    </template>

    <template #row-actions="{ item }">
      <button class="btn btn-sm btn-outline" @click="openDrawer(item)">查看消息</button>
    </template>
  </PageTable>

  <el-drawer
    v-model="drawerVisible"
    :title="drawerTitle"
    direction="rtl"
    size="460px"
    class="im-drawer"
  >
    <div class="im-body" v-if="conv">
      <div class="im-meta">
        <div class="im-meta-row"><span class="im-k">订单号</span><span class="im-v mono">{{ conv.orderNo || conv.orderId }}</span></div>
        <div class="im-meta-row"><span class="im-k">客户</span><span class="im-v">{{ conv.ownerName }}<span v-if="conv.ownerUnread" class="dot-owner">未读{{ conv.ownerUnread }}</span></span></div>
        <div class="im-meta-row"><span class="im-k">喂养员</span><span class="im-v">{{ conv.feederName }}<span v-if="conv.feederUnread" class="dot-feeder">未读{{ conv.feederUnread }}</span></span></div>
      </div>

      <div class="im-load-earlier" v-if="msgNextCursor">
        <button class="btn btn-xs btn-ghost" @click="loadEarlier" :disabled="msgLoading">加载更早的消息</button>
      </div>

      <div class="im-chat" v-loading="msgLoading">
        <div
          v-for="m in messages"
          :key="m.id"
          class="im-msg"
          :class="side(m.senderRole)"
        >
          <div class="im-msg-head">
            <span class="im-sender">{{ m.senderName }}</span>
            <span class="im-time">{{ fmtTime(m.createTime) }}</span>
          </div>
          <div class="im-bubble">{{ m.content }}</div>
        </div>
        <div v-if="!messages.length && !msgLoading" class="im-empty">暂无消息</div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { adminImApi } from '@/utils/api'
import PageTable from '@/components/PageTable.vue'

const loading = ref(false)
const keyword = ref('')
const tableData = ref([])
let pollTimer = null

const tableColumns = [
  { key: 'id', label: '编号', style: 'width:70px' },
  { key: 'orderNo', label: '订单号' },
  { key: 'ownerName', label: '客户' },
  { key: 'feederName', label: '喂养员' },
  { key: 'lastMessage', label: '末条消息' },
  { key: 'unread', label: '未读', slot: 'unread' },
  { key: 'lastMessageTime', label: '最后活跃', format: (v) => fmtTime(v) }
]

const emptyText = computed(() =>
  keyword.value ? '未找到匹配的会话' : '暂无聊天会话'
)

onMounted(() => {
  fetchData()
  // 轻轮询：静默刷新会话列表，新会话/新消息无需手动刷新（#8）
  pollTimer = setInterval(() => { if (!loading.value) fetchData() }, 20000)
})
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

async function fetchData() {
  loading.value = true
  try {
    const res = await adminImApi.list({
      page: 1,
      size: 100,
      keyword: keyword.value || undefined
    })
    tableData.value = res.data || []
  } catch (e) { /* 错误由拦截器提示 */ }
  finally { loading.value = false }
}

/* ===== 抽屉：消息记录 ===== */
const drawerVisible = ref(false)
const conv = ref(null)
const messages = ref([])
const msgLoading = ref(false)
const msgNextCursor = ref(null)

const drawerTitle = computed(() =>
  conv.value ? `会话 #${conv.value.id}（${conv.value.ownerName} ↔ ${conv.value.feederName}）` : '会话详情'
)

async function openDrawer(item) {
  conv.value = item
  messages.value = []
  msgNextCursor.value = null
  drawerVisible.value = true
  await loadMessages(null)
}

async function loadMessages(cursor) {
  if (!conv.value) return
  msgLoading.value = true
  try {
    const res = await adminImApi.messages(
      conv.value.id,
      cursor == null ? {} : { cursor, size: 50 }
    )
    const page = res.data || { list: [], nextCursor: null }
    const list = page.list || []
    messages.value = cursor == null ? list : list.concat(messages.value)
    msgNextCursor.value = page.nextCursor || null
  } catch (e) { /* */ }
  finally { msgLoading.value = false }
}

function loadEarlier() {
  if (msgNextCursor.value) loadMessages(msgNextCursor.value)
}

/* 客户(OWNER)居左，喂养员(FEEDER)居右 */
function side(role) {
  return role === 'FEEDER' ? 'right' : 'left'
}

function fmtTime(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
/* 搜索框 */
.search-box {
  display: flex;
  gap: 6px;
}
.search-input {
  width: 220px;
  padding: 7px 12px;
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  background: rgba(12, 20, 36, 0.6);
  color: var(--neutral-700);
  font-size: 13px;
  outline: none;
  transition: border-color var(--transition-fast);
}
.search-input:focus {
  border-color: var(--ice-bright);
}
.search-input::placeholder {
  color: var(--neutral-400);
}

.unread-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(248, 113, 113, 0.14);
  color: var(--color-danger);
  font-size: 11px;
  font-weight: 600;
}
.muted { color: var(--neutral-400); }

/* 刷新按钮 */
.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.refresh-icon {
  display: inline-block;
  font-size: 15px;
  line-height: 1;
  transition: transform 0.3s ease;
}
.refresh-icon.spinning { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 抽屉 ===== */
.im-meta {
  background: rgba(12, 20, 36, 0.5);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-md);
  padding: 12px 14px;
  margin-bottom: 14px;
}
.im-meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 0;
  font-size: 13px;
}
.im-k {
  width: 52px;
  color: var(--neutral-400);
  flex-shrink: 0;
}
.im-v {
  color: var(--neutral-700);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  color: var(--neutral-600);
}
.dot-owner, .dot-feeder {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}
.dot-owner { background: rgba(56, 189, 248, 0.16); color: var(--ice-bright); }
.dot-feeder { background: rgba(167, 139, 250, 0.16); color: var(--violet); }

.im-load-earlier {
  text-align: center;
  margin-bottom: 10px;
}
.btn-xs {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: var(--radius-sm);
}
.btn-ghost {
  background: transparent;
  border: 1px solid var(--border-soft);
  color: var(--neutral-500);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.btn-ghost:hover { color: var(--ice-bright); border-color: var(--ice-bright); }

.im-chat {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
}
.im-msg { display: flex; flex-direction: column; max-width: 82%; }
.im-msg.left { align-self: flex-start; align-items: flex-start; }
.im-msg.right { align-self: flex-end; align-items: flex-end; }
.im-msg-head {
  display: flex;
  gap: 8px;
  align-items: baseline;
  margin-bottom: 4px;
  font-size: 11px;
}
.im-sender { color: var(--neutral-500); font-weight: 600; }
.im-time { color: var(--neutral-400); }
.im-bubble {
  padding: 9px 13px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--neutral-800);
}
.im-msg.left .im-bubble {
  background: var(--surface-card);
  border: 1px solid var(--neutral-200);
  border-top-left-radius: 3px;
}
.im-msg.right .im-bubble {
  background: var(--brand-gradient);
  color: #061018;
  border-top-right-radius: 3px;
}
.im-empty {
  text-align: center;
  color: var(--neutral-400);
  font-size: 13px;
  padding: 40px 0;
}
</style>
