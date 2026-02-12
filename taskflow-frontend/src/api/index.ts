import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

let isRefreshing = false
let failedQueue: Array<{
  resolve: (token: string) => void
  reject: (error: any) => void
}> = []

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token!)
    }
  })
  failedQueue = []
}

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const authStore = JSON.parse(localStorage.getItem('auth') || '{}')
  const token = authStore?.accessToken
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || 'Request failed')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          throw new Error('No refresh token')
        }

        const response = await axios.post('/api/v1/auth/refresh', { refreshToken })
        const { accessToken, refreshToken: newRefreshToken } = response.data.data

        const authData = JSON.parse(localStorage.getItem('auth') || '{}')
        authData.accessToken = accessToken
        localStorage.setItem('auth', JSON.stringify(authData))
        localStorage.setItem('refreshToken', newRefreshToken)

        processQueue(null, accessToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        localStorage.removeItem('auth')
        localStorage.removeItem('refreshToken')
        router.push('/login')
        ElMessage.error('Session expired, please login again')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    const message = error.response?.data?.message || error.message || 'Network error'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default api
