<script setup>
import { ref, onMounted } from 'vue'

const token = localStorage.getItem('token')
const h = () => ({ 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' })

const apps = ref([])
const statusFilter = ref('')
const loading = ref(false)

const statusLabels = {
  APPLIED: '已投递', VIEWED: '已查看', SCREENING: '筛选中',
  INTERVIEW: '面试中', OFFER: '已发Offer', HIRED: '已入职',
  REJECTED: '不合适', WITHDRAWN: '已撤回'
}

async function loadApps() {
  loading.value = true
  try {
    let url = '/api/applications/received?current=1&size=50'
    if (statusFilter.value) url += '&status=' + statusFilter.value
    const res = await fetch(url, { headers: h() })
    const d = await res.json()
    if (d.code === 0) apps.value = d.data?.records || []
    else apps.value = []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function changeStatus(appId, status) {
  try {
    const res = await fetch('/api/applications/' + appId + '/status', {
      method: 'PUT', headers: h(),
      body: JSON.stringify({ status })
    })
    const d = await res.json()
    if (d.code === 0) {
      loadApps()
    } else {
      alert(d.message || '操作失败')
    }
  } catch(e) { alert('网络错误') }
}

onMounted(loadApps)
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">收到的简历</h1>
      <select v-model="statusFilter" @change="loadApps"
        class="px-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
        <option value="">全部状态</option>
        <option v-for="(label, key) in statusLabels" :key="key" :value="key">{{ label }}</option>
      </select>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

    <div v-else-if="apps.length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">
      暂无投递记录
    </div>

    <div v-else class="space-y-3">
      <div v-for="a in apps" :key="a.id"
        class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <span class="text-sm font-medium text-gray-900">{{ a.jobTitle }}</span>
              <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                :class="{
                  'bg-blue-50 text-blue-600': a.status === 'APPLIED',
                  'bg-yellow-50 text-yellow-600': a.status === 'VIEWED' || a.status === 'SCREENING',
                  'bg-purple-50 text-purple-600': a.status === 'INTERVIEW',
                  'bg-green-50 text-green-600': a.status === 'OFFER' || a.status === 'HIRED',
                  'bg-red-50 text-red-600': a.status === 'REJECTED',
                  'bg-gray-50 text-gray-500': a.status === 'WITHDRAWN',
                }">
                {{ statusLabels[a.status] || a.status }}
              </span>
              <span v-if="a.aiMatchScore" class="text-xs text-gray-400">AI匹配: {{ a.aiMatchScore }}%</span>
            </div>
            <p class="text-xs text-gray-400 mb-2">
              求职者: ID#{{ a.seekerId }} · 投递时间: {{ (a.createdAt||'').slice(0, 10) }}
            </p>
            <p v-if="a.coverLetter" class="text-xs text-gray-500 mb-2 bg-gray-50 p-2 rounded">
              附言: {{ a.coverLetter?.slice(0, 120) }}{{ a.coverLetter?.length > 120 ? '...' : '' }}
            </p>
            <div class="flex gap-2 flex-wrap">
              <button v-if="a.status === 'APPLIED'" @click="changeStatus(a.id, 'VIEWED')"
                class="text-xs px-2 py-1 border border-gray-200 rounded hover:bg-gray-50 text-gray-600">标记已查看</button>
              <button v-if="a.status === 'APPLIED' || a.status === 'VIEWED'" @click="changeStatus(a.id, 'SCREENING')"
                class="text-xs px-2 py-1 border border-blue-200 rounded hover:bg-blue-50 text-blue-600">筛选通过</button>
              <button v-if="a.status === 'SCREENING'" @click="changeStatus(a.id, 'INTERVIEW')"
                class="text-xs px-2 py-1 border border-purple-200 rounded hover:bg-purple-50 text-purple-600">安排面试</button>
              <button v-if="a.status === 'INTERVIEW'" @click="changeStatus(a.id, 'OFFER')"
                class="text-xs px-2 py-1 border border-green-200 rounded hover:bg-green-50 text-green-600">发Offer</button>
              <button v-if="a.status !== 'REJECTED' && a.status !== 'HIRED' && a.status !== 'WITHDRAWN'"
                @click="changeStatus(a.id, 'REJECTED')"
                class="text-xs px-2 py-1 border border-red-200 rounded hover:bg-red-50 text-red-600">不合适</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
