<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <h1 class="auth-logo">{{ $t('app.title') }}</h1>
        <p class="auth-subtitle">{{ $t('app.subtitle') }}</p>
      </div>
      <div class="auth-card">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style lang="scss" scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-container { width: 100%; max-width: 420px; padding: 20px; }
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { font-size: 36px; font-weight: 700; color: #fff; letter-spacing: 2px; }
.auth-subtitle { color: rgba(255, 255, 255, 0.8); margin-top: 8px; font-size: 14px; }
.auth-card { background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15); }
</style>
