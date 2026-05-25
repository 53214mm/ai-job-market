<script setup>
import { ref, onMounted } from 'vue'
const apps = ref([]); const loading = ref(true)

onMounted(async () => {
  try {
    const res = await fetch('/api/applications/my?current=1&size=20', {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.data) {
      // 只显示有面试的投递
      apps.value = await Promise.all((data.data.records||[]).filter(a => a.status === 'INTERVIEW' || a.status === 'OFFER').map(async a => {
        const dres = await fetch('/api/applications/' + a.id, {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        })
        const dd = await dres.json()
        return { ...a, interview: dd.data?.interview }
      }))
    }
  } catch(e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">面试日程</h1>
    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>
    <div v-else-if="apps.length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">暂无面试安排</div>
    <div v-else class="space-y-3">
      <div v-for="a in apps" :key="a.id" class="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm font-semibold text-gray-900">{{ a.jobTitle }}</p>
            <p class="text-xs text-gray-500">{{ a.companyName }}</p>
          </div>
          <span class="px-2 py-0.5 bg-purple-50 text-purple-600 text-xs rounded-full font-medium">面试中</span>
        </div>
        <div v-if="a.interview" class="mt-3 p-3 bg-blue-50 rounded-md text-sm space-y-1">
          <p><span class="text-gray-500">类型：</span>{{ a.interview.interviewType }}</p>
          <p><span class="text-gray-500">时间：</span>{{ (a.interview.scheduledTime||'').slice(0,16) }}</p>
          <p><span class="text-gray-500">地点：</span>{{ a.interview.location }}</p>
          <p><span class="text-gray-500">面试官：</span>{{ a.interview.interviewer }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
