<script setup>
import { ref, onMounted, nextTick } from 'vue'
const sessions = ref([])
const messages = ref([])
const currentSessionId = ref(null)
const input = ref('')
const loading = ref(false)
const token = localStorage.getItem('token')
const h = { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }

async function fetchSessions() {
  const res = await fetch('/api/ai/chat/sessions', { headers: h })
  const data = await res.json()
  sessions.value = data.data || []
}

async function openSession(sid) {
  currentSessionId.value = sid
  const res = await fetch('/api/ai/chat/sessions/' + sid + '/messages', { headers: h })
  const data = await res.json()
  messages.value = data.data || []
  await nextTick(); scrollDown()
}

async function newSession() {
  const res = await fetch('/api/ai/chat/sessions', { method: 'POST', headers: h, body: JSON.stringify({ type:'CAREER_ADVICE', title:'求职咨询' }) })
  const data = await res.json()
  if (data.code === 0) { await fetchSessions(); openSession(data.data) }
}

async function send() {
  if (!input.value.trim() || !currentSessionId.value) return
  const msg = input.value; input.value = ''; loading.value = true
  messages.value.push({ role: 'USER', content: msg })
  await nextTick(); scrollDown()
  const res = await fetch('/api/ai/chat/send', { method: 'POST', headers: h, body: JSON.stringify({ sessionId: currentSessionId.value, message: msg }) })
  const data = await res.json()
  if (data.code === 0) messages.value.push({ role: 'ASSISTANT', content: data.data })
  loading.value = false
  await nextTick(); scrollDown()
}

function scrollDown() {
  const el = document.getElementById('msg-area')
  if (el) el.scrollTop = el.scrollHeight
}

onMounted(async () => { await fetchSessions(); if (sessions.value.length > 0) openSession(sessions.value[0].id) })
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8 flex gap-4 h-[calc(100vh-8rem)]">
    <!-- Sidebar -->
    <div class="w-56 flex-shrink-0 bg-white border border-gray-200 rounded-xl p-3 overflow-y-auto">
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm font-semibold text-gray-700">会话</span>
        <button @click="newSession" class="text-xs px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">+</button>
      </div>
      <div v-for="s in sessions" :key="s.id" @click="openSession(s.id)"
        class="px-3 py-2 rounded-md cursor-pointer text-sm mb-1"
        :class="currentSessionId === s.id ? 'bg-blue-50 text-blue-700 font-medium' : 'text-gray-600 hover:bg-gray-50'">
        {{ s.title || '新对话' }}
      </div>
    </div>

    <!-- Chat Area -->
    <div class="flex-1 bg-white border border-gray-200 rounded-xl flex flex-col">
      <div class="p-3 border-b border-gray-100 text-sm font-semibold text-gray-700">AI 求职助手</div>
      <div id="msg-area" class="flex-1 p-4 overflow-y-auto space-y-4">
        <div v-if="messages.length === 0" class="text-center text-gray-400 mt-20">选择或创建一个会话开始对话</div>
        <div v-for="(m,i) in messages" :key="i" :class="m.role === 'USER' ? 'flex justify-end' : 'flex'">
          <div :class="m.role === 'USER' ? 'bg-blue-600 text-white max-w-[70%]' : 'bg-gray-100 text-gray-800 max-w-[80%]'"
            class="px-4 py-2.5 rounded-lg text-sm leading-relaxed whitespace-pre-wrap">{{ m.content }}</div>
        </div>
        <div v-if="loading" class="flex"><div class="bg-gray-100 text-gray-400 px-4 py-2.5 rounded-lg text-sm">AI 思考中...</div></div>
      </div>
      <div class="p-3 border-t border-gray-100 flex gap-2">
        <input v-model="input" @keyup.enter="send" placeholder="输入求职问题..."
          class="flex-1 px-3 py-2 border border-gray-200 rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
        <button @click="send" :disabled="loading" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:bg-blue-300">发送</button>
      </div>
    </div>
  </div>
</template>
