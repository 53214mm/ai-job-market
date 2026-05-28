<script setup>
import { ref, onMounted } from 'vue'

const token = localStorage.getItem('token')
const h = () => ({ 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' })
const notifications = ref([])
const loading = ref(true)
const current = ref(1)
const total = ref(0)

const typeColors = {
  'APPLICATION_UPDATE': 'bg-blue-50 text-blue-600',
  'INTERVIEW_INVITE': 'bg-purple-50 text-purple-600',
  'MESSAGE': 'bg-green-50 text-green-600',
  'SYSTEM': 'bg-gray-50 text-gray-600',
}

async function loadNotes() {
  loading.value = true
  try {
    const res = await fetch('/api/notifications?current=' + current.value + '&pageSize=20', { headers: h() })
    const d = await res.json()
    if (d.code === 0) {
      notifications.value = d.data.records || []
      total.value = d.data.total || 0
    }
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

function notifyChanged() {
  window.dispatchEvent(new Event('unread-changed'))
}

async function markRead(id) {
  await fetch('/api/notifications/' + id + '/read', { method: 'PUT', headers: h() })
  const n = notifications.value.find(x => x.id === id)
  if (n) n.isRead = 1
  notifyChanged()
}

async function markAllRead() {
  await fetch('/api/notifications/read-all', { method: 'PUT', headers: h() })
  notifications.value.forEach(n => n.isRead = 1)
  notifyChanged()
}

async function del(id) {
  await fetch('/api/notifications/' + id, { method: 'DELETE', headers: h() })
  notifications.value = notifications.value.filter(n => n.id !== id)
  notifyChanged()
}

function timeAgo(t) {
  if (!t) return ''
  const diff = Date.now() - new Date(t).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return mins + '分钟前'
  const hours = Math.floor(mins / 60)
  if (hours < 24) return hours + '小时前'
  const days = Math.floor(hours / 24)
  if (days < 30) return days + '天前'
  return t.slice(0, 10)
}

onMounted(loadNotes)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">消息通知</h1>
      <button v-if="notifications.some(n => n.isRead === 0)"
        @click="markAllRead" class="text-xs text-blue-600 hover:text-blue-700 font-medium">
        全部已读
      </button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

    <div v-else-if="notifications.length === 0" class="text-center py-16 bg-gray-50 rounded-xl">
      <p class="text-gray-400 text-sm">暂无通知</p>
    </div>

    <div v-else class="space-y-2">
      <div v-for="n in notifications" :key="n.id"
        class="bg-white border rounded-lg p-4 flex items-start gap-3 cursor-pointer hover:shadow-sm transition-shadow"
        :class="n.isRead === 0 ? 'border-blue-100 bg-blue-50/30' : 'border-gray-100'"
        @click="n.isRead === 0 && markRead(n.id)">
        <!-- unread dot -->
        <div class="flex-shrink-0 mt-1.5">
          <div v-if="n.isRead === 0" class="w-2 h-2 bg-blue-500 rounded-full"></div>
          <div v-else class="w-2 h-2 rounded-full"></div>
        </div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <span class="text-xs font-medium px-2 py-0.5 rounded-full" :class="typeColors[n.type] || 'bg-gray-50 text-gray-600'">
              {{ n.typeLabel || n.type }}
            </span>
            <span class="text-xs text-gray-400">{{ timeAgo(n.createdAt) }}</span>
          </div>
          <p class="text-sm font-medium text-gray-800">{{ n.title }}</p>
          <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ n.content }}</p>
        </div>
        <button @click.stop="del(n.id)" class="text-xs text-gray-300 hover:text-red-400 flex-shrink-0 mt-1">删除</button>
      </div>

      <!-- pagination -->
      <div v-if="total > 20" class="flex justify-center gap-2 pt-4">
        <button @click="current--; loadNotes()" :disabled="current <= 1"
          class="px-3 py-1 text-xs border rounded hover:bg-gray-50 disabled:opacity-30">上一页</button>
        <span class="text-xs text-gray-400 py-1">{{ current }} / {{ Math.ceil(total / 20) }}</span>
        <button @click="current++; loadNotes()" :disabled="current * 20 >= total"
          class="px-3 py-1 text-xs border rounded hover:bg-gray-50 disabled:opacity-30">下一页</button>
      </div>
    </div>
  </div>
</template>
