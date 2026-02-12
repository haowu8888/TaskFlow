<template>
  <div class="kanban-card" @click="$emit('click', task)">
    <div class="card-header">
      <PriorityBadge :priority="task.priority" />
      <span v-if="task.dueDate" class="card-due" :class="{ overdue: isOverdue }">
        {{ formatDate(task.dueDate) }}
      </span>
    </div>
    <div class="card-title">{{ task.title }}</div>
    <div class="card-footer" v-if="task.assignee || task.progress > 0">
      <div class="card-assignee" v-if="task.assignee">
        <el-avatar :size="22">{{ task.assignee.username?.charAt(0)?.toUpperCase() }}</el-avatar>
        <span class="assignee-name">{{ task.assignee.username }}</span>
      </div>
      <div v-else class="card-assignee-placeholder"></div>
      <div v-if="task.progress > 0" class="card-progress">
        <el-progress :percentage="task.progress" :stroke-width="4" :show-text="false" style="width: 60px" />
        <span class="progress-text">{{ task.progress }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import type { Task } from '@/types'

const props = defineProps<{ task: Task }>()
defineEmits<{ click: [task: Task] }>()

const isOverdue = computed(() => {
  return props.task.dueDate &&
    props.task.status !== 'DONE' &&
    dayjs(props.task.dueDate).isBefore(dayjs(), 'day')
})

function formatDate(date: string) {
  return dayjs(date).format('MMM DD')
}
</script>

<style lang="scss" scoped>
.kanban-card {
  background: var(--tf-bg-card);
  border: 1px solid var(--tf-border-color-lighter);
  border-radius: var(--tf-radius-md);
  padding: 12px 14px;
  cursor: pointer;
  transition: all var(--tf-transition-fast);

  &:hover {
    box-shadow: var(--tf-shadow-base);
    border-color: var(--tf-border-color);
    transform: translateY(-1px);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-due {
  font-size: 12px;
  color: var(--tf-text-secondary);
  background: var(--tf-bg-card-hover);
  padding: 2px 6px;
  border-radius: 4px;

  &.overdue {
    color: var(--tf-color-danger);
    background: rgba(245, 63, 63, 0.08);
    font-weight: 600;
  }
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--tf-text-primary);
  line-height: 1.5;
  margin-bottom: 8px;
  word-break: break-word;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-assignee {
  display: flex;
  align-items: center;
  gap: 6px;
}

.assignee-name {
  font-size: 12px;
  color: var(--tf-text-secondary);
}

.card-assignee-placeholder {
  flex: 1;
}

.card-progress {
  display: flex;
  align-items: center;
  gap: 4px;
}

.progress-text {
  font-size: 11px;
  color: var(--tf-text-secondary);
  font-weight: 500;
}
</style>
