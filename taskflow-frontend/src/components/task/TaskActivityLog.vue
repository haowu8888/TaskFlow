<template>
  <div class="task-activity-log">
    <div v-if="activities.length > 0" class="activity-timeline">
      <div v-for="activity in activities" :key="activity.id" class="activity-item">
        <el-avatar :size="24" class="activity-avatar">{{ activity.user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
        <div class="activity-content">
          <div class="activity-text">
            <span class="activity-user">{{ activity.user?.username }}</span>
            <span class="activity-action">{{ formatAction(activity) }}</span>
          </div>
          <div class="activity-time">{{ formatTime(activity.createdAt) }}</div>
        </div>
      </div>
    </div>
    <div v-else class="activity-empty">{{ $t('activity.noActivities') }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { activityApi } from '@/api/activity'
import type { TaskActivity } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const { t } = useI18n()
const props = defineProps<{ taskId: number }>()
const activities = ref<TaskActivity[]>([])

function formatAction(activity: TaskActivity): string {
  if (activity.action === 'CREATED') return t('activity.created')
  if (activity.action === 'DELETED') return t('activity.deleted')
  if (activity.action === 'UPDATED' && activity.fieldName) {
    const field = activity.fieldName
    const newVal = activity.newValue || ''
    return `${t('activity.updated')} ${field}: ${newVal}`
  }
  return activity.action
}

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

onMounted(async () => {
  try {
    const res = await activityApi.getTaskActivities(props.taskId)
    activities.value = res.data
  } catch {}
})
</script>

<style lang="scss" scoped>
.activity-timeline {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--tf-border-color-lighter);

  &:last-child { border-bottom: none; }
}

.activity-avatar {
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 13px;
  color: var(--tf-text-regular);
  line-height: 1.5;
}

.activity-user {
  font-weight: 600;
  color: var(--tf-text-primary);
  margin-right: 4px;
}

.activity-action {
  color: var(--tf-text-secondary);
}

.activity-time {
  font-size: 12px;
  color: var(--tf-text-placeholder);
  margin-top: 2px;
}

.activity-empty {
  color: var(--tf-text-placeholder);
  font-size: 13px;
}
</style>
