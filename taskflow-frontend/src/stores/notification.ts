import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'
import type { Notification } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)

  async function fetchNotifications() {
    const res = await notificationApi.list()
    notifications.value = res.data
  }

  async function fetchUnreadCount() {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data
  }

  async function markAsRead(id: number) {
    await notificationApi.markAsRead(id)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  async function markAllAsRead() {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  }

  return { notifications, unreadCount, fetchNotifications, fetchUnreadCount, markAsRead, markAllAsRead }
})
