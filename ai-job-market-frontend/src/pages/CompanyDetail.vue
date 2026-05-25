<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../api.js'

const route = useRoute()
const router = useRouter()
const company = ref(null)
const loading = ref(true)
const reviewForm = ref({ rating: 5, title: '', content: '', pros: '', cons: '' })
const submitting = ref(false)

onMounted(async () => {
  try {
    const res = await fetch('/api/companies/' + route.params.id)
    const data = await res.json()
    company.value = data.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

async function submitReview() {
  submitting.value = true
  try {
    const res = await fetch('/api/companies/' + route.params.id + '/reviews', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
      body: JSON.stringify(reviewForm.value)
    })
    const data = await res.json()
    if (data.code === 0) {
      reviewForm.value = { rating: 5, title: '', content: '', pros: '', cons: '' }
      alert('评价提交成功，等待审核')
    } else { alert(data.message) }
  } catch (e) { alert('提交失败') }
  finally { submitting.value = false }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div v-if="loading" class="text-center py-12">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <template v-else-if="company">
      <button @click="router.back()" class="text-sm text-gray-500 hover:text-blue-600 mb-4">&larr; 返回</button>

      <!-- Header -->
      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
        <div class="flex items-start gap-4">
          <div class="w-16 h-16 bg-gray-100 rounded-xl flex items-center justify-center text-gray-500 text-2xl font-bold flex-shrink-0">
            {{ company.name?.charAt(0) }}
          </div>
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <h1 class="text-xl font-bold text-gray-900">{{ company.name }}</h1>
              <span v-if="company.verified" class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full font-medium">已认证</span>
            </div>
            <p class="text-sm text-gray-500 mb-2">{{ company.shortName }}</p>
            <div class="flex flex-wrap gap-3 text-xs text-gray-500">
              <span>{{ company.industry || '-' }}</span>
              <span>{{ company.scale || '-' }}</span>
              <span>{{ company.stage || '-' }}</span>
              <a v-if="company.website" :href="company.website" target="_blank" class="text-blue-600">官网</a>
              <span>{{ company.address || '-' }}</span>
            </div>
            <div class="flex items-center gap-4 mt-3 text-sm text-gray-500">
              <span>{{ company.jobCount || 0 }} 个在招职位</span>
              <span v-if="company.avgRating" class="text-yellow-600">★ {{ company.avgRating.toFixed(1) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="space-y-6">
        <section v-if="company.description" class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">公司介绍</h2>
          <p class="text-sm text-gray-600 leading-relaxed">{{ company.description }}</p>
        </section>

        <section v-if="company.culture" class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">企业文化</h2>
          <p class="text-sm text-gray-600 leading-relaxed">{{ company.culture }}</p>
        </section>

        <section v-if="company.welfare" class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">福利待遇</h2>
          <div class="flex flex-wrap gap-2">
            <span v-for="(w, i) in (() => { try { return JSON.parse(company.welfare) } catch { return [company.welfare] } })()" :key="i"
              class="px-3 py-1.5 bg-green-50 text-green-700 text-xs rounded-md">{{ w }}</span>
          </div>
        </section>

        <!-- Reviews -->
        <section class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">公司评价 ({{ company.reviews?.length || 0 }})</h2>

          <div v-for="r in company.reviews" :key="r.id" class="border-b border-gray-100 pb-4 mb-4 last:border-0 last:pb-0 last:mb-0">
            <div class="flex items-center justify-between mb-1">
              <span class="text-sm font-medium text-gray-800">{{ r.title || '无标题' }}</span>
              <span class="text-sm text-yellow-500">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
            </div>
            <p class="text-sm text-gray-600 mb-2">{{ r.content }}</p>
            <div class="flex gap-4 text-xs text-gray-400">
              <span v-if="r.pros">优点：{{ r.pros }}</span>
              <span v-if="r.cons">缺点：{{ r.cons }}</span>
            </div>
          </div>
          <p v-if="!company.reviews?.length" class="text-sm text-gray-400">暂无评价</p>
        </section>

        <!-- Add Review -->
        <section class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-4">写评价</h2>
          <div class="space-y-3">
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500">评分：</span>
              <button v-for="s in 5" :key="s" @click="reviewForm.rating = s"
                class="text-lg" :class="s <= reviewForm.rating ? 'text-yellow-500' : 'text-gray-300'">★</button>
            </div>
            <input v-model="reviewForm.title" placeholder="评价标题" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
            <textarea v-model="reviewForm.content" rows="3" placeholder="评价内容" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
            <div class="grid grid-cols-2 gap-2">
              <input v-model="reviewForm.pros" placeholder="优点" class="px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
              <input v-model="reviewForm.cons" placeholder="缺点" class="px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
            </div>
            <button @click="submitReview" :disabled="submitting" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors disabled:bg-blue-300">
              {{ submitting ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>
