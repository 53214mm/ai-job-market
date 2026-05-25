<script setup>
import { ref } from 'vue'
import SearchBar from '../components/SearchBar.vue'
import JobCard from '../components/JobCard.vue'

const jobs = ref([
  {
    id: 1, title: '高级 Java 开发工程师', company: '阿里巴巴', city: '杭州',
    salary: '25K-45K', experience: '3-5年',
    tags: ['Java', 'Spring Boot', '微服务', 'MySQL'],
    matchScore: 92, postedTime: '2小时前'
  },
  {
    id: 2, title: 'AI 算法工程师', company: '百度', city: '北京',
    salary: '35K-60K', experience: '3-5年',
    tags: ['Python', 'PyTorch', 'NLP', '大模型'],
    matchScore: 88, postedTime: '4小时前'
  },
  {
    id: 3, title: '前端开发工程师', company: '字节跳动', city: '上海',
    salary: '30K-50K', experience: '1-3年',
    tags: ['Vue.js', 'React', 'TypeScript', 'Node.js'],
    matchScore: 85, postedTime: '6小时前'
  },
  {
    id: 4, title: '产品经理（AI 方向）', company: '腾讯', city: '深圳',
    salary: '28K-48K', experience: '3-5年',
    tags: ['AI', 'B端产品', '数据分析'],
    matchScore: 79, postedTime: '8小时前'
  },
  {
    id: 5, title: 'DevOps 运维工程师', company: '华为', city: '成都',
    salary: '20K-35K', experience: '3-5年',
    tags: ['Kubernetes', 'Docker', 'CI/CD', 'Linux'],
    matchScore: 75, postedTime: '12小时前'
  },
  {
    id: 6, title: '数据分析师', company: '美团', city: '北京',
    salary: '18K-30K', experience: '1-3年',
    tags: ['SQL', 'Python', 'Tableau', '数据仓库'],
    matchScore: 82, postedTime: '1天前'
  },
  {
    id: 7, title: '测试开发工程师', company: '小米', city: '北京',
    salary: '22K-38K', experience: '3-5年',
    tags: ['自动化测试', 'Python', 'Selenium'],
    matchScore: 71, postedTime: '1天前'
  },
  {
    id: 8, title: '技术总监', company: '网易', city: '广州',
    salary: '50K-80K', experience: '5-10年',
    tags: ['架构设计', '团队管理', 'Java', '云原生'],
    matchScore: 68, postedTime: '2天前'
  }
])

const sortBy = ref('match')

function handleSearch(params) {
  console.log('Search:', params)
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">职位搜索</h1>

    <SearchBar @search="handleSearch" />

    <!-- Sort & Count -->
    <div class="flex items-center justify-between mt-6 mb-4">
      <p class="text-sm text-gray-500">共找到 <span class="font-semibold text-gray-900">{{ jobs.length }}</span> 个职位</p>
      <div class="flex items-center gap-2">
        <span class="text-xs text-gray-500">排序：</span>
        <select v-model="sortBy"
          class="text-sm border border-gray-200 rounded-md px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500">
          <option value="match">AI 匹配度</option>
          <option value="time">发布时间</option>
          <option value="salary">薪资高低</option>
        </select>
      </div>
    </div>

    <!-- Job Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <JobCard v-for="job in jobs" :key="job.id" :job="job" />
    </div>

    <!-- Pagination -->
    <div class="flex items-center justify-center gap-2 mt-8">
      <button class="px-3 py-1.5 border border-gray-200 rounded-md text-sm text-gray-500 hover:bg-gray-50 transition-colors">上一页</button>
      <button class="w-8 h-8 bg-blue-600 text-white text-sm rounded-md font-medium">1</button>
      <button class="w-8 h-8 border border-gray-200 rounded-md text-sm text-gray-600 hover:bg-gray-50 transition-colors">2</button>
      <button class="w-8 h-8 border border-gray-200 rounded-md text-sm text-gray-600 hover:bg-gray-50 transition-colors">3</button>
      <button class="px-3 py-1.5 border border-gray-200 rounded-md text-sm text-gray-500 hover:bg-gray-50 transition-colors">下一页</button>
    </div>
  </div>
</template>
