<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { resumeApi } from '../api.js'

const router = useRouter()
const resumes = ref([])
const jobs = ref([])
const stats = ref({ applications: 0, interviews: 0 })

onMounted(async () => {
  try {
    const data = await resumeApi.list({ current: 1, pageSize: 5 })
    resumes.value = data.records || []
  } catch (e) { /* 未登录或无简历 */ }

  try {
    const token = localStorage.getItem('token')
    if (!token) return
    const h = { 'Authorization': 'Bearer ' + token }
    // 先获取第一个简历ID用于推荐
    const resumeRes = await fetch('/api/resumes?current=1&pageSize=1', { headers: h })
    const resumeData = await resumeRes.json()
    const resumeId = resumeData.code === 0 ? resumeData.data?.records?.[0]?.id : null

    const [jobRes, appRes] = await Promise.all([
      fetch('/api/jobs/recommend' + (resumeId ? '?resumeId=' + resumeId : ''), { headers: h }),
      fetch('/api/applications/my?current=1&size=10', { headers: h })
    ])
    const jobData = await jobRes.json()
    const appData = await appRes.json()
    if (jobData.code === 0) {
      jobs.value = (jobData.data || []).slice(0, 4).map(j => ({
        id: j.id,
        title: j.title,
        company: j.companyName || '未知公司',
        city: j.city || '全国',
        salary: (j.salaryMin || 0) + 'K-' + (j.salaryMax || 0) + 'K',
        matchScore: j.matchScore || 0
      }))
    }
    if (appData.code === 0) {
      stats.value.applications = appData.data.total || 0
    }
  } catch (e) { /* ignore */ }
})

const user = JSON.parse(localStorage.getItem('user') || '{}')
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Welcome -->
    <div class="bg-gradient-to-r from-blue-600 to-blue-700 rounded-xl p-6 mb-8 text-white">
      <h1 class="text-2xl font-bold mb-1">欢迎回来，{{ user.nickname || '求职者' }}</h1>
      <p class="text-blue-100 text-sm">AI 智能匹配，让好工作主动找到你</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Left -->
      <div class="lg:col-span-2 space-y-6">
        <!-- Quick Stats -->
        <div class="grid grid-cols-3 gap-4">
          <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
            <p class="text-2xl font-bold text-blue-600">{{ resumes.length }}</p>
            <p class="text-xs text-gray-500 mt-1">我的简历</p>
          </div>
          <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
            <p class="text-2xl font-bold text-green-600">{{ stats.applications }}</p>
            <p class="text-xs text-gray-500 mt-1">投递中</p>
          </div>
          <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
            <p class="text-2xl font-bold text-purple-600">{{ stats.interviews }}</p>
            <p class="text-xs text-gray-500 mt-1">面试邀请</p>
          </div>
        </div>

        <!-- AI 推荐职位 -->
        <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-base font-semibold text-gray-900">AI 智能推荐</h2>
            <router-link to="/jobs" class="text-sm text-blue-600">查看全部 &rarr;</router-link>
          </div>
          <div v-for="j in jobs" :key="j.id" class="flex items-center justify-between py-3 border-b border-gray-100 last:border-0">
            <div>
              <p class="text-sm font-medium text-gray-800">{{ j.title }}</p>
              <p class="text-xs text-gray-500">{{ j.company }} · {{ j.city }} · {{ j.salary }}</p>
            </div>
            <span class="px-2 py-0.5 bg-green-50 text-green-600 text-xs rounded-full font-medium">匹配 {{ j.matchScore }}%</span>
          </div>
        </div>
      </div>

      <!-- Right -->
      <div class="space-y-4">
        <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-900 mb-3">快速操作</h2>
          <div class="space-y-2">
            <button @click="router.push('/resumes/create')" class="w-full text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md text-sm transition-colors">+ 创建简历</button>
            <button @click="router.push('/resumes')" class="w-full text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md text-sm transition-colors">管理我的简历</button>
            <button @click="router.push('/jobs')" class="w-full text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md text-sm transition-colors">搜索职位</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
