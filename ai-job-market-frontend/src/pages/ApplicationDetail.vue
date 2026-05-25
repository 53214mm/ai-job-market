<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute(); const router = useRouter()
const app = ref(null); const loading = ref(true)
const statusOptions = ['VIEWED','SCREENING','INTERVIEW','OFFER','HIRED','REJECTED']
const statusForm = ref({ status: '', remark: '' })
const interviewForm = ref({ interviewType:'VIDEO', scheduledTime:'', durationMinutes:60, location:'', interviewer:'', contactPhone:'' })

async function fetch() {
  try {
    const res = await fetch('/api/applications/' + route.params.id, {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    app.value = data.data
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function updateStatus() {
  await fetch('/api/applications/' + route.params.id + '/status', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
    body: JSON.stringify(statusForm.value)
  })
  statusForm.value = { status: '', remark: '' }
  fetch()
}

async function scheduleInterview() {
  await fetch('/api/applications/' + route.params.id + '/interview', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
    body: JSON.stringify(interviewForm.value)
  })
  interviewForm.value = { interviewType:'VIDEO', scheduledTime:'', durationMinutes:60, location:'', interviewer:'', contactPhone:'' }
  fetch()
}

onMounted(fetch)
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <button @click="router.back()" class="text-sm text-gray-500 hover:text-blue-600 mb-4">&larr; 返回</button>
    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

    <template v-else-if="app">
      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
        <h1 class="text-lg font-bold text-gray-900 mb-2">{{ app.jobTitle }}</h1>
        <p class="text-sm text-gray-500 mb-3">{{ app.companyName }} · 投递于 {{ (app.createdAt||'').slice(0,10) }}</p>
        <div class="flex flex-wrap gap-3 text-xs">
          <span v-if="app.aiMatchScore" class="px-2 py-1 bg-green-50 text-green-700 rounded font-medium">AI匹配 {{ app.aiMatchScore }}%</span>
          <span class="px-2 py-1 bg-blue-50 text-blue-700 rounded">简历: {{ app.resumeTitle }}</span>
        </div>
        <p v-if="app.coverLetter" class="mt-4 p-3 bg-gray-50 rounded text-sm text-gray-600">{{ app.coverLetter }}</p>
      </div>

      <!-- Status Timeline -->
      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
        <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">状态时间线</h2>
        <div class="relative pl-6 border-l-2 border-gray-100 space-y-4">
          <div v-for="log in (app.logs||[])" :key="log.id" class="relative">
            <div class="absolute -left-[25px] w-3 h-3 rounded-full bg-blue-600 border-2 border-white mt-1"></div>
            <p class="text-sm font-medium text-gray-800">{{ log.toStatus ? (log.toStatus==='APPLIED'?'已投递':log.toStatus==='VIEWED'?'已查看':log.toStatus==='SCREENING'?'筛选中':log.toStatus==='INTERVIEW'?'面试中':log.toStatus==='OFFER'?'发Offer':log.toStatus==='HIRED'?'已录用':log.toStatus==='REJECTED'?'已拒绝':log.toStatus) : '-' }}</p>
            <p class="text-xs text-gray-400">{{ (log.createdAt||'').slice(0,16) }} <span v-if="log.remark">· {{ log.remark }}</span></p>
          </div>
        </div>
      </div>

      <!-- Interview Info -->
      <div v-if="app.interview" class="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-6">
        <h2 class="text-sm font-semibold text-blue-600 uppercase tracking-wider mb-3">面试安排</h2>
        <div class="grid grid-cols-2 gap-2 text-sm">
          <span class="text-gray-500">类型：</span><span>{{ app.interview.interviewType }}</span>
          <span class="text-gray-500">时间：</span><span>{{ (app.interview.scheduledTime||'').slice(0,16) }}</span>
          <span class="text-gray-500">地点：</span><span>{{ app.interview.location }}</span>
          <span class="text-gray-500">面试官：</span><span>{{ app.interview.interviewer }}</span>
        </div>
      </div>

      <!-- Recruiter Actions -->
      <div v-if="app.status !== 'REJECTED' && app.status !== 'HIRED'" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">更新状态</h3>
          <select v-model="statusForm.status" class="w-full px-3 py-2 border rounded-md text-sm mb-2">
            <option value="">选择状态</option>
            <option v-for="s in statusOptions" :key="s" :value="s">{{ s }}</option>
          </select>
          <input v-model="statusForm.remark" placeholder="备注" class="w-full px-3 py-2 border rounded-md text-sm mb-2" />
          <button @click="updateStatus" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700">确认</button>
        </div>
        <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h3 class="text-sm font-semibold text-gray-900 mb-3">安排面试</h3>
          <select v-model="interviewForm.interviewType" class="w-full px-3 py-2 border rounded-md text-sm mb-2"><option>VIDEO</option><option>PHONE</option><option>ONSITE</option></select>
          <input v-model="interviewForm.scheduledTime" placeholder="时间 2026-05-27 14:00:00" class="w-full px-3 py-2 border rounded-md text-sm mb-2" />
          <input v-model="interviewForm.location" placeholder="地点或链接" class="w-full px-3 py-2 border rounded-md text-sm mb-2" />
          <button @click="scheduleInterview" class="px-4 py-2 bg-purple-600 text-white text-sm rounded-md hover:bg-purple-700">安排面试</button>
        </div>
      </div>
    </template>
  </div>
</template>
