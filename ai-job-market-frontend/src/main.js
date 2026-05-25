import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

import Home from './pages/Home.vue'
import Login from './pages/Login.vue'
import Register from './pages/Register.vue'
import Jobs from './pages/Jobs.vue'

// 简历
import ResumeList from './pages/ResumeList.vue'
import ResumeDetail from './pages/ResumeDetail.vue'
import ResumeEdit from './pages/ResumeEdit.vue'
import ResumeCreate from './pages/ResumeCreate.vue'

// 角色仪表盘
import SeekerDashboard from './pages/SeekerDashboard.vue'
import RecruiterDashboard from './pages/RecruiterDashboard.vue'
import AdminDashboard from './pages/AdminDashboard.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/jobs', name: 'Jobs', component: Jobs },

  // 求职者
  { path: '/seeker', name: 'SeekerDashboard', component: SeekerDashboard },
  { path: '/resumes', name: 'ResumeList', component: ResumeList },
  { path: '/resumes/create', name: 'ResumeCreate', component: ResumeCreate },
  { path: '/resumes/:id', name: 'ResumeDetail', component: ResumeDetail },
  { path: '/resumes/:id/edit', name: 'ResumeEdit', component: ResumeEdit },

  // 招聘方
  { path: '/recruiter', name: 'RecruiterDashboard', component: RecruiterDashboard },
  { path: '/recruiter/jobs', name: 'RecruiterJobs', component: () => import('./pages/Jobs.vue') },
  { path: '/recruiter/applications', name: 'RecruiterApplications', component: () => import('./pages/RecruiterDashboard.vue') },

  // 管理员
  { path: '/admin', name: 'AdminDashboard', component: AdminDashboard },
  { path: '/admin/users', name: 'AdminUsers', component: AdminDashboard },
  { path: '/admin/companies', name: 'AdminCompanies', component: AdminDashboard },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const app = createApp(App)
app.use(router)
app.mount('#app')
