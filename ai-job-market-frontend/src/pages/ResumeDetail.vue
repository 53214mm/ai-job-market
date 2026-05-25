<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resumeApi } from '../api.js'

const route = useRoute()
const router = useRouter()
const resume = ref(null)
const analysis = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    resume.value = await resumeApi.detail(route.params.id)
  } catch (e) { alert(e.message); router.push('/resumes') }
  finally { loading.value = false }
})

async function handleAiAnalyze() {
  try {
    analysis.value = await resumeApi.aiAnalyze(route.params.id)
    resume.value.aiScore = analysis.value.overallScore
  } catch (e) { alert(e.message) }
}

async function handleExportPdf() {
  window.open(resumeApi.exportPdf(route.params.id), '_blank')
}

function goEdit() {
  router.push('/resumes/' + route.params.id + '/edit')
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <template v-else-if="resume">
      <!-- Top bar -->
      <div class="flex items-center justify-between mb-6">
        <button @click="router.push('/resumes')" class="text-sm text-gray-500 hover:text-blue-600 flex items-center gap-1">
          &larr; 返回列表
        </button>
        <div class="flex items-center gap-2">
          <button @click="handleAiAnalyze" class="px-3 py-1.5 text-xs font-medium bg-blue-50 text-blue-600 rounded-md hover:bg-blue-100 transition-colors">
            AI 分析
          </button>
          <button @click="handleExportPdf" class="px-3 py-1.5 text-xs font-medium border border-gray-200 text-gray-600 rounded-md hover:bg-gray-50 transition-colors">
            导出 PDF
          </button>
          <button @click="goEdit" class="px-3 py-1.5 text-xs font-medium bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
            编辑
          </button>
        </div>
      </div>

      <!-- Resume Content -->
      <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <!-- Header -->
        <div class="px-6 py-5 bg-gradient-to-r from-blue-50 to-white border-b border-gray-100">
          <div class="flex items-start justify-between">
            <div>
              <h1 class="text-xl font-bold text-gray-900">{{ resume.fullName }}</h1>
              <p class="text-sm text-gray-500 mt-1">{{ resume.title }}</p>
              <div class="flex items-center gap-3 mt-2 text-sm text-gray-500">
                <span v-if="resume.phone">{{ resume.phone }}</span>
                <span v-if="resume.email">{{ resume.email }}</span>
              </div>
            </div>
            <div v-if="resume.aiScore" class="text-center">
              <div class="w-16 h-16 rounded-full bg-blue-50 border-4 border-blue-100 flex items-center justify-center">
                <span class="text-xl font-bold text-blue-600">{{ resume.aiScore }}</span>
              </div>
              <p class="text-xs text-gray-400 mt-1">AI 评分</p>
            </div>
          </div>
        </div>

        <div class="p-6 space-y-6">
          <!-- Basic Info -->
          <section>
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">基本信息</h2>
            <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm">
              <div><span class="text-gray-400">现居：</span>{{ resume.currentCity || '-' }}</div>
              <div><span class="text-gray-400">期望城市：</span>{{ resume.expectedCity || '-' }}</div>
              <div><span class="text-gray-400">求职状态：</span>{{ resume.jobStatus || '-' }}</div>
              <div><span class="text-gray-400">期望薪资：</span>{{ resume.expectedSalaryMin || '-' }}K - {{ resume.expectedSalaryMax || '-' }}K</div>
            </div>
          </section>

          <!-- Summary -->
          <section v-if="resume.summary">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">自我评价</h2>
            <p class="text-sm text-gray-600 leading-relaxed">{{ resume.summary }}</p>
          </section>

          <!-- Education -->
          <section v-if="resume.educations?.length">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">教育经历</h2>
            <div v-for="e in resume.educations" :key="e.id" class="mb-3 last:mb-0">
              <div class="flex items-start justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-800">{{ e.schoolName }}</p>
                  <p class="text-sm text-gray-500">{{ e.degree }} · {{ e.major }}</p>
                </div>
                <span class="text-xs text-gray-400">{{ e.startDate }} ~ {{ e.endDate || '至今' }}</span>
              </div>
              <p v-if="e.description" class="text-sm text-gray-500 mt-1">{{ e.description }}</p>
            </div>
          </section>

          <!-- Work Experience -->
          <section v-if="resume.workExperiences?.length">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">工作经历</h2>
            <div v-for="w in resume.workExperiences" :key="w.id" class="mb-4 last:mb-0 border-l-2 border-gray-100 pl-4">
              <div class="flex items-start justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-800">{{ w.companyName }}</p>
                  <p class="text-sm text-gray-500">{{ w.position }}</p>
                </div>
                <span class="text-xs text-gray-400">{{ w.startDate }} ~ {{ w.endDate || '至今' }}</span>
              </div>
              <p v-if="w.description" class="text-sm text-gray-500 mt-1">{{ w.description }}</p>
              <p v-if="w.achievement" class="text-sm text-gray-600 mt-1">
                <span class="font-medium">成果：</span>{{ w.achievement }}
              </p>
              <div v-if="w.skillsUsed" class="flex flex-wrap gap-1 mt-2">
                <span v-for="s in w.skillsUsed.split(',')" :key="s" class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded">{{ s.trim() }}</span>
              </div>
            </div>
          </section>

          <!-- Projects -->
          <section v-if="resume.projects?.length">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">项目经历</h2>
            <div v-for="p in resume.projects" :key="p.id" class="mb-3 last:mb-0 border-l-2 border-gray-100 pl-4">
              <div class="flex items-start justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-800">{{ p.projectName }}</p>
                  <p class="text-sm text-gray-500" v-if="p.role">{{ p.role }}</p>
                </div>
                <span class="text-xs text-gray-400">{{ p.startDate }} ~ {{ p.endDate || '至今' }}</span>
              </div>
              <p v-if="p.description" class="text-sm text-gray-500 mt-1">{{ p.description }}</p>
              <div v-if="p.technologies" class="flex flex-wrap gap-1 mt-2">
                <span v-for="t in p.technologies.split(',')" :key="t" class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded">{{ t.trim() }}</span>
              </div>
            </div>
          </section>

          <!-- Skills -->
          <section v-if="resume.skills?.length">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">技能</h2>
            <div class="flex flex-wrap gap-2">
              <div v-for="s in resume.skills" :key="s.id"
                class="px-3 py-1.5 border border-gray-200 rounded-md text-sm">
                <span class="font-medium text-gray-800">{{ s.skillName }}</span>
                <span v-if="s.proficiency" class="text-gray-400 ml-1">· {{ s.proficiency }}</span>
                <span v-if="s.monthsOfUse" class="text-gray-400 ml-1">· {{ s.monthsOfUse }}个月</span>
              </div>
            </div>
          </section>

          <!-- Certificates -->
          <section v-if="resume.certificates?.length">
            <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">证书</h2>
            <div class="space-y-2">
              <div v-for="c in resume.certificates" :key="c.id" class="flex items-center justify-between text-sm">
                <span class="font-medium text-gray-800">{{ c.certName }}</span>
                <span class="text-gray-400">{{ c.issuingOrg }}</span>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- AI Analysis Result -->
      <div v-if="analysis" class="mt-6 bg-white border border-blue-100 rounded-xl p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
          AI 分析报告
        </h2>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
          <div class="text-center p-3 bg-blue-50 rounded-lg">
            <p class="text-2xl font-bold text-blue-600">{{ analysis.overallScore }}</p>
            <p class="text-xs text-gray-500 mt-1">综合评分</p>
          </div>
          <div class="text-center p-3 bg-gray-50 rounded-lg">
            <p class="text-xl font-bold text-gray-700">{{ analysis.formatScore }}</p>
            <p class="text-xs text-gray-500 mt-1">格式</p>
          </div>
          <div class="text-center p-3 bg-gray-50 rounded-lg">
            <p class="text-xl font-bold text-gray-700">{{ analysis.contentScore }}</p>
            <p class="text-xs text-gray-500 mt-1">内容</p>
          </div>
          <div class="text-center p-3 bg-gray-50 rounded-lg">
            <p class="text-xl font-bold text-gray-700">{{ analysis.keywordScore }}</p>
            <p class="text-xs text-gray-500 mt-1">关键词</p>
          </div>
        </div>
        <div class="space-y-4 text-sm">
          <div v-if="analysis.strengths" class="p-3 bg-green-50 rounded-lg">
            <p class="font-medium text-green-700 mb-1">优势</p>
            <p class="text-green-600">{{ analysis.strengths }}</p>
          </div>
          <div v-if="analysis.weaknesses" class="p-3 bg-yellow-50 rounded-lg">
            <p class="font-medium text-yellow-700 mb-1">待改进</p>
            <p class="text-yellow-600">{{ analysis.weaknesses }}</p>
          </div>
          <div v-if="analysis.suggestions" class="p-3 bg-blue-50 rounded-lg">
            <p class="font-medium text-blue-700 mb-1">优化建议</p>
            <p class="text-blue-600">{{ analysis.suggestions }}</p>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
