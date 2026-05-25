<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import SearchBar from '../components/SearchBar.vue'
import JobCard from '../components/JobCard.vue'

const router = useRouter()
const jobs = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const searchParams = ref({ keyword: '', city: '', salary: '', sortBy: 'time' })
const tab = ref('all') // all | my
const role = computed(() => JSON.parse(localStorage.getItem('user') || '{}').role || '')

async function fetchJobs() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value, pageSize: pageSize.value,
      sortBy: searchParams.value.sortBy
    }
    if (searchParams.value.keyword) params.keyword = searchParams.value.keyword
    if (searchParams.value.city) params.city = searchParams.value.city
    if (searchParams.value.salary) {
      const [lo, hi] = searchParams.value.salary.split('-')
      if (lo) params.salaryMin = parseInt(lo)
      if (hi) params.salaryMax = parseInt(hi)
    }
    const url = tab.value === 'my' ? '/api/jobs/my?' : '/api/jobs?'
    const res = await fetch(url + new URLSearchParams(params), {
      headers: localStorage.getItem('token') ? { 'Authorization': 'Bearer ' + localStorage.getItem('token') } : {}
    })
    const data = await res.json()
    if (data.code === 0) {
      jobs.value = (data.data.records || []).map(j => ({
        id: j.id, title: j.title, company: j.companyName || '未知公司', city: j.city || '未指定',
        salary: (j.salaryMin || j.salaryMax) ? (j.salaryMin || '') + 'K-' + (j.salaryMax || '') + 'K' : '薪资面议',
        experience: j.experienceLevel || '不限', tags: ((j.tags || '') + ',' + (j.skillsRequired || '')).split(/[,，]/).filter(Boolean).slice(0, 4),
        matchScore: j.matchScore || null, postedTime: j.publishedAt ? j.publishedAt.slice(0, 10) : (j.createdAt || '').slice(0, 10),
        status: j.status, viewCount: j.viewCount, applyCount: j.applyCount,
        companyVerified: j.companyVerified
      }))
      total.value = data.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function handleSearch(params) {
  searchParams.value = { ...searchParams.value, ...params }
  currentPage.value = 1
  fetchJobs()
}

function changePage(p) { currentPage.value = p; fetchJobs() }
function switchTab(t) { tab.value = t; currentPage.value = 1; fetchJobs() }
onMounted(fetchJobs)
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">职位搜索</h1>
      <div class="flex items-center gap-3">
        <div v-if="role === 'RECRUITER'" class="flex border border-gray-200 rounded-md overflow-hidden">
          <button @click="switchTab('all')" :class="tab === 'all' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">全部职位</button>
          <button @click="switchTab('my')" :class="tab === 'my' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">我的职位</button>
        </div>
        <button v-if="role === 'RECRUITER'" @click="router.push('/recruiter/jobs')" class="px-3 py-1.5 bg-blue-600 text-white text-xs rounded-md hover:bg-blue-700 transition-colors">发布新职位</button>
      </div>
    </div>

    <SearchBar @search="handleSearch" />

    <!-- Sort & Count -->
    <div class="flex items-center justify-between mt-6 mb-4">
      <p class="text-sm text-gray-500">共找到 <span class="font-semibold text-gray-900">{{ total }}</span> 个职位</p>
      <div class="flex items-center gap-2">
        <span class="text-xs text-gray-500">排序：</span>
        <select v-model="searchParams.sortBy" @change="fetchJobs" class="text-sm border border-gray-200 rounded-md px-3 py-1.5 focus:ring-2 focus:ring-blue-500">
          <option value="time">发布时间</option>
          <option value="salary">薪资高低</option>
        </select>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="jobs.length === 0" class="text-center py-16 bg-gray-50 rounded-xl border border-dashed border-gray-300">
      <p class="text-gray-500">{{ tab === 'my' ? '尚未发布职位' : '没有找到匹配的职位' }}</p>
    </div>

    <!-- Job Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <template v-for="job in jobs" :key="job.id">
        <div class="relative">
          <JobCard :job="job" @click="router.push('/jobs/' + job.id)" />
          <div v-if="tab === 'my'" class="absolute top-3 right-3 flex items-center gap-1">
            <span class="px-2 py-0.5 text-xs rounded-full"
              :class="job.status === 'PUBLISHED' ? 'bg-green-50 text-green-600' : job.status === 'CLOSED' ? 'bg-gray-100 text-gray-500' : 'bg-yellow-50 text-yellow-600'">
              {{ job.status === 'PUBLISHED' ? '已发布' : job.status === 'CLOSED' ? '已关闭' : '草稿' }}
            </span>
          </div>
        </div>
      </template>
    </div>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="flex items-center justify-center gap-2 mt-8">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="px-3 py-1.5 border border-gray-200 rounded-md text-sm text-gray-500 hover:bg-gray-50 disabled:opacity-30 transition-colors">上一页</button>
      <button v-for="p in Math.min(5, Math.ceil(total / pageSize))" :key="p" @click="changePage(p)" class="w-8 h-8 rounded-md text-sm font-medium transition-colors" :class="p === currentPage ? 'bg-blue-600 text-white' : 'border border-gray-200 text-gray-600 hover:bg-gray-50'">{{ p }}</button>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= Math.ceil(total / pageSize)" class="px-3 py-1.5 border border-gray-200 rounded-md text-sm text-gray-500 hover:bg-gray-50 disabled:opacity-30 transition-colors">下一页</button>
    </div>
  </div>
</template>
