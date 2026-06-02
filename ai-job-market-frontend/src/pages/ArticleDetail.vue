<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)

async function loadArticle() {
  try {
    const res = await fetch('/api/articles/' + route.params.id)
    const d = await res.json()
    if (d.code === 0) article.value = d.data
    else article.value = null
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadArticle)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div v-if="loading" class="text-center py-20">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <div v-else-if="!article" class="text-center py-20 text-gray-400">
      <p class="text-lg">文章不存在或已下架</p>
      <button @click="router.push('/articles')" class="mt-4 text-blue-600 hover:underline text-sm">返回文章列表</button>
    </div>

    <template v-else>
      <button @click="router.push('/articles')" class="text-sm text-gray-500 hover:text-blue-600 mb-6 inline-block">&larr; 返回列表</button>

      <article class="bg-white border border-gray-200 rounded-xl p-6 sm:p-8 shadow-sm">
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 mb-4">{{ article.title }}</h1>

        <div class="flex items-center gap-4 text-sm text-gray-400 mb-6 pb-6 border-b border-gray-100">
          <span>{{ (article.publishedAt || article.createdAt || '').slice(0, 10) }} 发布</span>
          <span>{{ article.viewCount || 0 }} 次阅读</span>
          <span v-if="article.tags" class="text-blue-500">{{ article.tags }}</span>
        </div>

        <div v-if="article.summary" class="p-4 bg-gray-50 rounded-lg text-sm text-gray-600 mb-6 leading-relaxed">
          {{ article.summary }}
        </div>

        <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed whitespace-pre-wrap">
          {{ article.content }}
        </div>
      </article>
    </template>
  </div>
</template>
