<template>
  <div class="dashboard-view">
    <h2 class="page-title">{{ $t('dashboard.title') }}</h2>

    <!-- Stats Cards -->
    <el-row :gutter="16" class="mb-16">
      <el-col :span="6" v-for="(_, i) in 4" :key="i" v-if="dashLoading">
        <el-skeleton animated>
          <template #template>
            <div class="stat-card">
              <el-skeleton-item variant="rect" style="width: 42px; height: 42px; border-radius: 10px; margin-bottom: 14px;" />
              <el-skeleton-item variant="h1" style="width: 60px; height: 32px; margin-bottom: 4px;" />
              <el-skeleton-item variant="text" style="width: 80px;" />
            </div>
          </template>
        </el-skeleton>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="mb-16" v-if="!dashLoading">
      <el-col :span="6">
        <div class="stat-card" style="--stat-gradient: var(--tf-gradient-primary);">
          <div class="stat-icon-wrap">
            <el-icon :size="22"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ stats.totalTasks }}</div>
          <div class="stat-label">{{ $t('dashboard.totalTasks') }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="--stat-gradient: var(--tf-gradient-blue);">
          <div class="stat-icon-wrap">
            <el-icon :size="22"><Loading /></el-icon>
          </div>
          <div class="stat-value">{{ stats.inProgress }}</div>
          <div class="stat-label">{{ $t('dashboard.inProgress') }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="--stat-gradient: var(--tf-gradient-green);">
          <div class="stat-icon-wrap">
            <el-icon :size="22"><CircleCheck /></el-icon>
          </div>
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">{{ $t('dashboard.completed') }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="--stat-gradient: var(--tf-gradient-red);">
          <div class="stat-icon-wrap">
            <el-icon :size="22"><WarningFilled /></el-icon>
          </div>
          <div class="stat-value">{{ stats.overdue }}</div>
          <div class="stat-label">{{ $t('dashboard.overdue') }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- Board Overview -->
    <div v-if="boards.length > 0" class="mb-16">
      <h3 class="section-title">{{ $t('board.title') }}</h3>
      <el-row :gutter="16">
        <el-col :span="6" v-for="board in boards.slice(0, 4)" :key="board.id">
          <el-card shadow="hover" class="board-summary-card cursor-pointer" @click="router.push(`/boards/${board.id}`)">
            <div class="board-color-bar">
              <span
                v-for="col in (board.columns || []).slice(0, 6)"
                :key="col.id"
                class="color-segment"
                :style="{ background: col.color || '#165DFF' }"
              ></span>
            </div>
            <div class="board-summary-name">{{ board.name }}</div>
            <div class="board-summary-meta">
              <el-tag size="small" type="info">{{ board.columns?.length || 0 }} {{ $t('board.columns') }}</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Charts -->
    <el-row :gutter="20" class="mb-16" v-if="stats.totalTasks > 0">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="section-title" style="margin-bottom:0">{{ $t('dashboard.statusDistribution') }}</span></template>
          <v-chart class="chart" :option="statusChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="section-title" style="margin-bottom:0">{{ $t('dashboard.priorityDistribution') }}</span></template>
          <v-chart class="chart" :option="priorityChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- Task Lists -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>{{ $t('dashboard.myTasks') }}</span></template>
          <el-empty v-if="myTasks.length === 0" :description="$t('dashboard.noTasks')" />
          <div v-else class="task-list">
            <div v-for="task in myTasks" :key="task.id" class="task-item" @click="router.push(`/tasks?taskId=${task.id}`)">
              <div class="task-info"><PriorityBadge :priority="task.priority" /><span class="task-title">{{ task.title }}</span></div>
              <StatusTag :status="task.status" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>{{ $t('dashboard.upcomingDeadlines') }}</span></template>
          <el-empty v-if="upcomingTasks.length === 0" :description="$t('dashboard.noDeadlines')" />
          <div v-else class="task-list">
            <div v-for="task in upcomingTasks" :key="task.id" class="task-item" @click="router.push(`/tasks?taskId=${task.id}`)">
              <span class="task-title">{{ task.title }}</span>
              <span class="due-date" :class="{ overdue: isOverdue(task.dueDate) }">{{ formatDate(task.dueDate) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import { Document, Loading, CircleCheck, WarningFilled } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { useTaskStore } from '@/stores/task'
import { useBoardStore } from '@/stores/board'
import { taskApi } from '@/api/task'
import type { Task, Board } from '@/types'

use([CanvasRenderer, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()
const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const taskStore = useTaskStore()
const boardStore = useBoardStore()
const stats = ref({ totalTasks: 0, inProgress: 0, completed: 0, overdue: 0 })
const myTasks = ref<Task[]>([])
const upcomingTasks = ref<Task[]>([])
const dashLoading = ref(true)
const boards = ref<Board[]>([])
const allTasks = ref<Task[]>([])

const statusChartOption = computed(() => {
  const statusMap: Record<string, number> = {}
  allTasks.value.forEach(task => {
    statusMap[task.status] = (statusMap[task.status] || 0) + 1
  })
  const statusColors: Record<string, string> = {
    TODO: '#86909c',
    IN_PROGRESS: '#165DFF',
    IN_REVIEW: '#ff7d00',
    DONE: '#00b42a',
    CANCELLED: '#f53f3f'
  }
  const statusLabels: Record<string, string> = {
    TODO: t('status.todo'),
    IN_PROGRESS: t('status.inProgress'),
    IN_REVIEW: t('status.inReview'),
    DONE: t('status.done'),
    CANCELLED: t('status.cancelled')
  }
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: 'var(--tf-text-secondary)' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: Object.entries(statusMap).map(([status, count]) => ({
        name: statusLabels[status] || status,
        value: count,
        itemStyle: { color: statusColors[status] || '#165DFF' }
      }))
    }]
  }
})

const priorityChartOption = computed(() => {
  const priorityMap: Record<string, number> = { URGENT: 0, HIGH: 0, MEDIUM: 0, LOW: 0 }
  allTasks.value.forEach(task => {
    if (task.priority in priorityMap) {
      priorityMap[task.priority]++
    }
  })
  const priorityLabels: Record<string, string> = {
    URGENT: t('priority.urgent'),
    HIGH: t('priority.high'),
    MEDIUM: t('priority.medium'),
    LOW: t('priority.low')
  }
  const priorityColors: Record<string, string> = {
    URGENT: '#f53f3f',
    HIGH: '#ff7d00',
    MEDIUM: '#165DFF',
    LOW: '#86909c'
  }
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['URGENT', 'HIGH', 'MEDIUM', 'LOW'].map(p => priorityLabels[p]),
      axisLabel: { color: 'var(--tf-text-secondary)' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: 'var(--tf-text-secondary)' }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      itemStyle: { borderRadius: [6, 6, 0, 0] },
      data: ['URGENT', 'HIGH', 'MEDIUM', 'LOW'].map(p => ({
        value: priorityMap[p],
        itemStyle: { color: priorityColors[p] }
      }))
    }]
  }
})

async function loadDashboard() {
  if (!workspaceStore.currentWorkspace) return
  dashLoading.value = true
  const wsId = workspaceStore.currentWorkspace.id
  try {
    const [totalRes, inProgressRes, doneRes, recentRes] = await Promise.all([
      taskApi.list(wsId, { size: 1 }),
      taskApi.list(wsId, { status: 'IN_PROGRESS' as any, size: 1 }),
      taskApi.list(wsId, { status: 'DONE' as any, size: 1 }),
      taskApi.list(wsId, { size: 50 })
    ])
    stats.value.totalTasks = totalRes.data.total
    stats.value.inProgress = inProgressRes.data.total
    stats.value.completed = doneRes.data.total
    const all = recentRes.data.records
    allTasks.value = all
    stats.value.overdue = all.filter(t => t.dueDate && dayjs(t.dueDate).isBefore(dayjs(), 'day') && t.status !== 'DONE').length
    myTasks.value = all.filter(t => t.status !== 'DONE' && t.status !== 'CANCELLED').slice(0, 5)
    upcomingTasks.value = all.filter(t => t.dueDate && t.status !== 'DONE').sort((a, b) => dayjs(a.dueDate).diff(dayjs(b.dueDate))).slice(0, 5)

    // Load boards
    await boardStore.fetchBoards(wsId)
    boards.value = boardStore.boards
  } catch {} finally {
    dashLoading.value = false
  }
}

onMounted(loadDashboard)
watch(() => workspaceStore.currentWorkspace, loadDashboard)
watch(() => taskStore.taskEventVersion, loadDashboard)

function formatDate(date?: string) { return date ? dayjs(date).format('MMM DD, YYYY') : '' }
function isOverdue(date?: string) { return date ? dayjs(date).isBefore(dayjs(), 'day') : false }
</script>

<style lang="scss" scoped>
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; color: var(--tf-text-primary); }
.section-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; color: var(--tf-text-primary); }
.mb-16 { margin-bottom: 20px; }

.stat-card {
  background: var(--tf-bg-card);
  border: 1px solid var(--tf-border-color-lighter);
  border-radius: var(--tf-radius-lg);
  padding: 20px;
  position: relative;
  overflow: hidden;
  transition: all var(--tf-transition-normal);
  cursor: default;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: var(--stat-gradient);
  }

  &:hover {
    box-shadow: var(--tf-shadow-hover);
    transform: translateY(-2px);
  }
}

.stat-icon-wrap {
  width: 42px; height: 42px; border-radius: var(--tf-radius-md);
  display: flex; align-items: center; justify-content: center;
  background: var(--stat-gradient); color: #fff;
  margin-bottom: 14px;
}

.stat-value {
  font-size: 32px; font-weight: 700; color: var(--tf-text-primary);
  line-height: 1; margin-bottom: 4px;
}

.stat-label { color: var(--tf-text-secondary); font-size: 13px; font-weight: 500; }

.board-summary-card {
  transition: all var(--tf-transition-normal);
  &:hover { transform: translateY(-3px); box-shadow: var(--tf-shadow-hover); }
}
.board-color-bar {
  display: flex; height: 4px; border-radius: 2px; overflow: hidden; margin-bottom: 12px; gap: 2px;
}
.color-segment { flex: 1; min-width: 8px; border-radius: 1px; }
.board-summary-name { font-size: 14px; font-weight: 600; color: var(--tf-text-primary); margin-bottom: 8px; }
.board-summary-meta { display: flex; gap: 8px; }

.task-list { max-height: 300px; overflow-y: auto; }
.task-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; cursor: pointer;
  border: 1px solid var(--tf-border-color-lighter);
  border-radius: var(--tf-radius-md);
  margin-bottom: 8px;
  transition: all var(--tf-transition-fast);
  &:hover {
    border-color: var(--tf-color-primary);
    box-shadow: 0 0 0 3px var(--tf-color-primary-lighter);
    background: var(--tf-color-primary-lighter);
  }
}
.task-info { display: flex; align-items: center; gap: 8px; flex: 1; min-width: 0; }
.task-title { font-size: 14px; color: var(--tf-text-primary); font-weight: 500; }
.due-date {
  font-size: 13px; color: var(--tf-text-secondary); flex-shrink: 0;
  &.overdue { color: var(--tf-color-danger); font-weight: 600; }
}

.chart {
  height: 280px;
}
</style>
