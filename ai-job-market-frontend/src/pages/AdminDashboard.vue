<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({ users: '-', companies: '-', jobs: '-', pending: '-' })

onMounted(async () => {
  // TODO: 添加管理统计 API 后对接
  // const res = await fetch('/api/admin/stats')
  // stats.value = await res.json()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-gradient-to-r from-red-600 to-red-700 rounded-xl p-6 mb-8 text-white">
      <h1 class="text-2xl font-bold mb-1">管理后台</h1>
      <p class="text-red-100 text-sm">{{ user.nickname || '管理员' }}，欢迎回来</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-gray-700">{{ stats.users }}</p><p class="text-xs text-gray-500 mt-1">用户总数</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-gray-700">{{ stats.companies }}</p><p class="text-xs text-gray-500 mt-1">企业数量</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-gray-700">{{ stats.jobs }}</p><p class="text-xs text-gray-500 mt-1">职位数量</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-4 text-center shadow-sm">
        <p class="text-2xl font-bold text-gray-700">{{ stats.pending }}</p><p class="text-xs text-gray-500 mt-1">待审核企业</p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div @click="router.push('/admin/users')" class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md cursor-pointer transition-all">
        <h3 class="font-semibold text-gray-900 mb-1">用户管理</h3>
        <p class="text-xs text-gray-500">管理平台注册用户，禁用违规账号</p>
      </div>
      <div @click="router.push('/admin/companies')" class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md cursor-pointer transition-all">
        <h3 class="font-semibold text-gray-900 mb-1">企业审核</h3>
        <p class="text-xs text-gray-500">审核企业认证申请，确保信息真实</p>
      </div>
      <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm hover:shadow-md cursor-pointer transition-all">
        <h3 class="font-semibold text-gray-900 mb-1">职位管理</h3>
        <p class="text-xs text-gray-500">审核和下架违规职位发布</p>
      </div>
    </div>
  </div>
</template>
