<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { resumeApi } from '../api.js'

const router = useRouter()

const form = ref({
  title: '', fullName: '', phone: '', email: '',
  currentCity: '', expectedCity: '',
  expectedSalaryMin: null, expectedSalaryMax: null,
  jobStatus: '', summary: '', privacy: 'PUBLIC'
})

const loading = ref(false)
const uploading = ref(false)

async function handlePdfUpload(e) {
  const file = e.target.files[0]
  if (!file || !file.name.endsWith('.pdf')) { alert('请选择 PDF 文件'); return }
  uploading.value = true
  try {
    const id = await resumeApi.uploadPdf(file)
    router.push('/resumes/' + id + '/edit')
  } catch (err) { alert(err.message) }
  finally { uploading.value = false }
}

async function handleCreate() {
  if (!form.value.title || !form.value.fullName) {
    alert('请填写简历标题和姓名')
    return
  }
  loading.value = true
  try {
    const id = await resumeApi.create(form.value)
    router.push('/resumes/' + id)
  } catch (e) { alert(e.message) }
  finally { loading.value = false }
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">创建新简历</h1>
      <button @click="router.push('/resumes')" class="text-sm text-gray-500 hover:text-blue-600">&larr; 返回列表</button>
    </div>

    <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
      <p class="text-sm text-gray-500 mb-4">填写基本信息后创建简历，之后可添加教育经历、工作经历、技能等详细信息。</p>

      <!-- PDF Upload -->
      <div class="mb-6 p-4 bg-gray-50 border border-dashed border-gray-300 rounded-lg text-center">
        <p class="text-sm font-medium text-gray-700 mb-2">上传 PDF 简历，AI 自动解析</p>
        <p class="text-xs text-gray-400 mb-3">支持 PDF 格式，AI 将自动提取信息填充简历</p>
        <label class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-md cursor-pointer hover:border-blue-300 transition-colors">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/></svg>
          <span class="text-sm text-gray-600">{{ uploading ? '解析中...' : '选择 PDF 文件' }}</span>
          <input type="file" accept=".pdf" @change="handlePdfUpload" class="hidden" :disabled="uploading" />
        </label>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">简历标题 <span class="text-red-400">*</span></label>
          <input v-model="form.title" placeholder="如：求职Java开发工程师" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">姓名 <span class="text-red-400">*</span></label>
          <input v-model="form.fullName" placeholder="真实姓名" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" />
        </div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">电话</label><input v-model="form.phone" placeholder="手机号" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">邮箱</label><input v-model="form.email" placeholder="邮箱地址" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">现居城市</label><input v-model="form.currentCity" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">期望城市</label><input v-model="form.expectedCity" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">最低薪资(K)</label><input v-model.number="form.expectedSalaryMin" type="number" placeholder="如 15" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">最高薪资(K)</label><input v-model.number="form.expectedSalaryMax" type="number" placeholder="如 25" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">求职状态</label>
          <select v-model="form.jobStatus" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500">
            <option value="">请选择</option><option>在职</option><option>离职</option><option>应届</option>
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
        <textarea v-model="form.summary" rows="4" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" placeholder="简要描述您的优势、技能和职业目标"></textarea>
      </div>

      <div class="flex gap-3 mt-6">
        <button @click="handleCreate" :disabled="loading"
          class="flex-1 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors disabled:bg-blue-300">
          {{ loading ? '创建中...' : '创建简历' }}
        </button>
        <button @click="router.push('/resumes')"
          class="px-6 py-2.5 border border-gray-200 text-gray-600 text-sm rounded-md hover:bg-gray-50 transition-colors">
          取消
        </button>
      </div>
    </div>
  </div>
</template>
