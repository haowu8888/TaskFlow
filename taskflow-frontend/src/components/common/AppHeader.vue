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
      <div class="search-trigger" @click="showSearch = true">
        <el-icon :size="14"><Search /></el-icon>
        <span class="search-trigger-text">{{ $t('search.placeholder') }}</span>
        <kbd class="search-trigger-kbd">Ctrl+K</kbd>
      </div>
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
        <div v-for="n in notificationStore.notifications" :key="n.id" class="notification-item" :class="{ unread: !n.isRead }" @click="handleNotificationClick(n)">
          <div class="notification-title">{{ n.title }}</div>
          <div class="notification-content" v-if="n.content">{{ n.content }}</div>
          <div class="notification-time">{{ formatTime(n.createdAt) }}</div>
        </div>
        <el-empty v-if="notificationStore.notifications.length === 0" :description="$t('header.noNotifications')" />
      </div>
    </el-drawer>
    <GlobalSearch v-model="showSearch" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Fold, Expand, Bell, Sunny, Moon, Search } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import GlobalSearch from '@/components/common/GlobalSearch.vue'
import type { Notification } from '@/types'
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
const showSearch = ref(false)

function handleKeyDown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    showSearch.value = true
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
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

function handleNotificationClick(n: Notification) {
  if (!n.isRead) {
    notificationStore.markAsRead(n.id)
  }
  showNotifications.value = false
  if (n.type === 'MEMBER_INVITED') {
    router.push('/workspace/settings')
  } else if (n.referenceId) {
    router.push(`/tasks?taskId=${n.referenceId}`)
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  height: 56px; background: var(--tf-bg-card); border-bottom: 1px solid var(--tf-border-color-lighter);
  display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0;
  backdrop-filter: blur(8px); background: rgba(var(--tf-bg-card), 0.8);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  cursor: pointer; color: var(--tf-text-secondary); transition: all var(--tf-transition-fast);
  padding: 6px; border-radius: var(--tf-radius-sm);
  &:hover { color: var(--tf-color-primary); background: var(--tf-color-primary-lighter); }
}
.header-right { display: flex; align-items: center; gap: 8px; }
.search-trigger {
  display: flex; align-items: center; gap: 8px;
  padding: 6px 12px; border-radius: var(--tf-radius-sm);
  border: 1px solid var(--tf-border-color-lighter);
  cursor: pointer; transition: all var(--tf-transition-fast);
  margin-right: 4px;
  &:hover { border-color: var(--tf-color-primary); background: var(--tf-color-primary-lighter); }
}
.search-trigger-text {
  font-size: 13px; color: var(--tf-text-placeholder); white-space: nowrap;
}
.search-trigger-kbd {
  padding: 1px 5px; font-size: 11px;
  background: var(--tf-bg-card-hover); border: 1px solid var(--tf-border-color-light);
  border-radius: 4px; color: var(--tf-text-secondary); font-family: inherit;
}
.header-icon {
  cursor: pointer; color: var(--tf-text-secondary); transition: all var(--tf-transition-fast);
  padding: 8px; border-radius: var(--tf-radius-sm);
  &:hover { color: var(--tf-color-primary); background: var(--tf-color-primary-lighter); }
}
.user-info {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px 4px 4px; border-radius: 20px; transition: background var(--tf-transition-fast);
  &:hover { background: var(--tf-bg-card-hover); }
}
.username { font-size: 14px; color: var(--tf-text-primary); font-weight: 500; }
.notification-header { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.notification-item {
  padding: 14px 16px; border-radius: var(--tf-radius-md); cursor: pointer;
  transition: all var(--tf-transition-fast); margin-bottom: 8px;
  border: 1px solid var(--tf-border-color-lighter);
  &:hover { background: var(--tf-bg-card-hover); border-color: var(--tf-border-color-light); }
  &.unread {
    background: var(--tf-color-primary-lighter);
    border-color: var(--tf-color-primary-light);
  }
}
.notification-title { font-weight: 500; margin-bottom: 4px; color: var(--tf-text-primary); font-size: 14px; }
.notification-content { color: var(--tf-text-secondary); font-size: 13px; margin-bottom: 6px; line-height: 1.5; }
.notification-time { color: var(--tf-text-placeholder); font-size: 12px; }
</style>
