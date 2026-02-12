import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User, LoginRequest, RegisterRequest } from '@/types'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const accessToken = ref<string>('')
  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(data: LoginRequest) {
    const res = await authApi.login(data)
    accessToken.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('auth', JSON.stringify({ accessToken: res.data.accessToken }))
  }

  async function register(data: RegisterRequest) {
    const res = await authApi.register(data)
    accessToken.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('refreshToken', res.data.refreshToken)
    localStorage.setItem('auth', JSON.stringify({ accessToken: res.data.accessToken }))
  }

  async function fetchUser() {
    try {
      const res = await authApi.getCurrentUser()
      user.value = res.data
    } catch {
      logout()
    }
  }

  async function logout() {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        await authApi.logout(refreshToken)
      }
    } catch { /* ignore */ } finally {
      user.value = null
      accessToken.value = ''
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('auth')
      router.push('/login')
    }
  }

  function initAuth() {
    const authData = localStorage.getItem('auth')
    if (authData) {
      try {
        const parsed = JSON.parse(authData)
        accessToken.value = parsed.accessToken || ''
      } catch {
        accessToken.value = ''
      }
    }
  }

  return { user, accessToken, isAuthenticated, login, register, fetchUser, logout, initAuth }
})
