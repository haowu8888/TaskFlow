<template>
  <el-tag :type="tagType" size="small">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TaskStatus } from '@/types'

const { t } = useI18n()
const props = defineProps<{ status: TaskStatus }>()

const tagType = computed(() => {
  const map: Record<TaskStatus, string> = { TODO: 'info', IN_PROGRESS: '', IN_REVIEW: 'warning', DONE: 'success', CANCELLED: 'danger' }
  return map[props.status] || ''
})
const label = computed(() => {
  const map: Record<TaskStatus, string> = { TODO: t('status.todo'), IN_PROGRESS: t('status.inProgress'), IN_REVIEW: t('status.inReview'), DONE: t('status.done'), CANCELLED: t('status.cancelled') }
  return map[props.status] || props.status
})
</script>
