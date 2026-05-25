<script setup>
defineProps({
  company: { type: Object, required: true }
})
</script>

<template>
  <div class="bg-white border border-gray-200 rounded-lg p-5 text-center hover:shadow-md hover:border-gray-300 transition-all cursor-pointer">
    <div class="w-14 h-14 mx-auto bg-gray-100 rounded-xl flex items-center justify-center text-gray-500 text-xl font-bold mb-3">
      {{ company.name?.charAt(0) || 'C' }}
    </div>
    <p class="text-sm font-medium text-gray-900 mb-1">{{ company.name }}</p>
    <p class="text-xs text-gray-400">{{ company.jobCount || 0 }} 个在招职位</p>
  </div>
</template>
