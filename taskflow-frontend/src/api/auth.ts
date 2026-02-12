import api from './index'
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, User } from '@/types'

export const authApi = {
  login(data: LoginRequest) {
    return api.post<any, ApiResponse<AuthResponse>>('/auth/login', data)
  },
  register(data: RegisterRequest) {
    return api.post<any, ApiResponse<AuthResponse>>('/auth/register', data)
  },
  refresh(refreshToken: string) {
    return api.post<any, ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken })
  },
  logout(refreshToken: string) {
    return api.post<any, ApiResponse<void>>('/auth/logout', { refreshToken })
  },
  getCurrentUser() {
    return api.get<any, ApiResponse<User>>('/users/me')
  },
  updateProfile(data: Partial<User>) {
    return api.put<any, ApiResponse<User>>('/users/me', data)
  },
  searchUsers(keyword: string) {
    return api.get<any, ApiResponse<User[]>>('/users/search', { params: { keyword } })
  },
  changePassword(data: { currentPassword: string; newPassword: string }) {
    return api.put<any, ApiResponse<void>>('/users/me/password', data)
  }
}
