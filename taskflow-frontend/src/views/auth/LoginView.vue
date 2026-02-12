<template>
  <div class="login-view">
    <h2 class="form-title">{{ $t('auth.signIn') }}</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
      <el-form-item :label="$t('auth.username')" prop="username">
        <el-input v-model="form.username" :placeholder="$t('auth.enterUsername')" size="large" />
      </el-form-item>
      <el-form-item :label="$t('auth.password')" prop="password">
        <el-input v-model="form.password" type="password" :placeholder="$t('auth.enterPassword')" size="large" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="handleLogin" :loading="loading">{{ $t('auth.signIn') }}</el-button>
      </el-form-item>
    </el-form>
    <div class="form-footer">{{ $t('auth.noAccount') }} <router-link to="/register">{{ $t('auth.goSignUp') }}</router-link></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: t('auth.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordRequired'), trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success(t('auth.loginSuccess'))
    router.push('/')
  } catch {} finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.form-title {
  text-align: center; margin-bottom: 28px; font-size: 24px; font-weight: 600;
  color: var(--tf-text-primary);
}
.form-footer {
  text-align: center; color: var(--tf-text-secondary); font-size: 14px; margin-top: 20px;
  a { font-weight: 500; }
}
</style>
