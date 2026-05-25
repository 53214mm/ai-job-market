<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const apps = ref([])
const loading = ref(true)
const filter = ref('')

const statusMap = { APPLIED:'已投递', VIEWED:'已查看', SCREENING:'筛选中', INTERVIEW:'面试中', OFFER:'已发Offer', HIRED:'已录用', REJECTED:'已拒绝', WITHDRAWN:'已撤回' }
const statusColor = { APPLIED:'text-blue-600 bg-blue-50', VIEWED:'text-gray-600 bg-gray-100', SCREENING:'text-yellow-600 bg-yellow-50', INTERVIEW:'text-purple-600 bg-purple-50', OFFER:'text-green-600 bg-green-50', HIRED:'text-green-600 bg-green-50', REJECTED:'text-red-600 bg-red-50', WITHDRAWN:'text-gray-400 bg-gray-100' }

async function fetch() {
  loading.value = true
  try {
    const params = new URLSearchParams({ current:'1', size:'50' })
    if (filter.value) params.set('status', filter.value)
    const res = await fetch('/api/applications/my?' + params, {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    apps.value = data.data?.records || []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function withdraw(id) {
  if (!confirm('确定撤回此投递？')) return
  await fetch('/api/applications/' + id + '/status', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
    body: JSON.stringify({ status: 'WITHDRAWN' })
  })
  fetch()
}

function changeFilter(f) { filter.value = f; fetch() }
onMounted(fetch)
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">我的投递</h1>

    <div class="flex flex-wrap gap-2 mb-6">
      <button v-for="(label,key) in { '':'全部', APPLIED:'已投递', VIEWED:'已查看', SCREENING:'筛选中', INTERVIEW:'面试中', OFFER:'Offer', HIRED:'已录用', REJECTED:'已拒绝' }" :key="key" @click="changeFilter(key)"
        class="px-3 py-1.5 text-xs rounded-full font-medium transition-colors"
        :class="filter === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">{{ label }}</button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>
    <div v-else-if="apps.length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">暂无投递记录</div>

    <div v-else class="space-y-3">
      <div v-for="a in apps" :key="a.id" @click="router.push('/applications/' + a.id)"
        class="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md cursor-pointer transition-all">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="text-sm font-semibold text-gray-900">{{ a.jobTitle }}</h3>
              <span class="px-1.5 py-0.5 text-xs rounded-full font-medium" :class="statusColor[a.status]">{{ statusMap[a.status] || a.status }}</span>
            </div>
            <p class="text-xs text-gray-500">{{ a.companyName }} · 投递于 {{ (a.createdAt||'').slice(0,10) }}</p>
          </div>
          <div class="flex items-center gap-3">
            <span v-if="a.aiMatchScore" class="text-xs px-2 py-0.5 bg-green-50 text-green-600 rounded-full font-medium">AI匹配 {{ a.aiMatchScore }}%</span>
            <button v-if="a.status === 'APPLIED'" @click.stop="withdraw(a.id)" class="text-xs text-red-400 hover:text-red-600">撤回</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
