<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useStomp } from '../composables/useStomp.js'

const token = localStorage.getItem('token')

// 从 JWT token 解析 userId，比 localStorage user 对象更可靠
function parseUid() {
  try {
    const u = JSON.parse(localStorage.getItem('user') || '{}')
    return u.id != null ? Number(u.id) : null
  } catch(e) { return null }
}
// 动态获取，每次使用时重新读取（防止 token 切换后 uid 过期）
function getUid() {
  return parseUid()
}
function getPeer(c) {
  const uid = getUid()
  if (uid == null) return null
  // 使用 == 而非 === 以兼容后端返回字符串/数字类型不一致
  return c.senderId == uid ? c.receiverId : c.senderId
}

const h = () => ({ 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' })

const { connected, connect, onMessage, sendMessage } = useStomp()

const conversations = ref([])
const messages = ref([])
const input = ref('')
const activePeer = ref(null)
const activePeerName = ref('')
const loading = ref(false)
const sending = ref(false)
const chatEl = ref(null)
let unsubMessage = null

// 用户搜索
const searchMode = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const searching = ref(false)

function scroll() { nextTick(() => { const el = chatEl.value; if (el) el.scrollTop = el.scrollHeight }) }

async function loadConversations() {
  try {
    const res = await fetch('/api/messages/conversations', { headers: h() })
    const d = await res.json()
    if (d.code === 0) conversations.value = d.data || []
  } catch(e) { console.error(e) }
}

async function openChat(peerId, peerName) {
  searchMode.value = false
  activePeer.value = peerId
  activePeerName.value = peerName
  loading.value = true
  try {
    const res = await fetch('/api/messages/' + peerId + '?current=1&pageSize=50', { headers: h() })
    const d = await res.json()
    if (d.code === 0) {
      messages.value = (d.data.records || [])
      // 无论当前页是否有未读，都调用批量已读 API 覆盖该会话全部消息（包括分页外的旧消息）
      fetch('/api/messages/' + peerId + '/read-all', { method: 'PUT', headers: h() })
        .catch(() => {})
        .finally(() => window.dispatchEvent(new Event('unread-changed')))
    }
  } catch(e) { console.error(e) }
  finally { loading.value = false }
  scroll()
}

async function sendMsg() {
  const text = input.value.trim()
  if (!text || !activePeer.value || sending.value) return
  sending.value = true

  if (connected.value) {
    sendMessage({ receiverId: activePeer.value, content: text })
    // 乐观更新：立即在本地显示自己发出的消息
    const myInfo = JSON.parse(localStorage.getItem('user') || '{}')
    messages.value.push({
      senderId: getUid(),
      receiverId: activePeer.value,
      content: text,
      createdAt: new Date().toISOString(),
      senderName: myInfo.nickname || '我',
      isRead: 1
    })
    input.value = ''
    sending.value = false
    scroll()
  } else {
    try {
      const res = await fetch('/api/messages', {
        method: 'POST', headers: h(),
        body: JSON.stringify({ receiverId: activePeer.value, content: text })
      })
      const d = await res.json()
      if (d.code === 0) {
        messages.value.push({ ...d.data, senderId: getUid() })
        input.value = ''
        scroll()
        loadConversations()
      }
    } catch(e) { console.error(e) }
    finally { sending.value = false }
  }
}

async function doSearch() {
  const q = searchQuery.value.trim()
  if (!q) { searchResults.value = []; return }
  searching.value = true
  try {
    const res = await fetch('/api/user/search?q=' + encodeURIComponent(q), { headers: h() })
    const d = await res.json()
    if (d.code === 0) searchResults.value = d.data || []
    else searchResults.value = []
  } catch(e) { console.error(e) }
  finally { searching.value = false }
}

function startNewChat() {
  searchMode.value = true
  searchQuery.value = ''
  searchResults.value = []
}

function selectUser(user) {
  openChat(user.id, user.nickname || user.email)
  loadConversations()
}

async function deleteConversation(peerId) {
  if (!confirm('确定删除该私聊记录？')) return
  // 乐观删除：立即从本地列表移除
  conversations.value = conversations.value.filter(c => {
    const p = getPeer(c)
    return p !== peerId
  })
  if (activePeer.value === peerId) {
    activePeer.value = null
    activePeerName.value = ''
    messages.value = []
  }
  window.dispatchEvent(new Event('unread-changed'))
  // 后台异步删除
  try {
    await fetch('/api/messages/conversations/' + peerId, { method: 'DELETE', headers: h() })
    loadConversations()
  } catch(e) { /* 网络错误不影响本地已移除 */ }
}

async function markAllRead() {
  try {
    const res = await fetch('/api/messages/read-all', { method: 'PUT', headers: h() })
    const d = await res.json()
    console.log('全部已读 API 返回:', d)
    // 同时清除通知未读
    await fetch('/api/notifications/read-all', { method: 'PUT', headers: h() }).catch(() => {})
  } catch(e) { console.error(e) }
  // 强制刷新角标
  for (let i = 0; i < 3; i++) {
    setTimeout(() => window.dispatchEvent(new Event('unread-changed')), i * 300)
  }
}

function timeStr(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await loadConversations()
  // 先注册回调，再建立连接——防止 STOMP 订阅建立后回调注册前的消息丢失
  unsubMessage = onMessage((msg) => {
    loadConversations()
    // 收到自己发的消息回显：替换之前乐观更新的临时消息
    if (msg.senderId == getUid()) {
      if (activePeer.value && msg.receiverId === activePeer.value) {
        const idx = messages.value.findIndex(m => !m.id && m.content === msg.content && m.receiverId === msg.receiverId)
        if (idx >= 0) {
          messages.value[idx] = msg
        } else if (!messages.value.some(m => m.id === msg.id)) {
          messages.value.push(msg)
        }
        scroll()
      }
      return
    }
    const peerId = msg.senderId
    if (activePeer.value && peerId === activePeer.value) {
      // 避免重复
      if (!messages.value.some(m => m.id === msg.id)) {
        messages.value.push(msg)
        scroll()
      }
      fetch('/api/messages/' + msg.id + '/read', { method: 'PUT', headers: h() }).catch(() => {}).finally(() => {
        window.dispatchEvent(new Event('unread-changed'))
      })
    }
  })
  await connect()
})

onUnmounted(() => {
  if (unsubMessage) unsubMessage()
})
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 h-[calc(100vh-64px)] flex">
    <!-- 会话列表 -->
    <div class="w-72 border-r border-gray-100 flex flex-col flex-shrink-0">
      <div class="py-4 px-3 border-b border-gray-100 flex items-center justify-between">
        <h1 class="text-lg font-bold text-gray-900">私信</h1>
        <div class="flex items-center gap-2">
          <button @click="markAllRead" class="text-[10px] text-gray-400 hover:text-blue-600 transition-colors" title="全部已读">全部已读</button>
          <span class="w-2 h-2 rounded-full" :class="connected ? 'bg-green-500' : 'bg-gray-300'" title="连接状态"></span>
        </div>
      </div>

      <!-- 新对话按钮 -->
      <div class="px-3 py-2">
        <button @click="startNewChat"
          class="w-full py-2 text-xs font-medium text-blue-600 border border-blue-200 rounded-lg hover:bg-blue-50 transition-colors">
          + 新对话
        </button>
      </div>

      <div class="flex-1 overflow-y-auto">
        <!-- 搜索面板 -->
        <div v-if="searchMode" class="px-3 pb-3">
          <div class="flex gap-2 mb-2">
            <input v-model="searchQuery" @keyup.enter="doSearch" @input="doSearch"
              placeholder="搜索用户昵称..."
              class="flex-1 px-3 py-1.5 border border-gray-200 rounded-lg text-xs focus:outline-none focus:ring-2 focus:ring-blue-500" />
            <button @click="searchMode = false" class="text-xs text-gray-400 hover:text-gray-600">取消</button>
          </div>
          <div v-if="searching" class="text-center py-4 text-xs text-gray-400">搜索中...</div>
          <div v-else-if="searchResults.length === 0 && searchQuery" class="text-center py-4 text-xs text-gray-400">无结果</div>
          <div v-for="u in searchResults" :key="u.id"
            @click="selectUser(u)"
            class="flex items-center gap-2 px-2 py-2 rounded-lg cursor-pointer hover:bg-gray-50 border-b border-gray-50">
            <div class="w-7 h-7 bg-blue-100 rounded-full flex items-center justify-center">
              <span class="text-blue-600 text-xs font-bold">{{ (u.nickname || u.email)[0] }}</span>
            </div>
            <div>
              <p class="text-xs font-medium text-gray-800">{{ u.nickname }}</p>
              <p class="text-[10px] text-gray-400">{{ u.email }}</p>
            </div>
          </div>
        </div>

        <!-- 会话列表 -->
        <div v-if="!searchMode">
          <div v-if="conversations.length === 0" class="text-center py-12 text-gray-400 text-sm">
            <p>暂无对话</p>
            <p class="text-xs mt-1">点击上方"新对话"开始聊天</p>
          </div>
          <div v-for="c in conversations" :key="c.id" class="flex items-stretch border-b border-gray-50 hover:bg-gray-50 transition-colors"
            :class="{ 'bg-blue-50': activePeer === getPeer(c) }">
            <div
              @click="openChat(getPeer(c), getUid() == c.senderId ? c.receiverName : c.senderName)"
              class="flex-1 px-3 py-3 cursor-pointer min-w-0">
              <div class="flex items-center justify-between mb-1">
                <span class="text-sm font-medium text-gray-800">
                  {{ getUid() == c.senderId ? c.receiverName : c.senderName }}
                </span>
                <span class="text-[10px] text-gray-300 ml-2 flex-shrink-0">{{ timeStr(c.createdAt) }}</span>
              </div>
              <p class="text-xs text-gray-400 truncate">
                {{ getUid() == c.senderId ? '我: ' : '' }}{{ c.content?.slice(0, 40) }}
              </p>
            </div>
            <span
              @click="deleteConversation(getPeer(c))"
              style="cursor:pointer;width:36px;display:flex;align-items:center;justify-content:center;flex-shrink:0;color:#d1d5db;border-left:1px solid #f9fafb"
              onmouseover="this.style.color='#ef4444';this.style.background='#fef2f2'"
              onmouseout="this.style.color='#d1d5db';this.style.background=''"
              title="删除对话">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天区 -->
    <div class="flex-1 flex flex-col">
      <template v-if="activePeer">
        <div class="py-3 px-4 border-b border-gray-100 flex items-center justify-between">
          <p class="text-sm font-medium text-gray-800">{{ activePeerName }}</p>
          <span class="text-[10px] text-gray-400">{{ connected ? '实时连接' : '离线' }}</span>
        </div>
        <div ref="chatEl" class="flex-1 overflow-y-auto py-4 px-4 space-y-3">
          <div v-if="loading" class="text-center text-gray-400 text-sm">加载中...</div>
          <div v-for="(m, i) in messages" :key="m.id || i"
            :class="m.senderId == getUid() ? 'flex justify-end' : 'flex gap-2'">
            <div v-if="m.senderId != getUid()"
              class="w-7 h-7 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
              <span class="text-blue-600 text-xs font-bold">{{ (m.senderName || '?')[0] }}</span>
            </div>
            <div :class="m.senderId == getUid()
              ? 'max-w-[70%] px-3 py-2 bg-blue-600 text-white text-sm rounded-xl rounded-tr-md'
              : 'max-w-[70%] px-3 py-2 bg-gray-100 text-gray-800 text-sm rounded-xl rounded-tl-md'">
              {{ m.content }}
              <div class="text-[10px] mt-1" :class="m.senderId == getUid() ? 'text-white/60' : 'text-gray-400'">
                {{ timeStr(m.createdAt) }}
                <span v-if="m.senderId == getUid() && m.isRead === 1" class="ml-1">已读</span>
              </div>
            </div>
          </div>
        </div>
        <div class="py-3 px-4 border-t border-gray-100 flex gap-2">
          <input v-model="input" @keyup.enter="sendMsg"
            placeholder="输入消息... Enter 发送"
            :disabled="sending"
            class="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-50" />
          <button @click="sendMsg" :disabled="sending || !input.trim()"
            class="px-4 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-xl hover:bg-blue-700 disabled:opacity-40 transition-colors">
            发送
          </button>
        </div>
      </template>
      <div v-else class="flex-1 flex flex-col items-center justify-center text-gray-400 text-sm">
        <p>选择左侧会话开始聊天</p>
        <p class="text-xs mt-1">或点击"新对话"搜索用户</p>
      </div>
    </div>
  </div>
</template>
