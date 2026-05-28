<script setup>
defineProps({ job: { type: Object, required: true } })
const emit = defineEmits(['apply', 'click', 'favorite'])
</script>

<template>
  <div @click="emit('click', job.id)"
    class="bg-white border border-gray-200 rounded-lg p-5 hover:shadow-md hover:border-blue-200 transition-all cursor-pointer">
    <div class="flex items-start justify-between">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-500 text-xs font-bold">
          {{ (job.company || 'C').charAt(0) }}
        </div>
        <div>
          <h3 class="text-base font-semibold text-gray-900">{{ job.title }}</h3>
          <p class="text-sm text-gray-500 mt-0.5">{{ job.company }}</p>
        </div>
      </div>
      <span v-if="job.matchScore"
        class="inline-flex items-center gap-1 px-2.5 py-1 bg-green-50 text-green-700 text-xs font-medium rounded-full">
        <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
        </svg>
        AI 匹配 {{ job.matchScore }}%
      </span>
    </div>

    <div class="mt-3 flex flex-wrap items-center gap-3 text-sm text-gray-500">
      <span class="inline-flex items-center gap-1"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>{{ job.city }}</span>
      <span class="inline-flex items-center gap-1"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>{{ job.salary }}</span>
      <span class="inline-flex items-center gap-1"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>{{ job.experience || '不限' }}</span>
    </div>

    <div class="mt-3 flex flex-wrap gap-1.5">
      <span v-for="tag in (job.tags || [])" :key="tag" class="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded">{{ tag }}</span>
    </div>

    <div class="mt-3 flex items-center justify-between">
      <span class="text-xs text-gray-400">{{ job.postedTime || '近期发布' }}</span>
      <div class="flex items-center gap-2">
        <button @click.stop="emit('favorite', job.id)"
          class="text-xs font-medium text-yellow-600 hover:text-white px-3 py-1.5 bg-yellow-50 hover:bg-yellow-500 rounded-md transition-colors">
          收藏
        </button>
        <button @click.stop="emit('apply', job.id)"
          class="text-xs font-medium text-blue-600 hover:text-white px-3 py-1.5 bg-blue-50 hover:bg-blue-600 rounded-md transition-colors">
          立即投递
        </button>
      </div>
    </div>
  </div>
</template>
