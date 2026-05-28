<script setup>
import { ref, onMounted } from 'vue'
const favs = ref([]); const loading = ref(true)

async function loadFavs() {
  loading.value = true
  try {
    const res = await fetch('/api/favorites', {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    favs.value = data.data || []
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function remove(id) {
  await fetch('/api/favorites/' + id, {
    method: 'DELETE',
    headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
  })
  favs.value = favs.value.filter(f => f.id !== id)
}

onMounted(loadFavs)
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">我的收藏</h1>
    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>
    <div v-else-if="favs.length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">暂无收藏</div>
    <div v-else class="space-y-3">
      <div v-for="f in favs" :key="f.id" class="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between shadow-sm">
        <div>
          <p class="text-sm font-medium text-gray-800">{{ f.targetTitle }}</p>
          <p class="text-xs text-gray-400" v-if="f.targetCompany">{{ f.targetCompany }} · {{ f.targetCity || '-' }} · {{ f.targetSalaryMax || '-' }}K</p>
          <p class="text-xs text-gray-400" v-else>收藏于 {{ (f.createdAt||'').slice(0,10) }}</p>
        </div>
        <button @click="remove(f.id)" class="text-xs text-red-400 hover:text-red-600">取消收藏</button>
      </div>
    </div>
  </div>
</template>
