<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const companyId = ref(route.params.id)
const saving = ref(false)
const aiLoading = ref(false)
const aiResult = ref('')

const form = ref({
  name: '', shortName: '', industry: '', scale: '', stage: '',
  website: '', address: '', description: '', culture: '', welfare: ''
})

const industries = ['互联网', '金融', '教育', '医疗', '人工智能', '电商', '游戏', '硬件']
const scales = ['1-20', '20-99', '100-499', '500-999', '1000+']
const stages = ['未融资', '天使轮', 'A轮', 'B轮', 'C轮', '上市']

onMounted(async () => {
  if (companyId.value) {
    isEdit.value = true
    try {
      const res = await fetch('/api/companies/' + companyId.value)
      const data = await res.json()
      if (data.code === 0) {
        const c = data.data
        form.value = {
          name: c.name || '', shortName: c.shortName || '',
          industry: c.industry || '', scale: c.scale || '', stage: c.stage || '',
          website: c.website || '', address: c.address || '',
          description: c.description || '', culture: c.culture || '', welfare: c.welfare || ''
        }
      }
    } catch (e) { console.error(e) }
  }
})

async function save() {
  if (!form.value.name) { alert('公司名称不能为空'); return }
  saving.value = true
  try {
    const url = isEdit.value ? '/api/companies/' + companyId.value : '/api/companies'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method, headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + localStorage.getItem('token') },
      body: JSON.stringify(form.value)
    })
    const data = await res.json()
    if (data.code === 0) {
      router.push('/companies/' + (isEdit.value ? companyId.value : data.data))
    } else { alert(data.message) }
  } catch (e) { alert('保存失败') }
  finally { saving.value = false }
}

async function aiDescribe() {
  if (!isEdit.value) { alert('请先保存公司后再使用 AI 生成'); return }
  aiLoading.value = true
  aiResult.value = ''
  try {
    const res = await fetch('/api/companies/' + companyId.value + '/ai-describe', {
      method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) { aiResult.value = data.data }
    else { alert(data.message) }
  } catch (e) { alert('AI 请求失败') }
  finally { aiLoading.value = false }
}

function applyAiResult() { form.value.description = aiResult.value; aiResult.value = '' }
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ isEdit ? '编辑公司' : '创建公司' }}</h1>

    <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm space-y-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div><label class="block text-xs font-medium text-gray-600 mb-1">公司名称 *</label><input v-model="form.name" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">简称</label><input v-model="form.shortName" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">行业</label>
          <select v-model="form.industry" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500"><option value="">请选择</option><option v-for="i in industries" :key="i" :value="i">{{ i }}</option></select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">规模</label>
          <select v-model="form.scale" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500"><option value="">请选择</option><option v-for="s in scales" :key="s" :value="s">{{ s }}</option></select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600 mb-1">融资阶段</label>
          <select v-model="form.stage" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500"><option value="">请选择</option><option v-for="s in stages" :key="s" :value="s">{{ s }}</option></select>
        </div>
        <div><label class="block text-xs font-medium text-gray-600 mb-1">官网</label><input v-model="form.website" placeholder="https://" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div class="sm:col-span-2"><label class="block text-xs font-medium text-gray-600 mb-1">地址</label><input v-model="form.address" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
        <div class="sm:col-span-2"><label class="block text-xs font-medium text-gray-600 mb-1">公司介绍</label><textarea v-model="form.description" rows="4" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" placeholder="简要介绍公司业务、发展历程和核心优势"></textarea></div>
        <div class="sm:col-span-2"><label class="block text-xs font-medium text-gray-600 mb-1">企业文化</label><textarea v-model="form.culture" rows="2" class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500"></textarea></div>
        <div class="sm:col-span-2"><label class="block text-xs font-medium text-gray-600 mb-1">福利待遇 (JSON数组)</label><input v-model="form.welfare" placeholder='["六险一金","免费三餐","弹性工作"]' class="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-blue-500" /></div>
      </div>

      <!-- AI Generate -->
      <div v-if="isEdit" class="p-4 bg-gray-50 border border-dashed border-gray-300 rounded-lg">
        <div class="flex items-center justify-between mb-2">
          <p class="text-sm font-medium text-gray-700">AI 生成公司介绍</p>
          <button @click="aiDescribe" :disabled="aiLoading" class="text-xs px-3 py-1.5 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors disabled:bg-blue-300">
            {{ aiLoading ? '生成中...' : '生成' }}
          </button>
        </div>
        <div v-if="aiResult" class="mt-3">
          <p class="text-sm text-gray-600 bg-white p-3 rounded border whitespace-pre-wrap">{{ aiResult }}</p>
          <button @click="applyAiResult" class="mt-2 text-xs px-3 py-1.5 bg-green-600 text-white rounded hover:bg-green-700 transition-colors">应用到公司介绍</button>
        </div>
      </div>

      <div class="flex gap-3 pt-2">
        <button @click="save" :disabled="saving" class="flex-1 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors disabled:bg-blue-300">
          {{ saving ? '保存中...' : (isEdit ? '保存修改' : '创建公司') }}
        </button>
        <button @click="router.back()" class="px-6 py-2.5 border border-gray-200 text-gray-600 text-sm rounded-md hover:bg-gray-50 transition-colors">取消</button>
      </div>
    </div>
  </div>
</template>
