<template>
  <div class="dashboard-view">
    <h2 class="page-title">{{ $t('dashboard.title') }}</h2>
    <el-row :gutter="20" class="mb-16">
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card"><div class="stat-value">{{ stats.totalTasks }}</div><div class="stat-label">{{ $t('dashboard.totalTasks') }}</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card"><div class="stat-value text-primary">{{ stats.inProgress }}</div><div class="stat-label">{{ $t('dashboard.inProgress') }}</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card"><div class="stat-value text-success">{{ stats.completed }}</div><div class="stat-label">{{ $t('dashboard.completed') }}</div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card"><div class="stat-value text-danger">{{ stats.overdue }}</div><div class="stat-label">{{ $t('dashboard.overdue') }}</div></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>{{ $t('dashboard.myTasks') }}</span></template>
          <el-empty v-if="myTasks.length === 0" :description="$t('dashboard.noTasks')" />
          <div v-else class="task-list">
            <div v-for="task in myTasks" :key="task.id" class="task-item" @click="router.push('/tasks')">
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
            <div v-for="task in upcomingTasks" :key="task.id" class="task-item">
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
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const router = useRouter()
const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const stats = ref({ totalTasks: 0, inProgress: 0, completed: 0, overdue: 0 })
const myTasks = ref<Task[]>([])
const upcomingTasks = ref<Task[]>([])

async function loadDashboard() {
  if (!workspaceStore.currentWorkspace) return
  try {
    const res = await taskApi.list(workspaceStore.currentWorkspace.id, { size: 100 })
    const all = res.data.records
    stats.value.totalTasks = res.data.total
    stats.value.inProgress = all.filter(t => t.status === 'IN_PROGRESS').length
    stats.value.completed = all.filter(t => t.status === 'DONE').length
    stats.value.overdue = all.filter(t => t.dueDate && dayjs(t.dueDate).isBefore(dayjs(), 'day') && t.status !== 'DONE').length
    myTasks.value = all.filter(t => t.status !== 'DONE' && t.status !== 'CANCELLED').slice(0, 5)
    upcomingTasks.value = all.filter(t => t.dueDate && t.status !== 'DONE').sort((a, b) => dayjs(a.dueDate).diff(dayjs(b.dueDate))).slice(0, 5)
  } catch {}
}

onMounted(loadDashboard)
watch(() => workspaceStore.currentWorkspace, loadDashboard)

function formatDate(date?: string) { return date ? dayjs(date).format('MMM DD, YYYY') : '' }
function isOverdue(date?: string) { return date ? dayjs(date).isBefore(dayjs(), 'day') : false }
</script>

<style lang="scss" scoped>
.page-title { font-size: 24px; margin-bottom: 24px; }
.stat-card { text-align: center; padding: 8px 0; }
.stat-value { font-size: 32px; font-weight: 700; margin-bottom: 4px; }
.stat-label { color: #909399; font-size: 14px; }
.text-primary { color: #409EFF; }
.text-success { color: #67C23A; }
.text-danger { color: #F56C6C; }
.task-list { max-height: 300px; overflow-y: auto; }
.task-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 0; border-bottom: 1px solid #EBEEF5; cursor: pointer;
  &:last-child { border-bottom: none; }
  &:hover { background: #F5F7FA; }
}
.task-info { display: flex; align-items: center; gap: 8px; }
.task-title { font-size: 14px; }
.due-date { font-size: 13px; color: #909399; &.overdue { color: #F56C6C; font-weight: 500; } }
</style>
