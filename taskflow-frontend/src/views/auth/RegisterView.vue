<template>
  <div class="register-view">
    <h2 class="form-title">{{ $t('auth.signUp') }}</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
      <el-form-item :label="$t('auth.username')" prop="username">
        <el-input v-model="form.username" :placeholder="$t('auth.chooseUsername')" size="large" />
      </el-form-item>
      <el-form-item :label="$t('auth.email')" prop="email">
        <el-input v-model="form.email" :placeholder="$t('auth.enterEmail')" size="large" />
      </el-form-item>
      <el-form-item :label="$t('auth.password')" prop="password">
        <el-input v-model="form.password" type="password" :placeholder="$t('auth.choosePassword')" size="large" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="handleRegister" :loading="loading">{{ $t('auth.signUp') }}</el-button>
      </el-form-item>
    </el-form>
    <div class="form-footer">{{ $t('auth.hasAccount') }} <router-link to="/login">{{ $t('auth.goSignIn') }}</router-link></div>
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
const form = reactive({ username: '', email: '', password: '' })
const rules = {
  username: [{ required: true, message: t('auth.usernameRequired'), trigger: 'blur' }, { min: 3, max: 50, message: t('auth.usernameLength'), trigger: 'blur' }],
  email: [{ required: true, message: t('auth.emailRequired'), trigger: 'blur' }, { type: 'email' as const, message: t('auth.emailInvalid'), trigger: 'blur' }],
  password: [{ required: true, message: t('auth.passwordRequired'), trigger: 'blur' }, { min: 6, message: t('auth.passwordLength'), trigger: 'blur' }]
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.register(form)
    ElMessage.success(t('auth.registerSuccess'))
    router.push('/')
  } catch {} finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.form-title { text-align: center; margin-bottom: 24px; font-size: 22px; color: #303133; }
.form-footer { text-align: center; color: #909399; font-size: 14px; margin-top: 16px; }
</style>
