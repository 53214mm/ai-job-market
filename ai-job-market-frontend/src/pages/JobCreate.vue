<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = localStorage.getItem('token')
const h = () => ({ 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' })

const loading = ref(false)

const form = ref({
  title: '',
  category: '',
  experienceLevel: '',
  educationLevel: '',
  jobType: 'FULL_TIME',
  city: '',
  district: '',
  salaryMin: null,
  salaryMax: null,
  salaryMonths: 12,
  skillsRequired: '',
  tags: '',
  description: '',
  requirement: '',
  welfare: '',
  headCount: 1,
})

const categories = ['后端开发', '前端开发', '移动端开发', 'AI/算法', '数据', '运维/DevOps', '产品经理', 'UI/UX设计', '测试', '运营', '市场/销售', '其他']
const expLevels = ['ENTRY', 'JUNIOR', 'MIDDLE', 'SENIOR', 'STAFF', 'PRINCIPAL']
const eduLevels = ['不限', '大专', '本科', '硕士', '博士']
const jobTypes = ['FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT']

async function submit() {
  if (!form.value.title || !form.value.category) {
    alert('请填写职位名称和类别')
    return
  }
  loading.value = true
  try {
    const body = {
      ...form.value,
      salaryMin: Number(form.value.salaryMin) || null,
      salaryMax: Number(form.value.salaryMax) || null,
      salaryMonths: Number(form.value.salaryMonths) || 12,
      headCount: Number(form.value.headCount) || 1,
    }
    const res = await fetch('/api/jobs', { method: 'POST', headers: h(), body: JSON.stringify(body) })
    const d = await res.json()
    if (d.code === 0) {
      alert('职位发布成功')
      router.push('/recruiter/jobs')
    } else {
      alert(d.message || '发布失败')
    }
  } catch(e) { alert('网络错误') }
  finally { loading.value = false }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">发布新职位</h1>

    <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm space-y-4">
      <!-- 职位名称 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">职位名称 <span class="text-red-400">*</span></label>
        <input v-model="form.title" placeholder="如：高级Java开发工程师"
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      </div>

      <!-- 类别 + 经验 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">职位类别</label>
          <select v-model="form.category" class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option value="">选择类别</option>
            <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">经验要求</label>
          <select v-model="form.experienceLevel" class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option value="">不限</option>
            <option v-for="e in expLevels" :key="e" :value="e">{{ e }}</option>
          </select>
        </div>
      </div>

      <!-- 学历 + 工作类型 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">学历要求</label>
          <select v-model="form.educationLevel" class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option v-for="e in eduLevels" :key="e" :value="e">{{ e }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">工作类型</label>
          <select v-model="form.jobType" class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500">
            <option v-for="t in jobTypes" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>
      </div>

      <!-- 城市 + 区县 -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">工作城市</label>
          <input v-model="form.city" placeholder="如：北京"
            class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">区县</label>
          <input v-model="form.district" placeholder="如：朝阳区"
            class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
      </div>

      <!-- 薪资 -->
      <div class="grid grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">最低薪资 (K)</label>
          <input v-model="form.salaryMin" type="number" placeholder="15"
            class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">最高薪资 (K)</label>
          <input v-model="form.salaryMax" type="number" placeholder="30"
            class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">薪月数</label>
          <input v-model="form.salaryMonths" type="number" placeholder="12"
            class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
      </div>

      <!-- 技能要求 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">技能要求（逗号分隔）</label>
        <input v-model="form.skillsRequired" placeholder="如：Java, Spring Boot, MySQL, Redis"
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      </div>

      <!-- 标签 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">职位标签（逗号分隔）</label>
        <input v-model="form.tags" placeholder="如：六险一金, 弹性工作, 技术氛围好"
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      </div>

      <!-- 职位描述 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">职位描述</label>
        <textarea v-model="form.description" rows="5" placeholder="描述该职位的职责和工作内容..."
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
      </div>

      <!-- 任职要求 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">任职要求</label>
        <textarea v-model="form.requirement" rows="4" placeholder="如：3年以上Java开发经验..."
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
      </div>

      <!-- 福利待遇 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">福利待遇</label>
        <input v-model="form.welfare" placeholder="如：五险一金, 年终奖, 弹性工作"
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      </div>

      <!-- 招聘人数 -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">招聘人数</label>
        <input v-model="form.headCount" type="number" min="1"
          class="w-full px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />
      </div>

      <button @click="submit" :disabled="loading"
        class="w-full py-3 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:bg-blue-300 transition-colors mt-4">
        {{ loading ? '发布中...' : '发布职位' }}
      </button>
    </div>
  </div>
</template>
