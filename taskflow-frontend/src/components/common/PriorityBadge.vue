<template>
  <el-tag :type="tagType" size="small" effect="dark">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TaskPriority } from '@/types'

const { t } = useI18n()
const props = defineProps<{ priority: TaskPriority }>()

const tagType = computed(() => {
  const map: Record<TaskPriority, string> = { URGENT: 'danger', HIGH: 'warning', MEDIUM: '', LOW: 'info' }
  return map[props.priority] || ''
})
const label = computed(() => {
  const map: Record<TaskPriority, string> = { URGENT: t('priority.urgent'), HIGH: t('priority.high'), MEDIUM: t('priority.medium'), LOW: t('priority.low') }
  return map[props.priority] || props.priority
})
</script>
