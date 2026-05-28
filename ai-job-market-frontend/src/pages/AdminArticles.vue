<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

const articles = ref([])
const categories = ref([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const showForm = ref(false)
const editing = ref(null)
const form = ref({ title: '', content: '', categoryId: null, summary: '', coverUrl: '', tags: '' })
const filterStatus = ref('')

onMounted(async () => {
  await loadCategories()
  await loadArticles()
})

async function loadCategories() {
  try {
    const res = await fetch('/api/articles/categories')
    const data = await res.json()
    if (data.code === 0) categories.value = data.data || []
  } catch (e) { /* */ }
}

async function loadArticles() {
  const token = localStorage.getItem('token')
  try {
    const params = new URLSearchParams({ current: current.value, pageSize: pageSize.value })
    if (filterStatus.value) params.set('status', filterStatus.value)
    const res = await fetch('/api/articles/admin/all?' + params, {
      headers: { 'Authorization': 'Bearer ' + token }
    })
    const data = await res.json()
    if (data.code === 0) {
      articles.value = data.data.records || []
      total.value = data.data.total || 0
    }
  } catch (e) { /* */ }
}

function openCreate() {
  editing.value = null
  form.value = { title: '', content: '', categoryId: null, summary: '', coverUrl: '', tags: '' }
  showForm.value = true
}

function openEdit(a) {
  editing.value = a.id
  form.value = {
    title: a.title || '',
    content: a.content || '',
    categoryId: a.categoryId,
    summary: a.summary || '',
    coverUrl: a.coverUrl || '',
    tags: a.tags || ''
  }
  showForm.value = true
}

async function save() {
  const token = localStorage.getItem('token')
  const h = { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }
  const url = editing.value ? '/api/articles/' + editing.value : '/api/articles'
  const method = editing.value ? 'PUT' : 'POST'
  try {
    const res = await fetch(url, { method, headers: h, body: JSON.stringify(form.value) })
    const data = await res.json()
    if (data.code === 0) {
      showForm.value = false
      await loadArticles()
    } else alert(data.message || '保存失败')
  } catch (e) { alert('保存失败') }
}

async function togglePublish(a) {
  const token = localStorage.getItem('token')
  const action = a.status === 'PUBLISHED' ? 'unpublish' : 'publish'
  try {
    const res = await fetch('/api/articles/' + a.id + '/' + action, {
      method: 'PUT', headers: { 'Authorization': 'Bearer ' + token }
    })
    const data = await res.json()
    if (data.code === 0) await loadArticles()
    else alert(data.message || '操作失败')
  } catch (e) { alert('操作失败') }
}

function changePage(page) {
  current.value = page
  loadArticles()
}

function goBack() {
  router.push('/admin')
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <div>
        <button @click="goBack" class="text-sm text-gray-500 hover:text-gray-700 mb-1">&larr; 返回管理后台</button>
        <h1 class="text-2xl font-bold text-gray-900">内容管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理求职攻略、行业资讯等文章</p>
      </div>
      <button @click="openCreate" class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors">
        + 新建文章
      </button>
    </div>

    <!-- Filters -->
    <div class="flex gap-2 mb-4">
      <button @click="filterStatus = ''; loadArticles()" class="px-3 py-1 text-xs rounded-full transition-colors" :class="filterStatus === '' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">全部</button>
      <button @click="filterStatus = 'PUBLISHED'; loadArticles()" class="px-3 py-1 text-xs rounded-full transition-colors" :class="filterStatus === 'PUBLISHED' ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">已发布</button>
      <button @click="filterStatus = 'DRAFT'; loadArticles()" class="px-3 py-1 text-xs rounded-full transition-colors" :class="filterStatus === 'DRAFT' ? 'bg-yellow-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">草稿</button>
    </div>

    <!-- Article List -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-4 py-3 font-medium text-gray-600">标题</th>
            <th class="text-left px-4 py-3 font-medium text-gray-600">分类</th>
            <th class="text-left px-4 py-3 font-medium text-gray-600">状态</th>
            <th class="text-left px-4 py-3 font-medium text-gray-600">阅读</th>
            <th class="text-left px-4 py-3 font-medium text-gray-600">发布时间</th>
            <th class="text-right px-4 py-3 font-medium text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in articles" :key="a.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-4 py-3">
              <p class="font-medium text-gray-800">{{ a.title }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ (a.summary || '').substring(0, 60) }}{{ (a.summary || '').length > 60 ? '...' : '' }}</p>
            </td>
            <td class="px-4 py-3 text-gray-600">{{ categories.find(c => c.id === a.categoryId)?.name || '-' }}</td>
            <td class="px-4 py-3">
              <span :class="a.status === 'PUBLISHED' ? 'bg-green-50 text-green-600' : 'bg-yellow-50 text-yellow-600'" class="px-2 py-0.5 text-xs rounded-full font-medium">
                {{ a.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </span>
            </td>
            <td class="px-4 py-3 text-gray-600">{{ a.viewCount || 0 }}</td>
            <td class="px-4 py-3 text-gray-500 text-xs">{{ a.publishedAt ? new Date(a.publishedAt).toLocaleDateString('zh-CN') : '-' }}</td>
            <td class="px-4 py-3 text-right">
              <button @click="openEdit(a)" class="text-blue-600 hover:text-blue-800 text-xs mr-3">编辑</button>
              <button @click="togglePublish(a)" class="text-xs" :class="a.status === 'PUBLISHED' ? 'text-yellow-600 hover:text-yellow-800' : 'text-green-600 hover:text-green-800'">
                {{ a.status === 'PUBLISHED' ? '下架' : '发布' }}
              </button>
            </td>
          </tr>
          <tr v-if="articles.length === 0">
            <td colspan="6" class="text-center py-8 text-gray-400">暂无文章</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div class="flex justify-between items-center mt-4 text-sm text-gray-500">
      <span>共 {{ total }} 篇文章</span>
      <div class="flex gap-1">
        <button v-for="p in Math.ceil(total / pageSize)" :key="p" @click="changePage(p)" class="px-2.5 py-1 rounded text-xs" :class="current === p ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'">{{ p }}</button>
      </div>
    </div>

    <!-- Form Modal -->
    <div v-if="showForm" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showForm = false">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto p-6">
        <h2 class="text-lg font-semibold mb-4">{{ editing ? '编辑文章' : '新建文章' }}</h2>
        <div class="space-y-3">
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">标题</label>
            <input v-model="form.title" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none" placeholder="文章标题" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">分类</label>
            <select v-model="form.categoryId" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none">
              <option :value="null">请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">摘要</label>
            <input v-model="form.summary" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none" placeholder="文章摘要" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">封面图 URL</label>
            <input v-model="form.coverUrl" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none" placeholder="https://..." />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">标签（逗号分隔）</label>
            <input v-model="form.tags" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none" placeholder="例如：求职攻略, 面试技巧" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-600 mb-1">内容（支持 Markdown）</label>
            <textarea v-model="form.content" rows="10" class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none" placeholder="文章内容..."></textarea>
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-4">
          <button @click="showForm = false" class="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded-md hover:bg-gray-200">取消</button>
          <button @click="save" class="px-4 py-2 text-sm text-white bg-blue-600 rounded-md hover:bg-blue-700">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>
