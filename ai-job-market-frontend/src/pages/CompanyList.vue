<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const companies = ref([])
const loading = ref(true)
const keyword = ref('')
const industry = ref('')

const industries = ['互联网', '金融', '教育', '医疗', '人工智能', '电商', '游戏', '硬件']

async function fetchCompanies() {
  loading.value = true
  try {
    const params = new URLSearchParams({ current: 1, pageSize: 20 })
    if (keyword.value) params.set('keyword', keyword.value)
    if (industry.value) params.set('industry', industry.value)
    const res = await fetch('/api/companies?' + params)
    const data = await res.json()
    companies.value = data.data?.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(fetchCompanies)
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">公司列表</h1>

    <!-- Search -->
    <div class="flex flex-col sm:flex-row gap-3 mb-6">
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
        <input v-model="keyword" @keyup.enter="fetchCompanies" placeholder="搜索公司名称"
          class="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
      </div>
      <select v-model="industry" @change="fetchCompanies"
        class="px-3 py-2.5 border border-gray-200 rounded-md text-sm text-gray-700 focus:ring-2 focus:ring-blue-500">
        <option value="">全部行业</option>
        <option v-for="ind in industries" :key="ind" :value="ind">{{ ind }}</option>
      </select>
      <button @click="fetchCompanies" class="px-4 py-2.5 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors">搜索</button>
    </div>

    <!-- List -->
    <div v-if="loading" class="text-center py-12"><div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div></div>
    <div v-else-if="companies.length === 0" class="text-center py-16 bg-gray-50 rounded-xl border border-dashed border-gray-300 text-gray-500">暂无公司</div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="c in companies" :key="c.id"
        @click="router.push('/companies/' + c.id)"
        class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md hover:border-blue-200 transition-all cursor-pointer">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center text-gray-500 text-lg font-bold">{{ c.name?.charAt(0) }}</div>
          <div class="flex-1 min-w-0">
            <h3 class="text-base font-semibold text-gray-900 truncate">{{ c.name }}</h3>
            <p class="text-xs text-gray-500">{{ c.industry || '-' }} · {{ c.scale || '-' }}</p>
          </div>
          <span v-if="c.verified" class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full font-medium">已认证</span>
        </div>
        <p class="text-xs text-gray-400 line-clamp-2 mb-3">{{ c.description || '暂无介绍' }}</p>
        <div class="flex items-center justify-between text-xs text-gray-500">
          <span>{{ c.jobCount || 0 }} 个在招职位</span>
          <span v-if="c.avgRating" class="text-yellow-600">★ {{ c.avgRating.toFixed(1) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
