<script setup>
defineProps({
  reason: { type: Object, default: () => ({}) }
})
</script>

<template>
  <div class="bg-white border border-blue-100 rounded-lg p-5 sticky top-20 shadow-sm">
    <div class="flex items-center gap-2 mb-3">
      <div class="w-5 h-5 bg-blue-600 rounded flex items-center justify-center">
        <svg class="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
        </svg>
      </div>
      <h3 class="text-base font-semibold text-gray-900">AI 推荐理由</h3>
    </div>

    <p class="text-sm text-gray-600 leading-relaxed mb-4">
      基于您的简历和求职偏好，以下职位与您的匹配度最高。
    </p>

    <div class="space-y-3">
      <div v-for="(item, key) in reason" :key="key" class="flex items-center justify-between">
        <span class="text-sm text-gray-600">{{ key }}</span>
        <div class="flex items-center gap-2">
          <div class="w-24 h-1.5 bg-gray-200 rounded-full overflow-hidden">
            <div class="h-full bg-blue-600 rounded-full" :style="{ width: item + '%' }"></div>
          </div>
          <span class="text-xs font-medium text-gray-500 w-8 text-right">{{ item }}%</span>
        </div>
      </div>
    </div>

    <div class="mt-5 p-3 bg-blue-50 rounded-lg">
      <p class="text-xs text-blue-700 leading-relaxed">
        <span class="font-semibold">AI 提示：</span>
        您的技能与当前热门岗位高度匹配，建议优先投递匹配度 85% 以上的职位，提高面试邀约率。
      </p>
    </div>
  </div>
</template>
