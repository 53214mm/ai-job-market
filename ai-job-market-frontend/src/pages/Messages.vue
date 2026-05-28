<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useStomp } from '../composables/useStomp.js'

const token = localStorage.getItem('token')
const uid = (() => {
  try { const u = JSON.parse(localStorage.getItem('user') || '{}'); return u.id || null } catch(e) { return null }
})()
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
      messages.value = (d.data.records || []).reverse()
      let anyRead = false
      for (const m of messages.value) {
        if (m.receiverId === Number(uid) && m.isRead === 0) {
          fetch('/api/messages/' + m.id + '/read', { method: 'PUT', headers: h() }).catch(() => {})
          anyRead = true
        }
      }
      if (anyRead) window.dispatchEvent(new Event('unread-changed'))
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
        messages.value.push({ ...d.data, senderId: Number(uid) })
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

function timeStr(t) {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await loadConversations()
  await connect()
  unsubMessage = onMessage((msg) => {
    loadConversations()
    // 不重复添加自己刚发的消息（REST 发送时已本地 push）
    if (msg.senderId === Number(uid)) return
    const peerId = msg.senderId
    if (activePeer.value && peerId === activePeer.value) {
      // 避免重复
      if (!messages.value.some(m => m.id === msg.id)) {
        messages.value.push(msg)
        scroll()
      }
      fetch('/api/messages/' + msg.id + '/read', { method: 'PUT', headers: h() }).catch(() => {})
      window.dispatchEvent(new Event('unread-changed'))
    }
  })
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
        <span class="w-2 h-2 rounded-full" :class="connected ? 'bg-green-500' : 'bg-gray-300'" title="连接状态"></span>
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
          <div v-for="c in conversations" :key="c.id"
            @click="openChat(c.senderId === Number(uid) ? c.receiverId : c.senderId, c.senderId === Number(uid) ? c.receiverName : c.senderName)"
            class="px-3 py-3 border-b border-gray-50 cursor-pointer hover:bg-gray-50 transition-colors"
            :class="{ 'bg-blue-50': activePeer === (c.senderId === Number(uid) ? c.receiverId : c.senderId) }">
            <div class="flex items-center justify-between mb-1">
              <span class="text-sm font-medium text-gray-800">
                {{ c.senderId === Number(uid) ? c.receiverName : c.senderName }}
              </span>
              <span class="text-xs text-gray-400">{{ timeStr(c.createdAt) }}</span>
            </div>
            <p class="text-xs text-gray-400 truncate">
              {{ c.senderId === Number(uid) ? '我: ' : '' }}{{ c.content?.slice(0, 40) }}
            </p>
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
            :class="m.senderId === Number(uid) ? 'flex justify-end' : 'flex gap-2'">
            <div v-if="m.senderId !== Number(uid)"
              class="w-7 h-7 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
              <span class="text-blue-600 text-xs font-bold">{{ (m.senderName || '?')[0] }}</span>
            </div>
            <div :class="m.senderId === Number(uid)
              ? 'max-w-[70%] px-3 py-2 bg-blue-600 text-white text-sm rounded-xl rounded-tr-md'
              : 'max-w-[70%] px-3 py-2 bg-gray-100 text-gray-800 text-sm rounded-xl rounded-tl-md'">
              {{ m.content }}
              <div class="text-[10px] mt-1" :class="m.senderId === Number(uid) ? 'text-white/60' : 'text-gray-400'">
                {{ timeStr(m.createdAt) }}
                <span v-if="m.senderId === Number(uid) && m.isRead === 1" class="ml-1">已读</span>
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
