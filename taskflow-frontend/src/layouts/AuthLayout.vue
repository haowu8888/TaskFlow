<template>
  <div class="auth-layout">
    <div class="auth-bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
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
  position: relative;
  overflow: hidden;
}

.auth-bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.bg-circle-1 {
  width: 400px; height: 400px;
  top: -100px; right: -100px;
  animation: float 20s ease-in-out infinite;
}

.bg-circle-2 {
  width: 300px; height: 300px;
  bottom: -80px; left: -80px;
  animation: float 25s ease-in-out infinite reverse;
}

.bg-circle-3 {
  width: 200px; height: 200px;
  top: 50%; left: 60%;
  opacity: 0.05;
  animation: float 18s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -30px) scale(1.05); }
  50% { transform: translate(-10px, 20px) scale(0.95); }
  75% { transform: translate(15px, 10px) scale(1.02); }
}

.auth-container { width: 100%; max-width: 420px; padding: 20px; position: relative; z-index: 1; }
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { font-size: 36px; font-weight: 700; color: #fff; letter-spacing: 2px; text-shadow: 0 2px 10px rgba(0,0,0,0.15); }
.auth-subtitle { color: rgba(255, 255, 255, 0.8); margin-top: 8px; font-size: 14px; }
.auth-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--tf-radius-lg);
  padding: 36px 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
</style>
