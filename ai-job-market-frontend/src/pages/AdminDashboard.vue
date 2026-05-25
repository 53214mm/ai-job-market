<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({ users: 0, companies: 0, jobs: 0, unverified: 0 })

onMounted(async () => {
  const token = localStorage.getItem('token')
  const h = token ? { 'Authorization': 'Bearer ' + token } : {}
  try {
    const [uRes, cRes, jRes] = await Promise.all([
      fetch('/api/user/list?current=1&pageSize=1', { headers: h }),
      fetch('/api/companies?current=1&pageSize=100', { headers: h }),
      fetch('/api/jobs?current=1&pageSize=1', { headers: h })
    ])
    const [u, c, j] = await Promise.all([uRes.json(), cRes.json(), jRes.json()])
    if (u.code === 0) stats.value.users = u.data.total || 0
    if (c.code === 0) {
      stats.value.companies = c.data.total || 0
      stats.value.unverified = (c.data.records || []).filter(x => !x.verified).length
    }
    if (j.code === 0) stats.value.jobs = j.data.total || 0
  } catch (e) { /* */ }
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-gradient-to-r from-red-600 to-red-700 rounded-xl p-6 mb-8 text-white">
      <h1 class="text-2xl font-bold mb-1">管理后台</h1>
      <p class="text-red-100 text-sm">{{ user.nickname || '管理员' }}，欢迎回来</p>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-blue-600">{{ stats.users }}</p>
        <p class="text-xs text-gray-500 mt-1">用户总数</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-purple-600">{{ stats.companies }}</p>
        <p class="text-xs text-gray-500 mt-1">企业数量</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-green-600">{{ stats.jobs }}</p>
        <p class="text-xs text-gray-500 mt-1">职位数量</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold" :class="stats.unverified > 0 ? 'text-orange-600' : 'text-gray-400'">{{ stats.unverified }}</p>
        <p class="text-xs text-gray-500 mt-1">待审核企业</p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <router-link to="/admin/users" class="block bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md hover:border-red-200 transition-all no-underline">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-red-50 rounded-lg flex items-center justify-center text-red-600">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 text-sm">用户管理</h3>
            <p class="text-xs text-gray-500">管理平台注册用户，查看列表，禁用违规账号</p>
          </div>
        </div>
        <div class="flex gap-4 text-xs text-gray-400 mt-3">
          <span>{{ stats.users }} 用户</span>
        </div>
      </router-link>

      <router-link to="/admin/companies" class="block bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md hover:border-red-200 transition-all no-underline">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-purple-50 rounded-lg flex items-center justify-center text-purple-600">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 text-sm">企业审核</h3>
            <p class="text-xs text-gray-500">审核企业认证，管理企业信息和评价</p>
          </div>
        </div>
        <div class="flex gap-4 text-xs text-gray-400 mt-3">
          <span>{{ stats.companies }} 企业</span>
          <span class="text-orange-500" v-if="stats.unverified > 0">{{ stats.unverified }} 待审核</span>
        </div>
      </router-link>

      <router-link to="/admin/jobs" class="block bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md hover:border-red-200 transition-all no-underline">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-green-50 rounded-lg flex items-center justify-center text-green-600">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 text-sm">职位管理</h3>
            <p class="text-xs text-gray-500">查看所有职位，审核和下架违规职位</p>
          </div>
        </div>
        <div class="flex gap-4 text-xs text-gray-400 mt-3">
          <span>{{ stats.jobs }} 职位</span>
        </div>
      </router-link>

      <router-link to="/admin/articles" class="block bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md hover:border-red-200 transition-all no-underline">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center text-blue-600">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 text-sm">内容管理</h3>
            <p class="text-xs text-gray-500">管理求职攻略、行业资讯等内容</p>
          </div>
        </div>
      </router-link>
    </div>
  </div>
</template>
