<template>
  <div class="profile-view">
    <h2 class="page-title">{{ $t('profile.title') }}</h2>

    <!-- Profile Header -->
    <el-card shadow="hover" class="profile-header-card mb-16" style="max-width: 600px;">
      <div class="profile-header">
        <el-avatar :size="80" :src="authStore.user?.avatarUrl" class="profile-avatar">
          {{ authStore.user?.username?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <div class="profile-info">
          <h3 class="profile-name">{{ authStore.user?.username }}</h3>
          <p class="profile-email">{{ authStore.user?.email }}</p>
        </div>
      </div>
    </el-card>

    <!-- Basic Info -->
    <el-card shadow="hover" class="mb-16" style="max-width: 600px;">
      <template #header>
        <span class="section-header">{{ $t('profile.basicInfo') || 'Basic Information' }}</span>
      </template>
      <el-form :model="form" label-position="top">
        <el-form-item :label="$t('profile.username')"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item :label="$t('profile.email')"><el-input v-model="form.email" disabled /></el-form-item>
        <el-form-item :label="$t('profile.avatarUrl')"><el-input v-model="form.avatarUrl" :placeholder="$t('profile.enterAvatarUrl')" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave" :loading="loading">{{ $t('common.save') }}</el-button></el-form-item>
      </el-form>
    </el-card>

    <!-- Security Settings -->
    <el-card shadow="hover" style="max-width: 600px;">
      <template #header>
        <span class="section-header">{{ $t('profile.security') || 'Security Settings' }}</span>
      </template>
      <el-form :model="passwordForm" label-position="top">
        <el-form-item :label="$t('profile.currentPassword') || 'Current Password'">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password :placeholder="$t('profile.enterCurrentPassword') || 'Enter current password'" />
        </el-form-item>
        <el-form-item :label="$t('profile.newPassword') || 'New Password'">
          <el-input v-model="passwordForm.newPassword" type="password" show-password :placeholder="$t('profile.enterNewPassword') || 'Enter new password'" />
        </el-form-item>
        <el-form-item :label="$t('profile.confirmPassword') || 'Confirm Password'">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password :placeholder="$t('profile.confirmNewPassword') || 'Confirm new password'" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword" :loading="changingPassword" :disabled="!passwordForm.currentPassword || !passwordForm.newPassword">
            {{ $t('profile.changePassword') || 'Change Password' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

const authStore = useAuthStore()
const { t } = useI18n()
const loading = ref(false)
const changingPassword = ref(false)
const form = reactive({ username: '', email: '', avatarUrl: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

onMounted(() => {
  if (authStore.user) {
    form.username = authStore.user.username
    form.email = authStore.user.email
    form.avatarUrl = authStore.user.avatarUrl || ''
  }
})

async function handleSave() {
  loading.value = true
  try {
    await authApi.updateProfile({ avatarUrl: form.avatarUrl })
    await authStore.fetchUser()
    ElMessage.success(t('profile.profileUpdated'))
  } catch {} finally { loading.value = false }
}

async function handleChangePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error(t('profile.passwordMismatch') || 'Passwords do not match')
    return
  }
  changingPassword.value = true
  try {
    await authApi.changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success(t('profile.passwordChanged') || 'Password changed successfully')
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {} finally { changingPassword.value = false }
}
</script>

<style lang="scss" scoped>
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; color: var(--tf-text-primary); }
.mb-16 { margin-bottom: 16px; }

.profile-header-card {
  :deep(.el-card__body) { padding: 28px; }
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  flex-shrink: 0;
  font-size: 28px;
  background: var(--tf-gradient-primary);
  color: #fff;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--tf-text-primary);
  margin-bottom: 4px;
}

.profile-email {
  font-size: 14px;
  color: var(--tf-text-secondary);
}

.section-header {
  font-weight: 600;
  color: var(--tf-text-primary);
  font-size: 15px;
}
</style>
