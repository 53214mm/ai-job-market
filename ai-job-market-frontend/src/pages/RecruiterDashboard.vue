<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const myCompany = ref(null)
const stats = ref({ publishedJobs: 0, receivedApps: 0, interviewing: 0, pending: 0 })

onMounted(async () => {
  const token = localStorage.getItem('token')
  const h = token ? { 'Authorization': 'Bearer ' + token } : {}
  try {
    const res = await fetch('/api/user/current', { headers: h })
    const d = await res.json()
    if (d.code === 0) {
      user.role = d.data.role
      user.id = d.data.id
    }
  } catch (e) { /* */ }
  try {
    const [compRes, jobRes, appRes] = await Promise.all([
      fetch('/api/companies?current=1&pageSize=50', { headers: h }),
      fetch('/api/jobs/my?current=1&pageSize=100', { headers: h }),
      fetch('/api/applications/received?current=1&pageSize=100', { headers: h })
    ])
    const [cdata, jdata, adata] = await Promise.all([compRes.json(), jobRes.json(), appRes.json()])
    if (cdata.code === 0) myCompany.value = cdata.data.records?.[0] || null
    if (jdata.code === 0) {
      const records = jdata.data.records || []
      stats.value.publishedJobs = records.filter(j => j.status === 'PUBLISHED').length
    }
    if (adata.code === 0) {
      const records = adata.data.records || []
      stats.value.receivedApps = records.length
      stats.value.interviewing = records.filter(a => a.status === 'INTERVIEW').length
      stats.value.pending = records.filter(a => a.status === 'APPLIED' || a.status === 'SCREENING').length
    }
  } catch (e) { /* */ }
})

function goCompany() {
  if (myCompany.value) router.push('/companies/' + myCompany.value.id)
  else router.push('/recruiter/company')
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-gradient-to-r from-purple-600 to-purple-700 rounded-xl p-6 mb-8 text-white">
      <h1 class="text-2xl font-bold mb-1">招聘工作台</h1>
      <p class="text-purple-100 text-sm">{{ user.nickname || '招聘方' }}，欢迎回来</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-purple-600">{{ stats.publishedJobs }}</p>
        <p class="text-xs text-gray-500 mt-1">发布中职位</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-blue-600">{{ stats.receivedApps }}</p>
        <p class="text-xs text-gray-500 mt-1">收到简历</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-green-600">{{ stats.interviewing }}</p>
        <p class="text-xs text-gray-500 mt-1">面试中</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-orange-600">{{ stats.pending }}</p>
        <p class="text-xs text-gray-500 mt-1">待处理</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <h2 class="text-base font-semibold text-gray-900 mb-4">快速操作</h2>
        <div class="space-y-2">
          <button @click="router.push('/recruiter/jobs')" class="w-full text-left px-3 py-2.5 bg-gray-50 hover:bg-purple-50 hover:text-purple-700 rounded-md text-sm transition-colors">发布新职位</button>
          <button @click="router.push('/recruiter/applications')" class="w-full text-left px-3 py-2.5 bg-gray-50 hover:bg-purple-50 hover:text-purple-700 rounded-md text-sm transition-colors">查看投递简历</button>
        </div>
      </div>
      <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <h2 class="text-base font-semibold text-gray-900 mb-4">公司信息</h2>
        <template v-if="myCompany">
          <p class="text-sm font-medium text-gray-800">{{ myCompany.name }}</p>
          <p class="text-xs text-gray-500 mt-1">{{ myCompany.industry || '-' }} · {{ myCompany.scale || '-' }}</p>
          <p class="text-xs mt-1" :class="myCompany.verified ? 'text-green-600' : 'text-yellow-600'">
            {{ myCompany.verified ? '已认证' : '待认证' }}
          </p>
          <button @click="goCompany" class="mt-3 px-4 py-2 bg-purple-600 text-white text-sm rounded-md hover:bg-purple-700 transition-colors">查看公司主页</button>
        </template>
        <template v-else>
          <p class="text-sm text-gray-500">请先完善公司信息并完成企业认证，即可发布职位。</p>
          <button @click="goCompany" class="mt-3 px-4 py-2 bg-purple-600 text-white text-sm rounded-md hover:bg-purple-700 transition-colors">创建公司</button>
        </template>
      </div>
    </div>
  </div>
</template>
