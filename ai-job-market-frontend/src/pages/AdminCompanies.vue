<script setup>
import { ref, onMounted } from 'vue'
const companies = ref([])
const loading = ref(true)
const filter = ref('all')

async function fetchCompanies() {
  loading.value = true
  try {
    const res = await fetch('/api/companies?current=1&pageSize=50', {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) companies.value = data.data.records || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function verify(id, verified) {
  try {
    const res = await fetch('/api/companies/' + id + '/verify?verified=' + verified, {
      method: 'PUT', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) {
      const c = companies.value.find(x => x.id === id)
      if (c) c.verified = verified
    } else { alert(data.message) }
  } catch (e) { alert('操作失败') }
}

const filtered = () => {
  if (filter.value === 'unverified') return companies.value.filter(c => !c.verified)
  if (filter.value === 'verified') return companies.value.filter(c => c.verified)
  return companies.value
}

onMounted(fetchCompanies)
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">企业审核</h1>
      <div class="flex border border-gray-200 rounded-md overflow-hidden">
        <button @click="filter = 'all'" :class="filter === 'all' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">全部</button>
        <button @click="filter = 'unverified'" :class="filter === 'unverified' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">待审核</button>
        <button @click="filter = 'verified'" :class="filter === 'verified' ? 'bg-red-600 text-white' : 'bg-white text-gray-600'" class="px-3 py-1.5 text-xs font-medium transition-colors">已认证</button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>
    <div v-else-if="filtered().length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">暂无数据</div>

    <div v-else class="space-y-3">
      <div v-for="c in filtered()" :key="c.id" class="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-500 font-bold">{{ c.name?.charAt(0) }}</div>
          <div>
            <div class="flex items-center gap-2">
              <p class="text-sm font-medium text-gray-800">{{ c.name }}</p>
              <span v-if="c.verified" class="px-1.5 py-0.5 bg-green-50 text-green-600 text-xs rounded">已认证</span>
              <span v-else class="px-1.5 py-0.5 bg-orange-50 text-orange-600 text-xs rounded">待审核</span>
            </div>
            <p class="text-xs text-gray-400">{{ c.industry || '-' }} · {{ c.scale || '-' }} · {{ c.stage || '-' }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button v-if="!c.verified" @click="verify(c.id, true)" class="px-3 py-1.5 bg-green-600 text-white text-xs rounded-md hover:bg-green-700 transition-colors">审核通过</button>
          <button v-if="c.verified" @click="verify(c.id, false)" class="px-3 py-1.5 bg-orange-500 text-white text-xs rounded-md hover:bg-orange-600 transition-colors">取消认证</button>
        </div>
      </div>
    </div>
  </div>
</template>
