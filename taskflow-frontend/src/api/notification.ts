import api from './index'
import type { ApiResponse, Notification } from '@/types'

export const notificationApi = {
  list() {
    return api.get<any, ApiResponse<Notification[]>>('/notifications')
  },
  getUnreadCount() {
    return api.get<any, ApiResponse<number>>('/notifications/unread-count')
  },
  markAsRead(id: number) {
    return api.put<any, ApiResponse<void>>(`/notifications/${id}/read`)
  },
  markAllAsRead() {
    return api.put<any, ApiResponse<void>>('/notifications/read-all')
  }
}
