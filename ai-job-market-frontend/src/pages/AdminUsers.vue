<script setup>
import { ref, onMounted } from 'vue'
const users = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const keyword = ref('')

async function fetchUsers() {
  loading.value = true
  try {
    const params = new URLSearchParams({ current: currentPage.value, pageSize: 10 })
    if (keyword.value) params.set('nickname', keyword.value)
    const res = await fetch('/api/user/list?' + params, {
      headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) { users.value = data.data.records || []; total.value = data.data.total || 0 }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function disableUser(id) {
  if (!confirm('确定禁用此用户？')) return
  try {
    const res = await fetch('/api/user/disable/' + id, {
      method: 'PUT', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
    })
    const data = await res.json()
    if (data.code === 0) {
      const u = users.value.find(x => x.id === id)
      if (u) u.status = 'DISABLED'
    } else { alert(data.message) }
  } catch (e) { alert('操作失败') }
}

function changePage(p) { currentPage.value = p; fetchUsers() }
onMounted(fetchUsers)

const roleMap = { SEEKER: '求职者', RECRUITER: '招聘方', ADMIN: '管理员' }
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">用户管理</h1>
      <div class="flex items-center gap-2">
        <input v-model="keyword" @keyup.enter="fetchUsers" placeholder="搜索昵称" class="px-3 py-1.5 border rounded-md text-sm focus:ring-2 focus:ring-red-500 w-40" />
        <button @click="fetchUsers" class="px-3 py-1.5 bg-red-600 text-white text-sm rounded-md hover:bg-red-700">搜索</button>
      </div>
    </div>

    <div class="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-4 py-3 font-medium text-gray-500">ID</th>
            <th class="text-left px-4 py-3 font-medium text-gray-500">昵称</th>
            <th class="text-left px-4 py-3 font-medium text-gray-500">邮箱</th>
            <th class="text-left px-4 py-3 font-medium text-gray-500">角色</th>
            <th class="text-left px-4 py-3 font-medium text-gray-500">状态</th>
            <th class="text-left px-4 py-3 font-medium text-gray-500">注册时间</th>
            <th class="text-right px-4 py-3 font-medium text-gray-500">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading"><td colspan="7" class="text-center py-12 text-gray-400">加载中...</td></tr>
          <tr v-else-if="users.length === 0"><td colspan="7" class="text-center py-12 text-gray-400">暂无数据</td></tr>
          <tr v-for="u in users" :key="u.id" class="border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3 text-gray-400 font-mono text-xs">{{ String(u.id).slice(-8) }}</td>
            <td class="px-4 py-3 font-medium text-gray-800">{{ u.nickname }}</td>
            <td class="px-4 py-3 text-gray-500">{{ u.email }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 text-xs rounded-full" :class="u.role === 'ADMIN' ? 'bg-red-50 text-red-600' : u.role === 'RECRUITER' ? 'bg-purple-50 text-purple-600' : 'bg-blue-50 text-blue-600'">{{ roleMap[u.role] || u.role }}</span>
            </td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 text-xs rounded-full" :class="u.status === 'ACTIVE' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'">{{ u.status === 'ACTIVE' ? '正常' : '已禁用' }}</span>
            </td>
            <td class="px-4 py-3 text-gray-400 text-xs">{{ (u.createdAt || '').slice(0, 10) }}</td>
            <td class="px-4 py-3 text-right">
              <button v-if="u.status === 'ACTIVE' && u.role !== 'ADMIN'" @click="disableUser(u.id)" class="text-xs px-2 py-1 text-red-600 hover:bg-red-50 rounded transition-colors">禁用</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="total > 10" class="flex justify-center gap-2 mt-6">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-30">上一页</button>
      <span class="px-3 py-1.5 text-sm text-gray-500">{{ currentPage }} / {{ Math.ceil(total / 10) }}</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= Math.ceil(total / 10)" class="px-3 py-1.5 border rounded-md text-sm disabled:opacity-30">下一页</button>
    </div>
  </div>
</template>
