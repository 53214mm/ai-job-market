<script setup>
import { ref, onMounted } from 'vue'
const token = localStorage.getItem('token'); const h = { 'Authorization':'Bearer '+token, 'Content-Type':'application/json' }
const jobs = ref([]); const selectedJob = ref(null)
const record = ref(null); const answer = ref('')
const feedback = ref(null); const loading = ref(false); const started = ref(false)
const records = ref([]); const report = ref(null)

onMounted(async () => {
  const res = await fetch('/api/jobs?current=1&pageSize=50', { headers: h })
  const data = await res.json()
  jobs.value = data.data?.records || []
})

async function start() {
  if (!selectedJob.value) return
  loading.value = true
  const res = await fetch('/api/ai/interview/start', { method:'POST', headers:h, body:JSON.stringify({jobId:selectedJob.value}) })
  const data = await res.json()
  if (data.code === 0) { record.value = data.data; feedback.value = null; started.value = true; records.value = [data.data] }
  loading.value = false
}

async function submitAnswer() {
  if (!answer.value.trim()) return
  loading.value = true
  const res = await fetch('/api/ai/interview/answer', { method:'POST', headers:h, body:JSON.stringify({recordId:record.value.id, answer:answer.value}) })
  const data = await res.json()
  if (data.code === 0) {
    feedback.value = { score: data.data.score, feedback: data.data.feedback }
    if (data.data.nextQuestion) { record.value = data.data.nextQuestion; records.value.push(data.data.nextQuestion) }
    else { record.value = null }
  }
  answer.value = ''; loading.value = false
}

async function showReport() {
  if (!started.value) return
  const sid = records.value[0]?.sessionId
  const res = await fetch('/api/ai/interview/' + sid + '/report', { headers: h })
  report.value = (await res.json()).data
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">AI 模拟面试</h1>

    <!-- Setup -->
    <div v-if="!started" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
      <p class="text-sm text-gray-500 mb-3">选择目标职位后开始AI模拟面试，系统将根据职位要求自动生成面试题。</p>
      <select v-model="selectedJob" class="w-full px-3 py-2.5 border rounded-md text-sm mb-3 focus:ring-2 focus:ring-blue-500">
        <option :value="null">选择目标职位</option>
        <option v-for="j in jobs" :key="j.id" :value="j.id">{{ j.title }} - {{ j.companyName }}</option>
      </select>
      <button @click="start" :disabled="!selectedJob || loading" class="px-6 py-2.5 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors">
        {{ loading ? '准备中...' : '开始面试' }}
      </button>
    </div>

    <!-- Question -->
    <div v-if="record" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
      <div class="text-xs text-gray-400 mb-2">第 {{ record.questionIndex }} 题</div>
      <div class="p-4 bg-blue-50 rounded-lg text-sm text-blue-800 mb-4 whitespace-pre-wrap">{{ record.question }}</div>
      <textarea v-model="answer" rows="4" placeholder="输入你的回答..." class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500 mb-3"></textarea>
      <button @click="submitAnswer" :disabled="loading" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors">
        {{ loading ? '评估中...' : '提交回答' }}
      </button>
    </div>

    <!-- Feedback -->
    <div v-if="feedback" class="bg-white border border-blue-100 rounded-xl p-5 shadow-sm mb-6">
      <div class="flex items-center gap-4 mb-2">
        <span class="text-2xl font-bold" :class="feedback.score >= 80 ? 'text-green-600' : feedback.score >= 60 ? 'text-yellow-600' : 'text-red-600'">{{ feedback.score }}</span>
        <span class="text-xs text-gray-400">/ 100</span>
      </div>
      <p class="text-sm text-gray-600">{{ feedback.feedback }}</p>
    </div>

    <!-- Report -->
    <button v-if="started && !record" @click="showReport" class="px-4 py-2 bg-purple-600 text-white text-sm rounded-md hover:bg-purple-700 mb-4">查看面试报告</button>
    <div v-if="report" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
      <h2 class="text-lg font-semibold text-gray-900 mb-3">面试报告</h2>
      <div class="grid grid-cols-3 gap-4 mb-4">
        <div class="text-center p-3 bg-gray-50 rounded-lg"><p class="text-2xl font-bold text-blue-600">{{ report.totalQuestions }}</p><p class="text-xs text-gray-400">总题数</p></div>
        <div class="text-center p-3 bg-gray-50 rounded-lg"><p class="text-2xl font-bold text-green-600">{{ report.answeredQuestions }}</p><p class="text-xs text-gray-400">已作答</p></div>
        <div class="text-center p-3 bg-gray-50 rounded-lg"><p class="text-2xl font-bold text-purple-600">{{ report.averageScore }}</p><p class="text-xs text-gray-400">平均分</p></div>
      </div>
      <p class="text-sm text-gray-600 p-3 bg-blue-50 rounded-lg">{{ report.suggestion }}</p>
      <div class="mt-4 space-y-2">
        <div v-for="r in (report.records||[])" :key="r.id" class="text-xs text-gray-500 border-b border-gray-100 pb-2"><span class="font-medium">Q{{ r.questionIndex }}:</span> {{ r.question?.slice(0, 80) }}... {{ r.score != null ? '→ 评分 ' + r.score : '未作答' }}</div>
      </div>
    </div>
  </div>
</template>
