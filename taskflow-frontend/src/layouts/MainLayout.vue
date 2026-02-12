<template>
  <div class="main-layout">
    <AppSidebar />
    <div class="main-content" :class="{ 'sidebar-collapsed': appStore.sidebarCollapsed }">
      <AppHeader />
      <div class="page-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/common/AppSidebar.vue'
import AppHeader from '@/components/common/AppHeader.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<style lang="scss" scoped>
.main-layout { display: flex; height: 100vh; overflow: hidden; }
.main-content {
  flex: 1; display: flex; flex-direction: column;
  margin-left: 240px; transition: margin-left 0.3s ease; overflow: hidden;
  &.sidebar-collapsed { margin-left: 64px; }
}
.page-content { flex: 1; padding: 20px; overflow-y: auto; background-color: #F5F7FA; }
</style>
