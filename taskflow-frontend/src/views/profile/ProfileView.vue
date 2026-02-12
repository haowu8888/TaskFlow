<template>
  <div class="profile-view">
    <h2 class="page-title">{{ $t('profile.title') }}</h2>
    <el-card shadow="hover" style="max-width: 600px;">
      <el-form :model="form" label-position="top">
        <el-form-item :label="$t('profile.username')"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item :label="$t('profile.email')"><el-input v-model="form.email" disabled /></el-form-item>
        <el-form-item :label="$t('profile.avatarUrl')"><el-input v-model="form.avatarUrl" :placeholder="$t('profile.enterAvatarUrl')" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave" :loading="loading">{{ $t('common.save') }}</el-button></el-form-item>
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
const form = reactive({ username: '', email: '', avatarUrl: '' })

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
</script>

<style lang="scss" scoped>.page-title { font-size: 24px; margin-bottom: 24px; }</style>
