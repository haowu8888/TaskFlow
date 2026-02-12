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
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const workspaceStore = useWorkspaceStore()
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
onMounted(loadTasks)
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 16px; }
.page-title { font-size: 24px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }

.gantt-card { overflow: hidden; }
.gantt-container { overflow-x: auto; }

.gantt-table { min-width: fit-content; }

.gantt-header-row {
  display: flex;
  border-bottom: 2px solid #DCDFE6;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
}

.gantt-task-info-header {
  min-width: 220px;
  max-width: 220px;
  padding: 8px 12px;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  border-right: 1px solid #EBEEF5;
}

.gantt-timeline-header {
  display: flex;
  position: relative;
}

.gantt-day-header {
  min-width: 40px;
  max-width: 40px;
  text-align: center;
  padding: 8px 0;
  font-size: 11px;
  color: #909399;
  border-right: 1px solid #F0F2F5;
  &.weekend { background: #FAFAFA; color: #C0C4CC; }
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  &:hover { background: #F5F7FA; }
}

.gantt-task-info {
  min-width: 220px;
  max-width: 220px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-right: 1px solid #EBEEF5;
}

.gantt-task-name { font-size: 13px; }

.gantt-timeline {
  display: flex;
  position: relative;
  flex: 1;
}

.gantt-cell {
  min-width: 40px;
  max-width: 40px;
  border-right: 1px solid #F0F2F5;
  &.weekend { background: #FAFAFA; }
}

.gantt-bar {
  position: absolute;
  top: 6px;
  height: 24px;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  z-index: 1;

  &.priority-urgent { background: #F56C6C; }
  &.priority-high { background: #E6A23C; }
  &.priority-medium { background: #409EFF; }
  &.priority-low { background: #909399; }
}

.gantt-bar-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
}

.gantt-bar-label {
  padding: 0 6px;
  font-size: 11px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  position: relative;
  z-index: 1;
}
</style>
