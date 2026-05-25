<script setup>
import { ref, onMounted } from 'vue'

const jobs = ref([])
const loading = ref(true)
const filter = ref('all')

async function fetchJobs() {
  loading.value = true
  try {
    const res = await fetch('/api/jobs?current=1&pageSize=50', {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) jobs.value = data.data.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function closeJob(id) {
  if (!confirm('确定关闭此职位？')) return
  try {
    const res = await fetch('/api/jobs/' + id + '/close', {
      method: 'PUT', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) {
      const j = jobs.value.find(x => x.id === id)
      if (j) j.status = 'CLOSED'
    } else { alert(data.message) }
  } catch (e) { alert('操作失败') }
}

const filtered = () => {
  if (filter.value === 'published') return jobs.value.filter(j => j.status === 'PUBLISHED')
  if (filter.value === 'closed') return jobs.value.filter(j => j.status === 'CLOSED')
  if (filter.value === 'draft') return jobs.value.filter(j => j.status === 'DRAFT')
  return jobs.value
}

onMounted(fetchJobs)
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">职位管理</h1>
      <div class="flex border border-gray-200 rounded-md overflow-hidden">
        <button @click="filter = 'all'" :class="filter === 'all' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">全部</button>
        <button @click="filter = 'published'" :class="filter === 'published' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">已发布</button>
        <button @click="filter = 'draft'" :class="filter === 'draft' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">草稿</button>
        <button @click="filter = 'closed'" :class="filter === 'closed' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">已关闭</button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>
    <div v-else-if="filtered().length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">暂无数据</div>

    <div v-else class="space-y-3">
      <div v-for="j in filtered()" :key="j.id" class="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between shadow-sm">
        <div>
          <div class="flex items-center gap-2">
            <p class="text-sm font-medium text-gray-800">{{ j.title }}</p>
            <span class="px-1.5 py-0.5 text-xs rounded" :class="j.status === 'PUBLISHED' ? 'bg-green-50 text-green-600' : j.status === 'CLOSED' ? 'bg-gray-100 text-gray-500' : 'bg-yellow-50 text-yellow-600'">
              {{ j.status === 'PUBLISHED' ? '已发布' : j.status === 'CLOSED' ? '已关闭' : '草稿' }}
            </span>
          </div>
          <p class="text-xs text-gray-400 mt-0.5">{{ j.companyName || '未知公司' }} · {{ j.city || '-' }} · {{ j.salaryMin }}K-{{ j.salaryMax }}K · 浏览 {{ j.viewCount || 0 }} · 投递 {{ j.applyCount || 0 }}</p>
        </div>
        <button v-if="j.status === 'PUBLISHED'" @click="closeJob(j.id)" class="px-3 py-1.5 text-xs text-red-600 border border-red-200 rounded-md hover:bg-red-50 transition-colors">下架</button>
      </div>
    </div>
  </div>
</template>
