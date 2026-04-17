import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 요청 인터셉터: JWT 토큰 자동 첨부
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 응답 인터셉터: 401 시 자동 로그아웃
api.interceptors.response.use(
  res => res,
  async err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const authApi = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }),
}

export const stationApi = {
  list: (params) => api.get('/stations', { params }),
  get: (id) => api.get(`/stations/${id}`),
  create: (data) => api.post('/stations', data),
  updateStatus: (id, status) => api.patch(`/stations/${id}/status`, null, { params: { status } }),
}

export const alertApi = {
  listActive: (page = 0, size = 20) =>
    api.get('/alerts', { params: { page, size } }),
  listByStation: (stationId, page = 0, size = 20) =>
    api.get(`/alerts/station/${stationId}`, { params: { page, size } }),
  acknowledge: (id) => api.post(`/alerts/${id}/acknowledge`),
  resolve: (id) => api.post(`/alerts/${id}/resolve`),
}

export const dashboardApi = {
  getKpi: () => api.get('/dashboard/kpi'),
  getCriticalAlerts: () => api.get('/dashboard/critical-alerts'),
}

export default api
