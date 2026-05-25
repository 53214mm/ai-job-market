<script setup>
defineProps({ resume: Object })

const emit = defineEmits(['delete', 'set-default', 'view', 'edit'])
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all">
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <div class="flex items-center gap-2 mb-2">
          <h3 class="text-base font-semibold text-gray-900">{{ resume.title }}</h3>
          <span v-if="resume.isDefault" class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full font-medium">默认</span>
          <span class="px-2 py-0.5 text-xs rounded-full font-medium"
            :class="resume.privacy === 'PUBLIC' ? 'bg-green-50 text-green-600' : resume.privacy === 'APPLICATION_ONLY' ? 'bg-yellow-50 text-yellow-600' : 'bg-gray-100 text-gray-500'">
            {{ resume.privacy === 'PUBLIC' ? '公开' : resume.privacy === 'APPLICATION_ONLY' ? '仅投递可见' : '私密' }}
          </span>
        </div>
        <p class="text-sm text-gray-500 mb-1">{{ resume.fullName }} · {{ resume.currentCity || '未填写' }}</p>
        <p class="text-sm text-gray-400">更新于 {{ resume.updatedAt?.slice(0, 10) || '-' }}</p>
      </div>
      <!-- AI Score -->
      <div v-if="resume.aiScore" class="text-center ml-4">
        <div class="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center">
          <span class="text-sm font-bold text-blue-600">{{ resume.aiScore }}</span>
        </div>
        <p class="text-xs text-gray-400 mt-1">AI 评分</p>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-2 mt-4 pt-3 border-t border-gray-100">
      <button @click="emit('view', resume.id)"
        class="text-xs px-3 py-1.5 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">查看</button>
      <button @click="emit('edit', resume.id)"
        class="text-xs px-3 py-1.5 border border-gray-200 text-gray-600 rounded-md hover:bg-gray-50 transition-colors">编辑</button>
      <div class="flex-1"></div>
      <button v-if="!resume.isDefault" @click="emit('set-default', resume.id)"
        class="text-xs px-2 py-1.5 text-gray-400 hover:text-blue-600 transition-colors">设为默认</button>
      <button @click="emit('delete', resume.id)"
        class="text-xs px-2 py-1.5 text-gray-400 hover:text-red-600 transition-colors">删除</button>
    </div>
  </div>
</template>
