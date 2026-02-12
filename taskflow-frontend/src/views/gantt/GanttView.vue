<template>
  <div class="gantt-view">
    <div class="page-header flex-between">
      <h2 class="page-title">{{ $t('gantt.title') }}</h2>
      <div class="gantt-toolbar">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="to" :start-placeholder="$t('gantt.start')" :end-placeholder="$t('gantt.end')" value-format="YYYY-MM-DD" size="default" @change="loadTasks" />
      </div>
    </div>

    <el-card shadow="never" class="gantt-card" v-loading="loading">
      <div class="gantt-container" v-if="tasks.length > 0">
        <div class="gantt-table">
          <div class="gantt-header-row">
            <div class="gantt-task-info-header">{{ $t('gantt.taskColumn') }}</div>
            <div class="gantt-timeline-header">
              <div v-for="day in timelineDays" :key="day" class="gantt-day-header" :class="{ weekend: isWeekend(day) }">
                {{ formatDayHeader(day) }}
              </div>
            </div>
          </div>
          <div v-for="task in tasks" :key="task.id" class="gantt-row" @click="openTask(task)">
            <div class="gantt-task-info">
              <PriorityBadge :priority="task.priority" />
              <span class="gantt-task-name text-ellipsis">{{ task.title }}</span>
            </div>
            <div class="gantt-timeline">
              <div v-for="day in timelineDays" :key="day" class="gantt-cell" :class="{ weekend: isWeekend(day) }"></div>
              <div
                class="gantt-bar"
                :style="getBarStyle(task)"
                :class="'priority-' + task.priority.toLowerCase()"
              >
                <div class="gantt-bar-progress" :style="{ width: task.progress + '%' }"></div>
                <span class="gantt-bar-label">{{ task.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('gantt.noTasks')" />
    </el-card>

    <TaskDetailDrawer v-model="showDetail" :task-id="selectedTaskId" @updated="loadTasks" @deleted="loadTasks" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import TaskDetailDrawer from '@/components/task/TaskDetailDrawer.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { useTaskStore } from '@/stores/task'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const workspaceStore = useWorkspaceStore()
const taskStore = useTaskStore()
const { t } = useI18n()
const loading = ref(false)
const tasks = ref<Task[]>([])
const showDetail = ref(false)
const selectedTaskId = ref(0)

const dateRange = ref<string[]>([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
])

const timelineDays = computed(() => {
  if (!dateRange.value || dateRange.value.length < 2) return []
  const days: string[] = []
  let current = dayjs(dateRange.value[0])
  const end = dayjs(dateRange.value[1])
  while (current.isBefore(end) || current.isSame(end, 'day')) {
    days.push(current.format('YYYY-MM-DD'))
    current = current.add(1, 'day')
  }
  return days
})

const totalDays = computed(() => timelineDays.value.length)

async function loadTasks() {
  if (!workspaceStore.currentWorkspace) return
  loading.value = true
  try {
    const res = await taskApi.getGanttTasks(workspaceStore.currentWorkspace.id)
    tasks.value = res.data.filter((t: Task) => t.startDate || t.dueDate)
  } catch {} finally { loading.value = false }
}

function getBarStyle(task: Task) {
  if (!dateRange.value || dateRange.value.length < 2) return {}
  const rangeStart = dayjs(dateRange.value[0])
  const taskStart = dayjs(task.startDate || task.dueDate)
  const taskEnd = dayjs(task.dueDate || task.startDate)

  const startOffset = Math.max(0, taskStart.diff(rangeStart, 'day'))
  const duration = Math.max(1, taskEnd.diff(taskStart, 'day') + 1)
  const cellWidth = 40

  return {
    left: startOffset * cellWidth + 'px',
    width: duration * cellWidth + 'px'
  }
}

function isWeekend(day: string) {
  const d = dayjs(day).day()
  return d === 0 || d === 6
}

function formatDayHeader(day: string) {
  return dayjs(day).format('DD')
}

function openTask(task: Task) {
  selectedTaskId.value = task.id
  showDetail.value = true
}

watch(() => workspaceStore.currentWorkspace, loadTasks)
watch(() => taskStore.taskEventVersion, loadTasks)
onMounted(loadTasks)
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--tf-text-primary); }
.flex-between { display: flex; justify-content: space-between; align-items: center; }

.gantt-card { overflow: hidden; }
.gantt-container { overflow-x: auto; }

.gantt-table { min-width: fit-content; }

.gantt-header-row {
  display: flex;
  border-bottom: 2px solid var(--tf-border-color);
  position: sticky;
  top: 0;
  background: var(--tf-bg-card);
  z-index: 1;
}

.gantt-task-info-header {
  min-width: 220px;
  max-width: 220px;
  padding: 10px 12px;
  font-weight: 600;
  font-size: 13px;
  color: var(--tf-text-regular);
  border-right: 1px solid var(--tf-border-color-lighter);
}

.gantt-timeline-header {
  display: flex;
  position: relative;
}

.gantt-day-header {
  min-width: 40px;
  max-width: 40px;
  text-align: center;
  padding: 10px 0;
  font-size: 11px;
  font-weight: 500;
  color: var(--tf-text-secondary);
  border-right: 1px solid var(--tf-border-color-lighter);
  &.weekend { background: var(--tf-bg-card-hover); color: var(--tf-text-placeholder); }
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid var(--tf-border-color-lighter);
  cursor: pointer;
  transition: background var(--tf-transition-fast);
  &:hover { background: var(--tf-bg-card-hover); }
}

.gantt-task-info {
  min-width: 220px;
  max-width: 220px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-right: 1px solid var(--tf-border-color-lighter);
}

.gantt-task-name { font-size: 13px; color: var(--tf-text-primary); font-weight: 500; }

.gantt-timeline {
  display: flex;
  position: relative;
  flex: 1;
}

.gantt-cell {
  min-width: 40px;
  max-width: 40px;
  border-right: 1px solid var(--tf-border-color-lighter);
  &.weekend { background: var(--tf-bg-card-hover); }
}

.gantt-bar {
  position: absolute;
  top: 6px;
  height: 24px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  z-index: 1;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
  transition: box-shadow var(--tf-transition-fast);

  &:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.25); }

  &.priority-urgent { background: var(--tf-color-danger); }
  &.priority-high { background: var(--tf-color-warning); }
  &.priority-medium { background: var(--tf-color-primary); }
  &.priority-low { background: var(--tf-text-secondary); }
}

.gantt-bar-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 6px 0 0 6px;
}

.gantt-bar-label {
  padding: 0 8px;
  font-size: 11px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  z-index: 1;
  font-weight: 500;
}
</style>
