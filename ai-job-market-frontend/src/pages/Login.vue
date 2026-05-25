<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await fetch('/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    })
    const data = await res.json()
    if (data.code === 0) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('user', JSON.stringify(data.data.user))
      // 按角色跳转
      const role = data.data.user.role
      if (role === 'ADMIN') router.push('/admin')
      else if (role === 'RECRUITER') router.push('/recruiter')
      else router.push('/seeker')
    } else {
      error.value = data.message || '登录失败'
    }
  } catch (e) {
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-4">
          <span class="text-white text-xl font-bold">AI</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">欢迎回来</h1>
        <p class="text-sm text-gray-500 mt-1">登录 AI 智能招聘市场</p>
      </div>

      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm space-y-4">
        <!-- Error -->
        <div v-if="error" class="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
          {{ error }}
        </div>

        <!-- Email -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">邮箱</label>
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
            <input v-model="email" type="email" placeholder="请输入邮箱"
              class="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
        </div>

        <!-- Password -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
          <div class="relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <input v-model="password" type="password" placeholder="请输入密码"
              class="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
        </div>

        <!-- Submit -->
        <button @click="handleLogin" :disabled="loading"
          class="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-md transition-colors">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <!-- Register link -->
        <p class="text-center text-sm text-gray-500">
          还没有账号？
          <router-link to="/register" class="text-blue-600 hover:text-blue-700 font-medium">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
