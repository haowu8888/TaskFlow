import { ref, watch } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { useAuthStore } from '@/stores/auth'
import { useWorkspaceStore } from '@/stores/workspace'
import { useNotificationStore } from '@/stores/notification'
import { useTaskStore } from '@/stores/task'
import type { Notification } from '@/types'

const connected = ref(false)
let stompClient: Client | null = null

export function useWebSocket() {
  const authStore = useAuthStore()
  const workspaceStore = useWorkspaceStore()
  const notificationStore = useNotificationStore()
  const taskStore = useTaskStore()

  function connect() {
    if (stompClient?.active) return

    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        connected.value = true
        subscribeToChannels()
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message'])
      }
    })

    stompClient.activate()
  }

  function subscribeToChannels() {
    if (!stompClient?.active) return

    // Subscribe to user notifications
    if (authStore.user?.id) {
      stompClient.subscribe(
        `/topic/user/${authStore.user.id}/notifications`,
        (message: IMessage) => {
          const notification: Notification = JSON.parse(message.body)
          notificationStore.notifications.unshift(notification)
          notificationStore.unreadCount++
        }
      )
    }

    // Subscribe to current workspace task updates
    if (workspaceStore.currentWorkspace?.id) {
      subscribeToWorkspace(workspaceStore.currentWorkspace.id)
    }
  }

  let workspaceSubscription: { unsubscribe: () => void } | null = null

  function subscribeToWorkspace(workspaceId: number) {
    if (!stompClient?.active) return

    // Unsubscribe from previous workspace
    if (workspaceSubscription) {
      workspaceSubscription.unsubscribe()
    }

    workspaceSubscription = stompClient.subscribe(
      `/topic/workspace/${workspaceId}/tasks`,
      (message: IMessage) => {
        try {
          const taskData = JSON.parse(message.body)
          if (taskData && taskData.id) {
            taskStore.updateTaskInList(taskData.id, taskData)
          }
        } catch {
          // Message body is not task JSON, ignore parse error
        }
        taskStore.notifyTaskChange()
        taskStore.refreshCurrentView()
      }
    )
  }

  function disconnect() {
    if (stompClient?.active) {
      stompClient.deactivate()
    }
    stompClient = null
    connected.value = false
  }

  // Watch for workspace changes to re-subscribe
  watch(
    () => workspaceStore.currentWorkspace?.id,
    (newId) => {
      if (newId && connected.value) {
        subscribeToWorkspace(newId)
      }
    }
  )

  // Watch for auth changes
  watch(
    () => authStore.isAuthenticated,
    (isAuth) => {
      if (isAuth) {
        connect()
      } else {
        disconnect()
      }
    }
  )

  return { connected, connect, disconnect }
}
