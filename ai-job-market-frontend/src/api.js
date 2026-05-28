const BASE = '/api'

function authHeaders() {
  const token = localStorage.getItem('token')
  return token ? { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' }
}

async function request(url, options = {}) {
  const res = await fetch(BASE + url, { ...options, headers: { ...authHeaders(), ...options.headers } })
  const data = await res.json()
  if (data.code !== 0) throw new Error(data.message || '请求失败')
  return data.data
}

// 用户
export const userApi = {
  login: (body) => request('/user/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => request('/user/register', { method: 'POST', body: JSON.stringify(body) }),
  current: () => request('/user/current'),
  update: (body) => request('/user/update', { method: 'PUT', body: JSON.stringify(body) }),
}

// 简历
export const resumeApi = {
  list: (params = {}) => request('/resumes?' + new URLSearchParams(params)),
  detail: (id) => request('/resumes/' + id),
  create: (body) => request('/resumes', { method: 'POST', body: JSON.stringify(body) }),
  update: (id, body) => request('/resumes/' + id, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (id) => request('/resumes/' + id, { method: 'DELETE' }),
  setDefault: (id) => request('/resumes/' + id + '/default', { method: 'PUT' }),

  // 子项
  addItem: (resumeId, type, body) => request('/resumes/' + resumeId + '/' + type, { method: 'POST', body: JSON.stringify(body) }),
  updateItem: (resumeId, type, itemId, body) => request('/resumes/' + resumeId + '/' + type + '/' + itemId, { method: 'PUT', body: JSON.stringify(body) }),
  deleteItem: (resumeId, type, itemId) => request('/resumes/' + resumeId + '/' + type + '/' + itemId, { method: 'DELETE' }),

  // AI
  aiAnalyze: (id) => request('/resumes/' + id + '/ai-analyze', { method: 'POST' }),
  aiOptimize: (id) => request('/resumes/' + id + '/ai-optimize', { method: 'POST' }),
  exportPdf: (id) => BASE + '/resumes/' + id + '/export-pdf',
  uploadPdf: async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    const res = await fetch(BASE + '/resumes/upload-pdf', {
      method: 'POST',
      headers: token ? { 'Authorization': 'Bearer ' + token } : {},
      body: formData
    })
    const data = await res.json()
    if (data.code !== 0) throw new Error(data.message || '上传失败')
    return data.data
  },
}

// 通知
export const notificationApi = {
  list: (params = {}) => request('/notifications?' + new URLSearchParams(params)),
  unreadCount: () => request('/notifications/unread-count'),
  markRead: (id) => request('/notifications/' + id + '/read', { method: 'PUT' }),
  markAllRead: () => request('/notifications/read-all', { method: 'PUT' }),
  delete: (id) => request('/notifications/' + id, { method: 'DELETE' }),
}

// 私信
export const messageApi = {
  conversations: () => request('/messages/conversations'),
  getMessages: (peerId, params = {}) => request('/messages/' + peerId + '?' + new URLSearchParams(params)),
  send: (body) => request('/messages', { method: 'POST', body: JSON.stringify(body) }),
  unreadCount: () => request('/messages/unread-count'),
  markRead: (id) => request('/messages/' + id + '/read', { method: 'PUT' }),
}

export const jobApi = {
  list: (params = {}) => request('/jobs?' + new URLSearchParams(params)),
  detail: (id) => request('/jobs/' + id),
  create: (body) => request('/jobs', { method: 'POST', body: JSON.stringify(body) }),
  update: (id, body) => request('/jobs/' + id, { method: 'PUT', body: JSON.stringify(body) }),
  publish: (id) => request('/jobs/' + id + '/publish', { method: 'PUT' }),
  close: (id) => request('/jobs/' + id + '/close', { method: 'PUT' }),
  myJobs: (params = {}) => request('/jobs/my?' + new URLSearchParams(params)),
  recommend: (resumeId) => request('/jobs/recommend?resumeId=' + (resumeId || '')),
  addSkillTag: (jobId, skillName, isRequired) => request('/jobs/' + jobId + '/skill-tags?skillName=' + encodeURIComponent(skillName) + '&isRequired=' + isRequired, { method: 'POST' }),
}
