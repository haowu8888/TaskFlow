<template>
  <div class="app-header">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar" :size="20">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <el-tooltip :content="$t('header.switchLang')" placement="bottom">
        <el-icon class="header-icon" :size="20" @click="toggleLocale">
          <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" width="1em" height="1em" fill="currentColor">
            <path d="M12.87 15.07l-2.54-2.51.03-.03A17.52 17.52 0 0014.07 6H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/>
          </svg>
        </el-icon>
      </el-tooltip>
      <el-tooltip :content="appStore.darkMode ? $t('header.lightMode') : $t('header.darkMode')" placement="bottom">
        <el-icon class="header-icon" :size="20" @click="appStore.toggleDarkMode">
          <Sunny v-if="appStore.darkMode" />
          <Moon v-else />
        </el-icon>
      </el-tooltip>
      <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" :max="99">
        <el-icon class="header-icon" :size="20" @click="showNotifications = true"><Bell /></el-icon>
      </el-badge>
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="authStore.user?.avatarUrl">
            {{ authStore.user?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <span class="username">{{ authStore.user?.username }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">{{ $t('auth.profile') }}</el-dropdown-item>
            <el-dropdown-item command="logout" divided>{{ $t('auth.logout') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <el-drawer v-model="showNotifications" :title="$t('header.notifications')" size="380px">
      <div class="notification-header">
        <el-button type="primary" link @click="notificationStore.markAllAsRead" :disabled="notificationStore.unreadCount === 0">{{ $t('header.markAllRead') }}</el-button>
      </div>
      <div class="notification-list">
        <div v-for="n in notificationStore.notifications" :key="n.id" class="notification-item" :class="{ unread: !n.isRead }" @click="notificationStore.markAsRead(n.id)">
          <div class="notification-title">{{ n.title }}</div>
          <div class="notification-content" v-if="n.content">{{ n.content }}</div>
          <div class="notification-time">{{ formatTime(n.createdAt) }}</div>
        </div>
        <el-empty v-if="notificationStore.notifications.length === 0" :description="$t('header.noNotifications')" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Fold, Expand, Bell, Sunny, Moon } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const showNotifications = ref(false)
const routeNameMap: Record<string, string> = {
  Dashboard: 'sidebar.dashboard',
  Tasks: 'sidebar.tasks',
  Boards: 'sidebar.boards',
  BoardDetail: 'board.title',
  Calendar: 'sidebar.calendar',
  Gantt: 'sidebar.ganttChart',
  WorkspaceSettings: 'sidebar.workspace',
  Profile: 'auth.profile'
}
const currentRouteName = computed(() => {
  const name = route.name as string
  const key = routeNameMap[name]
  return key ? t(key) : name || ''
})

onMounted(() => {
  notificationStore.fetchNotifications()
  notificationStore.fetchUnreadCount()
})

function formatTime(time: string) { return dayjs(time).fromNow() }

function handleCommand(command: string) {
  if (command === 'profile') router.push('/profile')
  else if (command === 'logout') authStore.logout()
}

function toggleLocale() {
  const newLocale = locale.value === 'zh' ? 'en' : 'zh'
  locale.value = newLocale
  localStorage.setItem('locale', newLocale)
}
</script>

<style lang="scss" scoped>
.app-header { height: 56px; background: #fff; border-bottom: 1px solid #EBEEF5; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; flex-shrink: 0; }
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { cursor: pointer; color: #606266; &:hover { color: #409EFF; } }
.header-right { display: flex; align-items: center; gap: 20px; }
.header-icon { cursor: pointer; color: #606266; &:hover { color: #409EFF; } }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; color: #303133; }
.notification-header { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.notification-item {
  padding: 12px; border-bottom: 1px solid #EBEEF5; cursor: pointer; transition: background 0.2s;
  &:hover { background: #F5F7FA; }
  &.unread { background: #ECF5FF; }
}
.notification-title { font-weight: 500; margin-bottom: 4px; }
.notification-content { color: #909399; font-size: 13px; margin-bottom: 4px; }
.notification-time { color: #C0C4CC; font-size: 12px; }
</style>
