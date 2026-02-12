<template>
  <div class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
    <div class="sidebar-header">
      <h2 v-if="!appStore.sidebarCollapsed" class="logo">{{ $t('app.title') }}</h2>
      <h2 v-else class="logo-mini">TF</h2>
    </div>
    <div class="workspace-switcher" v-if="!appStore.sidebarCollapsed">
      <el-select v-model="currentWorkspaceId" :placeholder="$t('sidebar.selectWorkspace')" size="default" @change="handleWorkspaceChange" style="width: 100%">
        <el-option v-for="ws in workspaceStore.workspaces" :key="ws.id" :label="ws.name" :value="ws.id" />
      </el-select>
    </div>
    <el-menu :default-active="activeMenu" :collapse="appStore.sidebarCollapsed" :collapse-transition="false" router class="sidebar-menu">
      <el-menu-item index="/">
        <el-icon><Odometer /></el-icon>
        <template #title>{{ $t('sidebar.dashboard') }}</template>
      </el-menu-item>
      <el-menu-item index="/tasks">
        <el-icon><List /></el-icon>
        <template #title>{{ $t('sidebar.tasks') }}</template>
      </el-menu-item>
      <el-menu-item index="/boards">
        <el-icon><Grid /></el-icon>
        <template #title>{{ $t('sidebar.boards') }}</template>
      </el-menu-item>
      <el-menu-item index="/calendar">
        <el-icon><Calendar /></el-icon>
        <template #title>{{ $t('sidebar.calendar') }}</template>
      </el-menu-item>
      <el-menu-item index="/gantt">
        <el-icon><DataLine /></el-icon>
        <template #title>{{ $t('sidebar.ganttChart') }}</template>
      </el-menu-item>
      <el-menu-item index="/workspace/settings">
        <el-icon><Setting /></el-icon>
        <template #title>{{ $t('sidebar.workspace') }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Odometer, List, Grid, Calendar, DataLine, Setting } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useWorkspaceStore } from '@/stores/workspace'

const { t } = useI18n()

const route = useRoute()
const appStore = useAppStore()
const workspaceStore = useWorkspaceStore()
const currentWorkspaceId = ref<number | undefined>()
const activeMenu = computed(() => route.path)

onMounted(async () => {
  await workspaceStore.fetchWorkspaces()
  if (workspaceStore.currentWorkspace) {
    currentWorkspaceId.value = workspaceStore.currentWorkspace.id
  }
})

watch(() => workspaceStore.currentWorkspace, (ws) => {
  if (ws) currentWorkspaceId.value = ws.id
})

function handleWorkspaceChange(id: number) {
  const ws = workspaceStore.workspaces.find(w => w.id === id)
  if (ws) workspaceStore.setCurrentWorkspace(ws)
}
</script>

<style lang="scss" scoped>
.sidebar {
  position: fixed; left: 0; top: 0; bottom: 0; width: 240px;
  background: var(--tf-bg-sidebar); color: #fff; display: flex; flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1); z-index: 100; overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  &.collapsed { width: 64px; }
}
.sidebar-header {
  height: 56px; display: flex; align-items: center; justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.logo {
  font-size: 20px; font-weight: 700; letter-spacing: 1px;
  background: var(--tf-gradient-primary); -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.logo-mini {
  font-size: 18px; font-weight: 700;
  background: var(--tf-gradient-primary); -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.workspace-switcher {
  padding: 12px 16px; border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  :deep(.el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  }
  :deep(.el-input__inner) {
    color: rgba(255, 255, 255, 0.85);
  }
}
.sidebar-menu {
  flex: 1; border-right: none; background: transparent; padding: 8px;
  :deep(.el-menu-item) {
    color: rgba(255, 255, 255, 0.6); height: 44px; margin-bottom: 2px;
    border-radius: var(--tf-radius-sm); transition: all 0.2s ease;
    &:hover { background-color: var(--tf-bg-sidebar-hover); color: rgba(255, 255, 255, 0.9); }
    &.is-active {
      background-color: var(--tf-bg-sidebar-active); color: #fff;
      position: relative;
      &::before {
        content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
        width: 3px; height: 20px; border-radius: 0 3px 3px 0; background: var(--tf-color-primary);
      }
    }
    .el-icon { font-size: 18px; }
  }
}
</style>
