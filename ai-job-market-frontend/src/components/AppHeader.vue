<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token'))
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

const isLoggedIn = computed(() => !!token.value)
const role = computed(() => user.value?.role || '')

// 路由变化时重新读取登录状态（响应 Login/Logout 变化）
function refreshAuth() {
  token.value = localStorage.getItem('token')
  user.value = JSON.parse(localStorage.getItem('user') || 'null')
}

onMounted(() => window.addEventListener('focus', refreshAuth))
onUnmounted(() => window.removeEventListener('focus', refreshAuth))

// 监听路由变化
import { watch } from 'vue'
watch(() => route.path, refreshAuth)

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  token.value = null
  user.value = null
  router.push('/')
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link :to="isLoggedIn ? (role === 'ADMIN' ? '/admin' : role === 'RECRUITER' ? '/recruiter' : '/seeker') : '/'"
          class="flex items-center gap-2 text-xl font-bold text-gray-900 no-underline">
          <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
            <span class="text-white text-sm font-bold">AI</span>
          </div>
          <span class="hidden sm:inline">AI 招聘市场</span>
        </router-link>

        <!-- Nav: 求职者 -->
        <nav v-if="role === 'SEEKER'" class="hidden md:flex items-center gap-8">
          <router-link to="/seeker" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">首页</router-link>
          <router-link to="/jobs" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">职位搜索</router-link>
          <router-link to="/resumes" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">我的简历</router-link>
          <router-link to="/applications" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">投递记录</router-link>
          <router-link to="/favorites" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">收藏</router-link>
        </nav>

        <!-- Nav: 招聘方 -->
        <nav v-else-if="role === 'RECRUITER'" class="hidden md:flex items-center gap-8">
          <router-link to="/recruiter" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">工作台</router-link>
          <router-link to="/recruiter/company" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">公司信息</router-link>
          <router-link to="/recruiter/jobs" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">职位管理</router-link>
          <router-link to="/recruiter/applications" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">简历筛选</router-link>
        </nav>

        <!-- Nav: 管理员 -->
        <nav v-else-if="role === 'ADMIN'" class="hidden md:flex items-center gap-8">
          <router-link to="/admin" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">仪表盘</router-link>
          <router-link to="/admin/users" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">用户管理</router-link>
          <router-link to="/admin/companies" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">企业审核</router-link>
        </nav>

        <!-- Nav: 未登录 -->
        <nav v-else class="hidden md:flex items-center gap-8">
          <router-link to="/" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">求职</router-link>
          <router-link to="/jobs" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">招聘</router-link>
        </nav>

        <!-- Auth -->
        <div class="flex items-center gap-3">
          <template v-if="isLoggedIn">
            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
              :class="role === 'ADMIN' ? 'bg-red-50 text-red-600' : role === 'RECRUITER' ? 'bg-purple-50 text-purple-600' : 'bg-blue-50 text-blue-600'">
              {{ role === 'ADMIN' ? '管理员' : role === 'RECRUITER' ? '招聘方' : '求职者' }}
            </span>
            <span class="text-sm text-gray-600 hidden sm:inline">{{ user?.nickname }}</span>
            <button @click="logout"
              class="text-sm px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors">
              退出
            </button>
          </template>
          <template v-else>
            <router-link to="/login"
              class="text-sm px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 transition-colors no-underline">
              登录
            </router-link>
            <router-link to="/register"
              class="text-sm px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors no-underline">
              注册
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>
