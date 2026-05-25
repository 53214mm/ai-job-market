<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ email: '', password: '', checkPassword: '', role: 'SEEKER', nickname: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  if (form.value.password !== form.value.checkPassword) {
    error.value = '两次密码不一致'
    return
  }
  loading.value = true
  try {
    const res = await fetch('/api/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })
    const data = await res.json()
    if (data.code === 0) {
      router.push('/login')
    } else {
      error.value = data.message || '注册失败'
    }
  } catch (e) {
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4 py-8">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-4">
          <span class="text-white text-xl font-bold">AI</span>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">创建账号</h1>
        <p class="text-sm text-gray-500 mt-1">加入 AI 智能招聘市场</p>
      </div>

      <div class="bg-white border border-gray-200 rounded-xl p-6 shadow-sm space-y-4">
        <div v-if="error" class="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
          {{ error }}
        </div>

        <!-- Role -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">注册身份</label>
          <div class="grid grid-cols-2 gap-2">
            <button type="button" @click="form.role = 'SEEKER'"
              :class="form.role === 'SEEKER' ? 'bg-blue-50 border-blue-500 text-blue-700' : 'bg-white border-gray-200 text-gray-600'"
              class="px-4 py-2.5 border rounded-md text-sm font-medium transition-colors">
              求职者
            </button>
            <button type="button" @click="form.role = 'RECRUITER'"
              :class="form.role === 'RECRUITER' ? 'bg-blue-50 border-blue-500 text-blue-700' : 'bg-white border-gray-200 text-gray-600'"
              class="px-4 py-2.5 border rounded-md text-sm font-medium transition-colors">
              招聘方
            </button>
          </div>
        </div>

        <!-- Email -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">邮箱</label>
          <input v-model="form.email" type="email" placeholder="请输入邮箱"
            class="w-full px-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>

        <!-- Nickname -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">昵称</label>
          <input v-model="form.nickname" type="text" placeholder="请输入昵称"
            class="w-full px-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>

        <!-- Password -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
          <input v-model="form.password" type="password" placeholder="至少6位密码"
            class="w-full px-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>

        <!-- Confirm Password -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1.5">确认密码</label>
          <input v-model="form.checkPassword" type="password" placeholder="再次输入密码"
            class="w-full px-3 py-2.5 border border-gray-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>

        <button @click="handleRegister" :disabled="loading"
          class="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white text-sm font-medium rounded-md transition-colors">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <p class="text-center text-sm text-gray-500">
          已有账号？
          <router-link to="/login" class="text-blue-600 hover:text-blue-700 font-medium">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
