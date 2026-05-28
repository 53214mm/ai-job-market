<script setup>
import { ref, onMounted } from 'vue'

const token = localStorage.getItem('token')
const h = () => ({ 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' })
const jobs = ref([])
const selectedJob = ref(null)
const record = ref(null); const answer = ref('')
const feedback = ref(null); const loading = ref(false); const started = ref(false)
const records = ref([]); const report = ref(null)

onMounted(async () => {
  try { const res = await fetch('/api/jobs?current=1&pageSize=50', { headers: h() }); const d = await res.json(); jobs.value = d.data?.records || [] } catch(e){}
})

async function start() {
  if (!selectedJob.value) return
  loading.value = true
  const res = await fetch('/api/ai/interview/start', { method:'POST', headers:h(), body:JSON.stringify({jobId:selectedJob.value}) })
  const d = await res.json()
  if (d.code === 0) { record.value = d.data; feedback.value = null; started.value = true; records.value = [d.data] }
  loading.value = false
}

async function submitAnswer() {
  if (!answer.value.trim()) return
  loading.value = true
  const res = await fetch('/api/ai/interview/answer', { method:'POST', headers:h(), body:JSON.stringify({recordId:record.value.id, answer:answer.value}) })
  const d = await res.json()
  if (d.code === 0) {
    feedback.value = d.data
    if (d.data.nextQuestion) { record.value = d.data.nextQuestion; records.value.push(d.data.nextQuestion) }
    else record.value = null
  }
  answer.value = ''; loading.value = false
}

async function showReport() {
  if (!started.value) return
  const sid = records.value[0]?.sessionId
  if (!sid) return
  const res = await fetch('/api/ai/interview/' + sid + '/report', { headers: h() })
  report.value = (await res.json()).data
}

function reset() { started.value = false; record.value = null; feedback.value = null; report.value = null; records.value = [] }
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">AI 模拟面试</h1>

    <!-- Setup -->
    <div v-if="!started" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
      <p class="text-sm text-gray-500 mb-4">选择目标职位后，AI将根据职位要求自动生成技术面试题，并评估你的回答质量。全程模拟真实面试场景。</p>
      <select v-model="selectedJob" class="w-full px-4 py-3 border rounded-lg text-sm mb-4 focus:ring-2 focus:ring-blue-500">
        <option :value="null">选择目标职位</option>
        <option v-for="j in jobs" :key="j.id" :value="j.id">{{ j.title }} - {{ j.companyName }}</option>
      </select>
      <button @click="start" :disabled="!selectedJob || loading"
        class="w-full py-3 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 transition-colors">
        {{ loading ? '准备中...' : '开始模拟面试' }}
      </button>
    </div>

    <!-- Question -->
    <div v-if="record" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6">
      <div class="flex items-center justify-between mb-4">
        <span class="text-xs font-medium text-gray-400 bg-gray-100 px-2 py-1 rounded">第 {{ records.length }} 题</span>
        <span class="text-xs text-gray-400">共5题</span>
      </div>
      <div class="p-5 bg-blue-50 rounded-xl text-sm text-gray-800 leading-relaxed whitespace-pre-wrap mb-4">{{ record.question }}</div>
      <label class="block text-xs font-medium text-gray-600 mb-2">你的回答</label>
      <textarea v-model="answer" rows="5" placeholder="请在此输入你的回答..." :disabled="loading"
        class="w-full px-4 py-3 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent mb-3 resize-none"></textarea>
      <button @click="submitAnswer" :disabled="loading || !answer.trim()"
        class="w-full py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 transition-colors">
        {{ loading ? 'AI 评估中...' : '提交回答' }}
      </button>
    </div>

    <!-- Feedback -->
    <div v-if="feedback" class="bg-white border border-blue-100 rounded-xl p-6 shadow-sm mb-6">
      <div class="flex items-center gap-4 mb-3">
        <div class="w-16 h-16 rounded-full flex items-center justify-center"
          :class="feedback.score >= 80 ? 'bg-green-50' : feedback.score >= 60 ? 'bg-yellow-50' : 'bg-red-50'">
          <span class="text-2xl font-bold" :class="feedback.score >= 80 ? 'text-green-600' : feedback.score >= 60 ? 'text-yellow-600' : 'text-red-600'">{{ feedback.score || '-' }}</span>
        </div>
        <div>
          <p class="text-sm font-medium text-gray-700">AI 评分</p>
          <p class="text-xs text-gray-400">满分 100</p>
        </div>
      </div>
      <p class="text-sm text-gray-600 leading-relaxed p-3 bg-gray-50 rounded-lg">{{ feedback.feedback }}</p>
    </div>

    <!-- Interview Complete -->
    <div v-if="started && !record" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-6 text-center">
      <p class="text-lg font-semibold text-gray-800 mb-2">面试完成！</p>
      <p class="text-sm text-gray-500 mb-4">你已完成全部 {{ records.length }} 道题目</p>
      <div class="flex gap-3 justify-center">
        <button @click="showReport" class="px-6 py-2.5 bg-purple-600 text-white text-sm font-medium rounded-lg hover:bg-purple-700 transition-colors">查看面试报告</button>
        <button @click="reset" class="px-6 py-2.5 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition-colors">重新开始</button>
      </div>
    </div>

    <!-- Report -->
    <div v-if="report" class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">面试报告</h2>
      <div class="grid grid-cols-3 gap-4 mb-4">
        <div class="text-center p-4 bg-gray-50 rounded-xl"><p class="text-3xl font-bold text-blue-600">{{ report.totalQuestions }}</p><p class="text-xs text-gray-400 mt-1">总题数</p></div>
        <div class="text-center p-4 bg-gray-50 rounded-xl"><p class="text-3xl font-bold text-green-600">{{ report.answeredQuestions }}</p><p class="text-xs text-gray-400 mt-1">已作答</p></div>
        <div class="text-center p-4 bg-gray-50 rounded-xl"><p class="text-3xl font-bold text-purple-600">{{ report.averageScore }}</p><p class="text-xs text-gray-400 mt-1">平均分</p></div>
      </div>
      <p class="text-sm text-gray-600 p-4 bg-blue-50 rounded-lg mb-4">{{ report.suggestion }}</p>
      <div class="space-y-2">
        <div v-for="r in (report.records||[])" :key="r.id" class="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
          <span class="text-xs text-gray-600">Q{{ r.questionIndex }}: {{ r.question?.slice(0, 60) }}...</span>
          <span class="text-xs font-medium" :class="r.score >= 80 ? 'text-green-600' : r.score >= 60 ? 'text-yellow-600' : r.score ? 'text-red-600' : 'text-gray-300'">{{ r.score ? r.score + '分' : '未作答' }}</span>
        </div>
      </div>
      <button @click="reset" class="mt-4 w-full py-2.5 border border-gray-200 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition-colors">重新开始</button>
    </div>
  </div>
</template>
