<script setup>
import { ref, nextTick, watch, onBeforeUnmount } from 'vue'

const token = localStorage.getItem('token')

// ─── 按用户隔离存储 ───
function getUID() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.id || (token ? token.slice(-8) : 'anon')
  } catch(e) { return token ? token.slice(-8) : 'anon' }
}
let uid = getUID()
let STORAGE_KEY = 'ai_chat_msgs_' + uid
let CHAT_ID_KEY = 'ai_chat_id_' + uid

function loadHistory() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const msgs = JSON.parse(saved)
      if (msgs.length > 0) return msgs
    }
  } catch(e) {}
  return [{ role: 'assistant', content: '你好！我是AI招聘助手，可以帮你解答求职、招聘相关的任何问题。\n\n你可以问我：\n- 2026年Java后端的发展方向\n- 如何优化简历通过AI筛选\n- 大厂面试流程和准备策略\n- AI行业的最新趋势和薪资水平' }]
}

function saveHistory() {
  const toSave = messages.value.slice(-50)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(toSave))
}

const messages = ref(loadHistory())
const input = ref('')
const loading = ref(false)
const streamingContent = ref('')
const chatEl = ref(null)
let chatId = localStorage.getItem(CHAT_ID_KEY) || ('recruitment-' + Date.now())

// ─── 检测账号切换：重新加载 + 重置会话 ───
import { useRoute } from 'vue-router'
const route = useRoute()
watch(() => route.path, () => {
  const newUID = getUID()
  if (newUID !== uid) {
    uid = newUID
    STORAGE_KEY = 'ai_chat_msgs_' + uid
    CHAT_ID_KEY = 'ai_chat_id_' + uid
    messages.value = loadHistory()
    chatId = localStorage.getItem(CHAT_ID_KEY) || ('recruitment-' + Date.now())
    streamingContent.value = ''
    loading.value = false
  }
})

// 消息变化时自动保存
watch(messages, saveHistory, { deep: true })

// 页面切换前：确保最新数据已持久化
onBeforeUnmount(() => {
  // onmessage 已在每个 SSE 事件中直接写 localStorage，此处兜底
  if (streamingContent.value) {
    messages.value.push({ role: 'assistant', content: streamingContent.value })
  }
  saveHistory()
  localStorage.setItem(CHAT_ID_KEY, chatId)
})

function scroll() { nextTick(() => { const el = chatEl.value; if (el) el.scrollTop = el.scrollHeight }) }

function renderMd(text) {
  if (!text) return ''
  try { return typeof marked !== 'undefined' ? marked.parse(text) : text.replace(/\n/g, '<br>') }
  catch(e) { return text.replace(/\n/g, '<br>') }
}

async function send() {
  const msg = input.value.trim()
  if (!msg || loading.value) return
  messages.value.push({ role: 'user', content: msg })
  input.value = ''; loading.value = true
  streamingContent.value = ''
  scroll()

  try {
    // 用 EventSource API 处理 SSE（浏览器原生支持，最可靠）
    const url = '/api/ai/chat/stream?message=' + encodeURIComponent(msg)
      + '&chatId=' + chatId
      + '&token=' + encodeURIComponent(token || '')
    const es = new EventSource(url)

    es.onmessage = (event) => {
      streamingContent.value += event.data
      // 每个 SSE 事件直接写 localStorage，不依赖 Vue 生命周期
      try {
        const msgs = messages.value.slice(-49)
        msgs.push({ role: 'assistant', content: streamingContent.value })
        localStorage.setItem(STORAGE_KEY, JSON.stringify(msgs))
      } catch(e) {}
      scroll()
    }

    es.onerror = () => {
      es.close()
      if (streamingContent.value) {
        messages.value.push({ role: 'assistant', content: streamingContent.value })
      } else {
        messages.value.push({ role: 'assistant', content: '（响应超时，请重试）' })
      }
      streamingContent.value = ''
            loading.value = false
    }

    // 正常关闭（服务端 complete）
    es.addEventListener('done', () => {
      es.close()
      if (streamingContent.value) {
        messages.value.push({ role: 'assistant', content: streamingContent.value })
        localStorage.setItem(CHAT_ID_KEY, chatId)
      }
      streamingContent.value = ''
            loading.value = false
    })
  } catch(e) {
    messages.value.push({ role: 'assistant', content: '（网络异常，请重试）' })
    loading.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 h-[calc(100vh-64px)] flex flex-col">
    <div class="py-3 border-b border-gray-100 flex items-center justify-between">
      <div>
        <h1 class="text-lg font-bold text-gray-900">AI 招聘助手</h1>
        <p class="text-xs text-gray-400">流式输出 · 支持 Markdown · 咨询求职/招聘问题</p>
      </div>
      <button @click="messages = [{ role: 'assistant', content: '对话已清空，有什么可以帮你的？' }]; chatId = 'recruitment-' + Date.now(); localStorage.removeItem(STORAGE_KEY); localStorage.removeItem(CHAT_ID_KEY)"
        class="text-xs text-gray-400 hover:text-red-500 transition-colors">清空对话</button>
    </div>

    <div ref="chatEl" class="flex-1 overflow-y-auto py-4 space-y-4">
      <!-- 已完成消息 -->
      <div v-for="(m, i) in messages" :key="i" :class="m.role === 'user' ? 'flex justify-end' : 'flex gap-3'">
        <div v-if="m.role !== 'user'" class="w-8 h-8 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
          <span class="text-white text-xs font-bold">AI</span>
        </div>
        <div v-if="m.role === 'user'" class="max-w-[75%] px-4 py-2.5 bg-blue-600 text-white text-sm rounded-2xl rounded-tr-md">{{ m.content }}</div>
        <div v-else class="max-w-[85%] px-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl rounded-tl-md markdown-body text-sm text-gray-800">
          <div v-html="renderMd(m.content)"></div>
        </div>
      </div>
      <!-- 流式输出中的临时气泡 -->
      <div v-if="loading" class="flex gap-3">
        <div class="w-8 h-8 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
          <span class="text-white text-xs font-bold">AI</span>
        </div>
        <div class="max-w-[85%] px-4 py-3 bg-purple-50 border border-purple-100 rounded-2xl rounded-tl-md text-sm text-gray-800">
          <span v-if="streamingContent" class="markdown-body" v-html="renderMd(streamingContent)"></span>
          <span v-else class="flex gap-1">
            <span class="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style="animation-delay:0ms"></span>
            <span class="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style="animation-delay:150ms"></span>
            <span class="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style="animation-delay:300ms"></span>
          </span>
          <span class="inline-block w-0.5 h-4 bg-purple-500 animate-pulse ml-0.5 align-middle rounded-sm"></span>
        </div>
      </div>
    </div>

    <div class="py-3 border-t border-gray-100">
      <div class="flex gap-2">
        <input v-model="input" @keyup.enter="send"
          placeholder="如：Java后端2026年发展方向？"
          :disabled="loading"
          class="flex-1 px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-50" />
        <button @click="send" :disabled="loading || !input.trim()"
          class="px-5 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white text-sm font-medium rounded-xl hover:from-blue-700 hover:to-purple-700 disabled:opacity-40 transition-all">
          <svg v-if="!loading" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"/></svg>
          <span v-else>...</span>
        </button>
      </div>
      <p class="text-xs text-gray-400 mt-2 text-center">AI 基于多轮对话上下文回答 · 支持 Markdown 格式</p>
    </div>
  </div>
</template>
