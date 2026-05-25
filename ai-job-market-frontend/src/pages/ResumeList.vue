<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { resumeApi } from '../api.js'
import ResumeCard from '../components/ResumeCard.vue'

const router = useRouter()
const resumes = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await resumeApi.list({ current: 1, pageSize: 20 })
    resumes.value = data.records || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

function goCreate() { router.push('/resumes/create') }
function goDetail(id) { router.push('/resumes/' + id) }
function goEdit(id) { router.push('/resumes/' + id + '/edit') }

async function handleDelete(id) {
  if (!confirm('确定删除此简历？所有子项将一并删除。')) return
  try {
    await resumeApi.delete(id)
    resumes.value = resumes.value.filter(r => r.id !== id)
  } catch (e) { alert(e.message) }
}

async function handleSetDefault(id) {
  try {
    await resumeApi.setDefault(id)
    resumes.value.forEach(r => r.isDefault = (r.id === id))
  } catch (e) { alert(e.message) }
}

async function handleAiAnalyze(id) {
  try {
    const result = await resumeApi.aiAnalyze(id)
    alert('AI 分析完成！综合评分：' + result.overallScore)
    router.push('/resumes/' + id + '/analysis')
  } catch (e) { alert(e.message) }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的简历</h1>
        <p class="text-sm text-gray-500 mt-1">管理您的多份简历，最多可创建 5 份</p>
      </div>
      <button @click="goCreate"
        class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors">
        + 创建新简历
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
      <p class="text-sm text-gray-500 mt-3">加载中...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="resumes.length === 0" class="text-center py-16 bg-gray-50 rounded-xl border border-dashed border-gray-300">
      <div class="w-16 h-16 mx-auto bg-gray-100 rounded-full flex items-center justify-center mb-4">
        <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>
      <h3 class="text-base font-medium text-gray-900 mb-1">还没有简历</h3>
      <p class="text-sm text-gray-500 mb-4">创建一份简历，开启您的求职之旅</p>
      <button @click="goCreate"
        class="px-6 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors">
        创建第一份简历
      </button>
    </div>

    <!-- Resume List -->
    <div v-else class="space-y-4">
      <ResumeCard
        v-for="r in resumes" :key="r.id" :resume="r"
        @view="goDetail" @edit="goEdit"
        @delete="handleDelete" @set-default="handleSetDefault"
      />
    </div>

    <!-- AI Score hint -->
    <div v-if="resumes.some(r => !r.aiScore)" class="mt-6 p-4 bg-blue-50 border border-blue-100 rounded-lg flex items-center gap-3">
      <div class="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center flex-shrink-0">
        <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      </div>
      <div>
        <p class="text-sm text-blue-700 font-medium">AI 简历分析</p>
        <p class="text-xs text-blue-600">让 AI 帮您分析简历，从 HR 视角给出评分和优化建议。查看简历详情后点击"AI 分析"即可。</p>
      </div>
    </div>
  </div>
</template>
