<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { resumeApi } from '../api.js'

const router = useRouter()
const resumes = ref([])
const company = ref(null)
// TODO: 对接职位模块 API，目前使用静态占位数据
const jobs = ref([
  { id: 1, title: '高级 Java 开发工程师', company: '阿里巴巴', city: '杭州', salary: '25K-45K', matchScore: 92 },
  { id: 2, title: 'AI 算法工程师', company: '百度', city: '北京', salary: '35K-60K', matchScore: 88 },
  { id: 3, title: '前端开发工程师', company: '字节跳动', city: '上海', salary: '30K-50K', matchScore: 85 },
  { id: 4, title: '产品经理（AI 方向）', company: '腾讯', city: '深圳', salary: '28K-48K', matchScore: 79 },
])

onMounted(async () => {
  try {
    const data = await resumeApi.list({ current: 1, pageSize: 5 })
    resumes.value = data.records || []
  } catch (e) { /* 未登录或无简历 */ }
  try {
    const res = await fetch('/api/user/current')
    const d = await res.json()
    if (d.code === 0 && d.data.role === 'RECRUITER') {
      // 如果是招聘方，获取公司信息
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
            <p class="text-2xl font-bold text-green-600">0</p>
            <p class="text-xs text-gray-500 mt-1">投递中</p>
          </div>
          <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
            <p class="text-2xl font-bold text-purple-600">0</p>
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
