<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { resumeApi } from '../api.js'

const route = useRoute()
const router = useRouter()
const resumeId = ref(route.params.id)

const form = ref({
  title: '', fullName: '', phone: '', email: '',
  currentCity: '', expectedCity: '',
  expectedSalaryMin: null, expectedSalaryMax: null,
  jobStatus: '', summary: '', privacy: 'PUBLIC'
})

const educations = ref([])
const works = ref([])
const projects = ref([])
const skills = ref([])
const certs = ref([])
const loading = ref(true)
const saving = ref(false)

onMounted(async () => {
  try {
    const data = await resumeApi.detail(resumeId.value)
    form.value = {
      title: data.title, fullName: data.fullName, phone: data.phone, email: data.email,
      currentCity: data.currentCity, expectedCity: data.expectedCity,
      expectedSalaryMin: data.expectedSalaryMin, expectedSalaryMax: data.expectedSalaryMax,
      jobStatus: data.jobStatus, summary: data.summary, privacy: data.privacy
    }
    educations.value = data.educations || []
    works.value = data.workExperiences || []
    projects.value = data.projects || []
    skills.value = data.skills || []
    certs.value = data.certificates || []
  } catch (e) { alert(e.message); router.push('/resumes') }
  finally { loading.value = false }
})

async function saveBasic() {
  saving.value = true
  try { await resumeApi.update(resumeId.value, form.value); alert('保存成功') }
  catch (e) { alert(e.message) }
  finally { saving.value = false }
}

// Sub-item helpers
const newItem = (type) => ({ education: {}, work: {}, project: {}, skill: {}, cert: {} }[type]())

async function addItem(type) {
  const item = newItem(type)
  try {
    const result = await resumeApi.addItem(resumeId.value, type, item)
    if (type === 'education') educations.value.push(result)
    else if (type === 'work') works.value.push(result)
    else if (type === 'project') projects.value.push(result)
    else if (type === 'skill') skills.value.push(result)
    else if (type === 'cert') certs.value.push(result)
  } catch (e) { alert(e.message) }
}

async function updateItem(type, index, item) {
  try {
    const result = await resumeApi.updateItem(resumeId.value, type, item.id, item)
    if (type === 'education') educations.value[index] = result
    else if (type === 'work') works.value[index] = result
    else if (type === 'project') projects.value[index] = result
    else if (type === 'skill') skills.value[index] = result
    else if (type === 'cert') certs.value[index] = result
  } catch (e) { alert(e.message) }
}

async function deleteItem(type, index, item) {
  if (!confirm('确定删除？')) return
  try {
    await resumeApi.deleteItem(resumeId.value, type, item.id)
    if (type === 'education') educations.value.splice(index, 1)
    else if (type === 'work') works.value.splice(index, 1)
    else if (type === 'project') projects.value.splice(index, 1)
    else if (type === 'skill') skills.value.splice(index, 1)
    else if (type === 'cert') certs.value.splice(index, 1)
  } catch (e) { alert(e.message) }
}

function goBack() { router.push('/resumes/' + resumeId.value) }
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">编辑简历</h1>
      <button @click="goBack" class="text-sm text-gray-500 hover:text-blue-600">&larr; 返回详情</button>
    </div>

    <div v-if="loading" class="text-center py-12">加载中...</div>

    <template v-else>
      <!-- Basic Info -->
      <section class="bg-white border border-gray-200 rounded-xl p-6 mb-6 shadow-sm">
        <h2 class="text-base font-semibold text-gray-900 mb-4">基本信息</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div><label class="block text-xs font-medium text-gray-600 mb-1">简历标题</label><input v-model="form.title" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">姓名</label><input v-model="form.fullName" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">电话</label><input v-model="form.phone" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">邮箱</label><input v-model="form.email" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">现居城市</label><input v-model="form.currentCity" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">期望城市</label><input v-model="form.expectedCity" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">最低薪资(K)</label><input v-model.number="form.expectedSalaryMin" type="number" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div><label class="block text-xs font-medium text-gray-600 mb-1">最高薪资(K)</label><input v-model.number="form.expectedSalaryMax" type="number" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">求职状态</label>
            <select v-model="form.jobStatus" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500">
              <option value="">--</option><option value="在职">在职</option><option value="离职">离职</option><option value="应届">应届</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">隐私设置</label>
            <select v-model="form.privacy" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500">
              <option value="PUBLIC">公开</option><option value="APPLICATION_ONLY">仅投递可见</option><option value="PRIVATE">私密</option>
            </select>
          </div>
        </div>
        <div class="mt-4">
          <label class="block text-xs font-medium text-gray-600 mb-1">自我评价</label>
          <textarea v-model="form.summary" rows="3" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" placeholder="简要描述您的优势、技能和职业目标"></textarea>
        </div>
        <button @click="saveBasic" :disabled="saving"
          class="mt-4 px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors">
          {{ saving ? '保存中...' : '保存基本信息' }}
        </button>
      </section>

      <!-- Education -->
      <section class="bg-white border border-gray-200 rounded-xl p-6 mb-6 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold text-gray-900">教育经历</h2>
          <button @click="addItem('education')" class="text-xs px-2 py-1 border border-blue-200 text-blue-600 rounded hover:bg-blue-50">+ 添加</button>
        </div>
        <div v-for="(e, i) in educations" :key="e.id || i" class="border-b border-gray-100 pb-4 mb-4 last:border-0 last:pb-0 last:mb-0">
          <div class="grid grid-cols-2 gap-2 text-sm">
            <input v-model="e.schoolName" placeholder="学校" class="px-2 py-1.5 border rounded text-sm" />
            <input v-model="e.degree" placeholder="学历" class="px-2 py-1.5 border rounded text-sm" />
            <input v-model="e.major" placeholder="专业" class="px-2 py-1.5 border rounded text-sm" />
            <div class="flex gap-1"><input v-model="e.startDate" type="date" class="flex-1 px-2 py-1.5 border rounded text-sm" /><input v-model="e.endDate" type="date" class="flex-1 px-2 py-1.5 border rounded text-sm" /></div>
          </div>
          <div class="flex items-center justify-between mt-2">
            <button @click="updateItem('education', i, e)" class="text-xs text-blue-600">保存</button>
            <button @click="deleteItem('education', i, e)" class="text-xs text-red-400">删除</button>
          </div>
        </div>
      </section>

      <!-- Work Experience -->
      <section class="bg-white border border-gray-200 rounded-xl p-6 mb-6 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold text-gray-900">工作经历</h2>
          <button @click="addItem('work')" class="text-xs px-2 py-1 border border-blue-200 text-blue-600 rounded hover:bg-blue-50">+ 添加</button>
        </div>
        <div v-for="(w, i) in works" :key="w.id || i" class="border-b border-gray-100 pb-4 mb-4 last:border-0 last:pb-0 last:mb-0">
          <div class="grid grid-cols-2 gap-2 text-sm">
            <input v-model="w.companyName" placeholder="公司" class="px-2 py-1.5 border rounded" />
            <input v-model="w.position" placeholder="职位" class="px-2 py-1.5 border rounded" />
            <div class="flex gap-1"><input v-model="w.startDate" type="date" class="flex-1 px-2 py-1.5 border rounded text-sm" /><input v-model="w.endDate" type="date" class="flex-1 px-2 py-1.5 border rounded text-sm" /></div>
            <input v-model="w.industry" placeholder="行业" class="px-2 py-1.5 border rounded" />
            <input v-model="w.description" placeholder="工作内容" class="col-span-2 px-2 py-1.5 border rounded" />
          </div>
          <div class="flex items-center justify-between mt-2">
            <button @click="updateItem('work', i, w)" class="text-xs text-blue-600">保存</button>
            <button @click="deleteItem('work', i, w)" class="text-xs text-red-400">删除</button>
          </div>
        </div>
      </section>

      <!-- Skills -->
      <section class="bg-white border border-gray-200 rounded-xl p-6 mb-6 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold text-gray-900">技能</h2>
          <button @click="addItem('skill')" class="text-xs px-2 py-1 border border-blue-200 text-blue-600 rounded hover:bg-blue-50">+ 添加</button>
        </div>
        <div v-for="(s, i) in skills" :key="s.id || i" class="flex items-center gap-2 mb-2">
          <input v-model="s.skillName" placeholder="技能名" class="w-36 px-2 py-1.5 border rounded text-sm" />
          <select v-model="s.proficiency" class="w-24 px-2 py-1.5 border rounded text-sm">
            <option value="">水平</option><option>初学</option><option>熟练</option><option>精通</option><option>专家</option>
          </select>
          <button @click="updateItem('skill', i, s)" class="text-xs text-blue-600">保存</button>
          <button @click="deleteItem('skill', i, s)" class="text-xs text-red-400">删除</button>
        </div>
      </section>
    </template>
  </div>
</template>
