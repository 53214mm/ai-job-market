<script setup>
import { ref } from 'vue'
import SearchBar from '../components/SearchBar.vue'
import JobCard from '../components/JobCard.vue'
import AIMatchCard from '../components/AIMatchCard.vue'
import CompanyCard from '../components/CompanyCard.vue'

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
  }
])

const matchReason = ref({
  '技能匹配': 92,
  '经验匹配': 88,
  '学历匹配': 95,
  '薪资匹配': 80,
  '地点匹配': 85
})

const companies = ref([
  { name: '阿里巴巴', jobCount: 128 },
  { name: '腾讯', jobCount: 96 },
  { name: '字节跳动', jobCount: 85 },
  { name: '百度', jobCount: 72 },
  { name: '华为', jobCount: 64 },
  { name: '美团', jobCount: 58 },
])

function handleSearch(params) {
  console.log('Search params:', params)
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="bg-gradient-to-b from-blue-50 to-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="text-center mb-8">
          <h1 class="text-3xl sm:text-4xl font-bold text-gray-900 mb-3 tracking-tight">
            找到你的<span class="text-blue-600">理想工作</span>
          </h1>
          <p class="text-base text-gray-500 max-w-xl mx-auto">
            AI 智能匹配，让好工作主动找到你。已为 <span class="font-semibold text-gray-700">10,000+</span> 求职者精准推荐
          </p>
        </div>
        <SearchBar @search="handleSearch" />
        <!-- Quick tags -->
        <div class="flex flex-wrap justify-center gap-2 mt-4">
          <span class="text-xs text-gray-400">热门搜索：</span>
          <button class="text-xs px-3 py-1 bg-white border border-gray-200 rounded-full text-gray-600 hover:border-blue-300 hover:text-blue-600 transition-colors">Java</button>
          <button class="text-xs px-3 py-1 bg-white border border-gray-200 rounded-full text-gray-600 hover:border-blue-300 hover:text-blue-600 transition-colors">前端</button>
          <button class="text-xs px-3 py-1 bg-white border border-gray-200 rounded-full text-gray-600 hover:border-blue-300 hover:text-blue-600 transition-colors">AI 算法</button>
          <button class="text-xs px-3 py-1 bg-white border border-gray-200 rounded-full text-gray-600 hover:border-blue-300 hover:text-blue-600 transition-colors">远程工作</button>
        </div>
      </div>
    </section>

    <!-- Main Content: Two-Column Layout -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-5">
        <h2 class="text-lg font-semibold text-gray-900">AI 智能推荐职位</h2>
        <button class="text-sm text-blue-600 hover:text-blue-700 font-medium">查看全部 &rarr;</button>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Left: Job Listings -->
        <div class="lg:col-span-2 space-y-4">
          <JobCard v-for="job in jobs" :key="job.id" :job="job" />
        </div>

        <!-- Right: AI Match + Sidebar -->
        <div class="space-y-5">
          <AIMatchCard :reason="matchReason" />

          <!-- Quick actions -->
          <div class="bg-white border border-gray-200 rounded-lg p-5 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-900 mb-3">快速入口</h3>
            <div class="grid grid-cols-2 gap-2">
              <button class="text-xs text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md transition-colors">
                我的简历
              </button>
              <button class="text-xs text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md transition-colors">
                投递记录
              </button>
              <button class="text-xs text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md transition-colors">
                我的收藏
              </button>
              <button class="text-xs text-left px-3 py-2.5 bg-gray-50 hover:bg-blue-50 hover:text-blue-700 rounded-md transition-colors">
                AI 求职助手
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Companies -->
    <section class="bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-gray-900">热门企业</h2>
          <button class="text-sm text-blue-600 hover:text-blue-700 font-medium">查看全部 &rarr;</button>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4">
          <CompanyCard v-for="c in companies" :key="c.name" :company="c" />
        </div>
      </div>
    </section>

    <!-- Stats Banner -->
    <section class="bg-blue-600">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
          <div>
            <p class="text-2xl font-bold text-white">10,000+</p>
            <p class="text-sm text-blue-100 mt-1">活跃求职者</p>
          </div>
          <div>
            <p class="text-2xl font-bold text-white">5,000+</p>
            <p class="text-sm text-blue-100 mt-1">合作企业</p>
          </div>
          <div>
            <p class="text-2xl font-bold text-white">20,000+</p>
            <p class="text-sm text-blue-100 mt-1">在招职位</p>
          </div>
          <div>
            <p class="text-2xl font-bold text-white">92%</p>
            <p class="text-sm text-blue-100 mt-1">AI 推荐精准度</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
