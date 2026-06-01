<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStomp } from '../composables/useStomp.js'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token'))
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

const isLoggedIn = computed(() => !!token.value)
const role = computed(() => user.value?.role || '')

const { connect, onUnreadCount } = useStomp()

// 通知和私信分开计数
const unreadNotif = ref(0)
const unreadMsg = ref(0)
let unsubUnread = null

async function fetchUnread() {
  const t = localStorage.getItem('token')
  if (!t) { unreadNotif.value = 0; unreadMsg.value = 0; return }
  const h = { 'Authorization': 'Bearer ' + t }
  try {
    const [nr, mr] = await Promise.all([
      fetch('/api/notifications/unread-count', { headers: h }),
      fetch('/api/messages/unread-count', { headers: h })
    ])
    const nd = await nr.json(); const md = await mr.json()
    unreadNotif.value = nd.data?.count || 0
    unreadMsg.value = md.data?.count || 0
  } catch(e) { unreadNotif.value = 0; unreadMsg.value = 0 }
}

// 路由变化或页面聚焦时重新读取登录状态
function refreshAuth() {
  token.value = localStorage.getItem('token')
  user.value = JSON.parse(localStorage.getItem('user') || 'null')
}

function onUnreadChanged() { fetchUnread() }

onMounted(async () => {
  window.addEventListener('focus', refreshAuth)
  window.addEventListener('unread-changed', onUnreadChanged)
  // 初始 REST 拉取
  await fetchUnread()
  // 先注册回调，再建立 WebSocket 连接——防止 STOMP 订阅建立后回调注册前的推送丢失
  unsubUnread = onUnreadCount(() => {
    fetchUnread()
  })
  try {
    await connect()
  } catch(e) { /* STOMP 连接失败，仅依赖 REST 轮询 */ }
})

onUnmounted(() => {
  window.removeEventListener('focus', refreshAuth)
  window.removeEventListener('unread-changed', onUnreadChanged)
  if (unsubUnread) unsubUnread()
})

watch(() => route.path, () => { refreshAuth(); fetchUnread() })

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  token.value = null
  user.value = null
  unreadNotif.value = 0
  unreadMsg.value = 0
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
          <router-link to="/ai/interview" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">AI面试</router-link>
          <router-link to="/ai/chat" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">AI助手</router-link>
          <router-link to="/notifications" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">通知
            <span v-if="unreadNotif > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadNotif > 99 ? '99+' : unreadNotif }}</span>
          </router-link>
          <router-link to="/messages" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">私信
            <span v-if="unreadMsg > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadMsg > 99 ? '99+' : unreadMsg }}</span>
          </router-link>
        </nav>

        <!-- Nav: 招聘方 -->
        <nav v-else-if="role === 'RECRUITER'" class="hidden md:flex items-center gap-8">
          <router-link to="/recruiter" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">工作台</router-link>
          <router-link to="/recruiter/company" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">公司信息</router-link>
          <router-link to="/recruiter/jobs" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">职位管理</router-link>
          <router-link to="/recruiter/applications" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">简历筛选</router-link>
          <router-link to="/notifications" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">通知
            <span v-if="unreadNotif > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadNotif > 99 ? '99+' : unreadNotif }}</span>
          </router-link>
          <router-link to="/messages" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">私信
            <span v-if="unreadMsg > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadMsg > 99 ? '99+' : unreadMsg }}</span>
          </router-link>
          <router-link to="/ai/chat" class="text-sm font-medium text-purple-600 hover:text-purple-700 transition-colors">AI助手</router-link>
        </nav>

        <!-- Nav: 管理员 -->
        <nav v-else-if="role === 'ADMIN'" class="hidden md:flex items-center gap-8">
          <router-link to="/admin" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">仪表盘</router-link>
          <router-link to="/admin/users" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">用户管理</router-link>
          <router-link to="/admin/companies" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">企业审核</router-link>
          <router-link to="/notifications" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">通知
            <span v-if="unreadNotif > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadNotif > 99 ? '99+' : unreadNotif }}</span>
          </router-link>
          <router-link to="/messages" class="relative text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">私信
            <span v-if="unreadMsg > 0" class="absolute -top-1.5 -right-3.5 min-w-[16px] h-4 bg-red-500 text-white text-[10px] rounded-full flex items-center justify-center px-1">{{ unreadMsg > 99 ? '99+' : unreadMsg }}</span>
          </router-link>
          <router-link to="/ai/chat" class="text-sm font-medium text-purple-600 hover:text-purple-700 transition-colors">AI助手</router-link>
        </nav>

        <!-- Nav: 未登录 -->
        <nav v-else class="hidden md:flex items-center gap-8">
          <router-link to="/" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">求职</router-link>
          <router-link to="/jobs" class="text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors">招聘</router-link>
          <router-link to="/ai/chat" class="text-sm font-medium text-purple-600 hover:text-purple-700 transition-colors">AI助手</router-link>
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
