<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const articles = ref([])
const categories = ref([])
const loading = ref(true)
const currentPage = ref(1)
const total = ref(0)
const categoryId = ref(null)
const keyword = ref('')

async function fetchArticles() {
  loading.value = true
  try {
    const params = new URLSearchParams({ current: currentPage.value, size: 10 })
    if (categoryId.value) params.set('categoryId', categoryId.value)
    if (keyword.value) params.set('keyword', keyword.value)
    const res = await fetch('/api/articles?' + params)
    const d = await res.json()
    if (d.code === 0) {
      articles.value = d.data?.records || []
      total.value = d.data?.total || 0
    }
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function fetchCategories() {
  try {
    const res = await fetch('/api/articles/categories')
    const d = await res.json()
    if (d.code === 0) categories.value = d.data || []
  } catch(e) {}
}

function selectCat(id) { categoryId.value = id; currentPage.value = 1; fetchArticles() }
function search() { currentPage.value = 1; fetchArticles() }
function changePage(p) { currentPage.value = p; fetchArticles(); window.scrollTo(0, 0) }

onMounted(() => { fetchCategories(); fetchArticles() })
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">求职攻略</h1>

    <!-- Categories -->
    <div class="flex flex-wrap items-center gap-2 mb-6">
      <button @click="selectCat(null)"
        class="px-3 py-1.5 text-xs rounded-full font-medium transition-colors"
        :class="!categoryId ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">全部</button>
      <button v-for="c in categories" :key="c.id" @click="selectCat(c.id)"
        class="px-3 py-1.5 text-xs rounded-full font-medium transition-colors"
        :class="categoryId === c.id ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">{{ c.name }}</button>
    </div>

    <!-- Search -->
    <div class="flex gap-2 mb-6">
      <input v-model="keyword" @keyup.enter="search" placeholder="搜索文章..."
        class="flex-1 px-4 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      <button @click="search" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">搜索</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="articles.length === 0" class="text-center py-16 bg-gray-50 rounded-xl text-gray-400">
      暂无文章
    </div>

    <!-- Article List -->
    <div v-else class="space-y-4">
      <div v-for="a in articles" :key="a.id" @click="router.push('/articles/' + a.id)"
        class="bg-white border border-gray-200 rounded-lg p-5 cursor-pointer hover:shadow-md hover:border-blue-200 transition-all">
        <div class="flex gap-4">
          <div v-if="a.coverUrl" class="w-24 h-16 flex-shrink-0 rounded-lg overflow-hidden bg-gray-100">
            <img :src="a.coverUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <h3 class="text-base font-semibold text-gray-900 mb-1">{{ a.title }}</h3>
            <p class="text-sm text-gray-500 line-clamp-2 mb-2">{{ a.summary || (a.content || '').slice(0, 120) }}</p>
            <div class="flex items-center gap-3 text-xs text-gray-400">
              <span>{{ (a.publishedAt || a.createdAt || '').slice(0, 10) }}</span>
              <span>{{ a.viewCount || 0 }} 次阅读</span>
              <span v-if="a.tags" class="text-blue-500">{{ a.tags }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > 10" class="flex items-center justify-center gap-2 mt-8">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1"
        class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-30">上一页</button>
      <span class="px-3 py-1.5 text-sm text-gray-500">{{ currentPage }} / {{ Math.ceil(total / 10) }}</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= Math.ceil(total / 10)"
        class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-30">下一页</button>
    </div>
  </div>
</template>
