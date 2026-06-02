<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const job = ref(null)
const loading = ref(true)
const applyLoading = ref(false)

const token = () => localStorage.getItem('token')
const h = () => ({ 'Authorization': 'Bearer ' + token(), 'Content-Type': 'application/json' })
const role = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || '{}').role || '' }
  catch(e) { return '' }
})

async function loadJob() {
  loading.value = true
  try {
    const res = await fetch('/api/jobs/' + route.params.id)
    const d = await res.json()
    if (d.code === 0) job.value = d.data
  } catch(e) { console.error(e) }
  finally { loading.value = false }
}

async function handleApply() {
  if (!token()) { router.push('/login'); return }
  applyLoading.value = true
  try {
    const resumeRes = await fetch('/api/resumes?current=1&pageSize=1', { headers: h() })
    const resumeData = await resumeRes.json()
    const resumeId = resumeData.data?.records?.[0]?.id
    if (!resumeId) { alert('请先创建一份简历'); router.push('/resumes/create'); return }
    const res = await fetch('/api/applications', {
      method: 'POST', headers: h(),
      body: JSON.stringify({ jobId: job.value.id, resumeId })
    })
    const d = await res.json()
    if (d.code === 0) alert('投递成功！')
    else alert(d.message || '投递失败')
  } catch(e) { alert('投递失败') }
  finally { applyLoading.value = false }
}

async function handleFavorite() {
  if (!token()) { router.push('/login'); return }
  try {
    const res = await fetch('/api/favorites', {
      method: 'POST', headers: h(),
      body: JSON.stringify({ targetType: 'JOB', targetId: job.value.id })
    })
    const d = await res.json()
    if (d.code === 0) alert('已收藏！')
    else alert(d.message || '收藏失败')
  } catch(e) { alert('收藏失败') }
}

function fmtSalary(j) {
  if (!j.salaryMin && !j.salaryMax) return '薪资面议'
  return (j.salaryMin || '') + 'K-' + (j.salaryMax || '') + 'K' + (j.salaryMonths ? '·' + j.salaryMonths + '薪' : '')
}

function fmtTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit' })
}

onMounted(loadJob)
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <!-- Loading -->
    <div v-if="loading" class="text-center py-20">
      <div class="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
    </div>

    <!-- Not Found -->
    <div v-else-if="!job" class="text-center py-20 text-gray-400">
      <p class="text-lg">职位不存在或已下线</p>
      <button @click="router.push('/jobs')" class="mt-4 text-blue-600 hover:underline text-sm">返回职位搜索</button>
    </div>

    <!-- Job Detail -->
    <template v-else>
      <!-- Header -->
      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ job.title }}</h1>
            <div class="flex items-center gap-3 text-sm text-gray-500 mb-4">
              <router-link :to="'/companies/' + job.companyId" class="text-blue-600 font-medium hover:underline">{{ job.companyName }}</router-link>
              <span v-if="job.companyVerified" class="px-1.5 py-0.5 bg-green-50 text-green-600 text-[10px] rounded">已认证</span>
              <span>{{ job.city }}</span>
              <span v-if="job.district">{{ job.district }}</span>
              <span class="text-red-500 font-semibold">{{ fmtSalary(job) }}</span>
            </div>
            <div class="flex flex-wrap gap-2">
              <span v-for="(t, i) in (job.tags || '').split(/[,，]/).filter(Boolean)" :key="i"
                class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full">{{ t }}</span>
            </div>
          </div>
          <!-- Actions -->
          <div v-if="role === 'SEEKER'" class="flex flex-col gap-2 ml-4">
            <button @click="handleApply" :disabled="applyLoading"
              class="px-6 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 transition-colors">
              {{ applyLoading ? '投递中...' : '立即投递' }}
            </button>
            <button @click="handleFavorite"
              class="px-6 py-2.5 border border-gray-200 text-gray-600 text-sm font-medium rounded-lg hover:bg-gray-50 transition-colors">
              收藏职位
            </button>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-6">
        <!-- Main Content -->
        <div class="col-span-2 space-y-6">
          <!-- 职位描述 -->
          <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 mb-3">职位描述</h2>
            <pre class="text-sm text-gray-600 whitespace-pre-wrap font-sans leading-relaxed">{{ job.description }}</pre>
          </div>

          <!-- 任职要求 -->
          <div v-if="job.requirement" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 mb-3">任职要求</h2>
            <pre class="text-sm text-gray-600 whitespace-pre-wrap font-sans leading-relaxed">{{ job.requirement }}</pre>
          </div>

          <!-- 技能要求 -->
          <div v-if="job.skillsRequired" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 mb-3">技能要求</h2>
            <div class="flex flex-wrap gap-2">
              <span v-for="(s, i) in job.skillsRequired.split(/[,，]/).filter(Boolean)" :key="i"
                class="px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-lg">{{ s.trim() }}</span>
            </div>
          </div>

          <!-- 福利待遇 -->
          <div v-if="job.welfare" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 mb-3">福利待遇</h2>
            <div class="flex flex-wrap gap-2">
              <span v-for="(w, i) in job.welfare.split(/[,，]/).filter(Boolean)" :key="i"
                class="px-3 py-1 bg-green-50 text-green-700 text-sm rounded-lg">{{ w.trim() }}</span>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-4">
          <!-- 基本信息 -->
          <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
            <h3 class="text-sm font-bold text-gray-900 mb-3">基本信息</h3>
            <dl class="space-y-2 text-sm">
              <div class="flex justify-between">
                <dt class="text-gray-400">职位类别</dt>
                <dd class="text-gray-800">{{ job.category || '不限' }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">经验要求</dt>
                <dd class="text-gray-800">{{ job.experienceLevel || '不限' }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">学历要求</dt>
                <dd class="text-gray-800">{{ job.educationLevel || '不限' }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">工作类型</dt>
                <dd class="text-gray-800">{{ job.jobType || '全职' }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">招聘人数</dt>
                <dd class="text-gray-800">{{ job.headCount || 1 }}人</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">浏览次数</dt>
                <dd class="text-gray-800">{{ job.viewCount || 0 }}</dd>
              </div>
              <div class="flex justify-between">
                <dt class="text-gray-400">投递人数</dt>
                <dd class="text-gray-800">{{ job.applyCount || 0 }}</dd>
              </div>
              <div v-if="job.publishedAt" class="flex justify-between">
                <dt class="text-gray-400">发布时间</dt>
                <dd class="text-gray-800">{{ fmtTime(job.publishedAt) }}</dd>
              </div>
            </dl>
          </div>

          <!-- 公司信息 -->
          <div class="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
            <h3 class="text-sm font-bold text-gray-900 mb-3">公司信息</h3>
            <div class="flex items-center gap-3 mb-2">
              <div v-if="job.companyLogo" class="w-10 h-10 rounded-lg overflow-hidden">
                <img :src="job.companyLogo" class="w-full h-full object-cover" />
              </div>
              <div v-else class="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                <span class="text-blue-600 text-sm font-bold">{{ (job.companyName || '?')[0] }}</span>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-800">{{ job.companyName }}</p>
                <p v-if="job.companyVerified" class="text-[10px] text-green-600">已认证企业</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
