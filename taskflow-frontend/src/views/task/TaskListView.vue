<template>
  <div class="task-list-view">
    <div class="page-header flex-between">
      <h2 class="page-title">{{ $t('task.title') }}</h2>
      <el-button v-if="canEdit" type="primary" @click="showCreateModal = true">
        <el-icon><Plus /></el-icon> {{ $t('task.newTask') }}
      </el-button>
    </div>

    <el-card shadow="never" class="filter-card mb-16">
      <div class="filters">
        <el-input v-model="filters.keyword" :placeholder="$t('task.searchTasks')" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filters.status" :placeholder="$t('task.status')" clearable style="width: 140px" @change="handleSearch">
          <el-option :label="$t('status.todo')" value="TODO" />
          <el-option :label="$t('status.inProgress')" value="IN_PROGRESS" />
          <el-option :label="$t('status.inReview')" value="IN_REVIEW" />
          <el-option :label="$t('status.done')" value="DONE" />
          <el-option :label="$t('status.cancelled')" value="CANCELLED" />
        </el-select>
        <el-select v-model="filters.priority" :placeholder="$t('task.priority')" clearable style="width: 140px" @change="handleSearch">
          <el-option :label="$t('priority.urgent')" value="URGENT" />
          <el-option :label="$t('priority.high')" value="HIGH" />
          <el-option :label="$t('priority.medium')" value="MEDIUM" />
          <el-option :label="$t('priority.low')" value="LOW" />
        </el-select>
        <el-select v-model="filters.sortBy" style="width: 140px" @change="handleSearch">
          <el-option :label="$t('task.created')" value="createdAt" />
          <el-option :label="$t('task.dueDate')" value="dueDate" />
          <el-option :label="$t('task.priority')" value="priority" />
          <el-option :label="$t('task.taskTitle')" value="title" />
        </el-select>
        <el-button @click="resetFilters">{{ $t('common.reset') }}</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-skeleton :loading="taskStore.loading" animated :count="5">
        <template #template>
          <div style="display: flex; align-items: center; padding: 16px 0; border-bottom: 1px solid var(--tf-border-color-lighter);">
            <el-skeleton-item variant="text" style="width: 40%; margin-right: auto;" />
            <el-skeleton-item variant="rect" style="width: 60px; height: 22px; border-radius: 4px; margin-right: 16px;" />
            <el-skeleton-item variant="rect" style="width: 70px; height: 22px; border-radius: 10px; margin-right: 16px;" />
            <el-skeleton-item variant="circle" style="width: 24px; height: 24px; margin-right: 8px;" />
            <el-skeleton-item variant="text" style="width: 60px;" />
          </div>
        </template>
        <template #default>
          <el-table :data="taskStore.tasks" @row-click="openTaskDetail" row-class-name="cursor-pointer" stripe>
        <el-table-column prop="title" :label="$t('task.taskTitle')" min-width="200">
          <template #default="{ row }">
            <span class="task-title-cell">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('task.priority')" width="120" align="center">
          <template #default="{ row }">
            <el-select
              v-if="canEdit"
              v-model="row.priority"
              size="small"
              class="inline-select"
              @click.stop
              @change="handleInlineUpdate(row.id, 'priority', row.priority)"
            >
              <el-option :label="$t('priority.urgent')" value="URGENT" />
              <el-option :label="$t('priority.high')" value="HIGH" />
              <el-option :label="$t('priority.medium')" value="MEDIUM" />
              <el-option :label="$t('priority.low')" value="LOW" />
            </el-select>
            <PriorityBadge v-else :priority="row.priority" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('task.status')" width="140" align="center">
          <template #default="{ row }">
            <el-select
              v-if="canEdit"
              v-model="row.status"
              size="small"
              class="inline-select"
              @click.stop
              @change="handleInlineUpdate(row.id, 'status', row.status)"
            >
              <el-option :label="$t('status.todo')" value="TODO" />
              <el-option :label="$t('status.inProgress')" value="IN_PROGRESS" />
              <el-option :label="$t('status.inReview')" value="IN_REVIEW" />
              <el-option :label="$t('status.done')" value="DONE" />
              <el-option :label="$t('status.cancelled')" value="CANCELLED" />
            </el-select>
            <StatusTag v-else :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('task.assignee')" width="150">
          <template #default="{ row }">
            <div v-if="row.assignee" class="assignee-cell">
              <el-avatar :size="24">{{ row.assignee.username?.charAt(0)?.toUpperCase() }}</el-avatar>
              <span>{{ row.assignee.username }}</span>
            </div>
            <span v-else class="text-secondary">{{ $t('common.unassigned') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('task.dueDate')" width="120">
          <template #default="{ row }">
            <span v-if="row.dueDate" :class="{ 'text-danger': isOverdue(row.dueDate, row.status) }">
              {{ formatDate(row.dueDate) }}
            </span>
            <span v-else class="text-secondary">{{ $t('task.noDate') }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canEdit" :label="$t('common.actions')" width="100" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click.stop="handleDelete(row.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="taskStore.pagination.total > 0">
        <el-pagination
          v-model:current-page="filters.page"
          v-model:page-size="filters.size"
          :total="taskStore.pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
        </template>
      </el-skeleton>
    </el-card>

    <TaskCreateModal v-model="showCreateModal" @created="handleTaskCreated" />
    <TaskDetailDrawer v-model="showDetailDrawer" :task-id="selectedTaskId" @updated="handleSearch" @deleted="handleSearch" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import TaskCreateModal from '@/components/task/TaskCreateModal.vue'
import TaskDetailDrawer from '@/components/task/TaskDetailDrawer.vue'
import { useTaskStore } from '@/stores/task'
import { useWorkspaceStore } from '@/stores/workspace'
import { usePermission } from '@/composables/usePermission'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()
const workspaceStore = useWorkspaceStore()
const { canEdit } = usePermission()
const showCreateModal = ref(false)
const showDetailDrawer = ref(false)
const selectedTaskId = ref<number>(0)

const filters = reactive({
  keyword: '',
  status: '',
  priority: '',
  sortBy: 'createdAt',
  sortDir: 'desc' as const,
  page: 1,
  size: 20
})

function handleSearch() {
  if (!workspaceStore.currentWorkspace) return
  taskStore.fetchTasks(workspaceStore.currentWorkspace.id, {
    keyword: filters.keyword || undefined,
    status: (filters.status as any) || undefined,
    priority: (filters.priority as any) || undefined,
    sortBy: filters.sortBy,
    sortDir: filters.sortDir,
    page: filters.page,
    size: filters.size
  })
}

function resetFilters() {
  filters.keyword = ''
  filters.status = ''
  filters.priority = ''
  filters.sortBy = 'createdAt'
  filters.page = 1
  handleSearch()
}

function openTaskDetail(row: Task) {
  selectedTaskId.value = row.id
  showDetailDrawer.value = true
}

function handleTaskCreated() {
  showCreateModal.value = false
  handleSearch()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm(t('task.deleteConfirm'), t('common.confirm'), { type: 'warning' })
    await taskStore.deleteTask(id)
    ElMessage.success(t('task.taskDeleted'))
    handleSearch()
  } catch {
    // User cancelled or delete failed
  }
}

async function handleInlineUpdate(taskId: number, field: string, value: any) {
  try {
    const res = await taskApi.update(taskId, { [field]: value })
    taskStore.updateTaskInList(taskId, res.data)
    taskStore.notifyTaskChange()
  } catch {
    handleSearch()
  }
}

function formatDate(date: string) {
  return dayjs(date).format('MMM DD')
}

function isOverdue(date: string, status: string) {
  return status !== 'DONE' && dayjs(date).isBefore(dayjs(), 'day')
}

watch(() => workspaceStore.currentWorkspace, () => {
  filters.page = 1
  handleSearch()
})

onMounted(() => {
  handleSearch()
  // Check for taskId in query (e.g. from notification click)
  const taskId = route.query.taskId
  if (taskId) {
    selectedTaskId.value = Number(taskId)
    showDetailDrawer.value = true
    router.replace({ path: route.path })
  }
})
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--tf-text-primary);
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-card {
  :deep(.el-card__body) { padding: 14px 16px; }
  .filters {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    align-items: center;
  }
}

.mb-16 {
  margin-bottom: 16px;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.text-secondary {
  color: var(--tf-text-secondary);
  font-size: 13px;
}

.text-danger {
  color: var(--tf-color-danger);
  font-weight: 600;
}

.task-title-cell {
  font-weight: 500;
  color: var(--tf-text-primary);
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

:deep(.cursor-pointer) {
  cursor: pointer;
  &:hover .task-title-cell { color: var(--tf-color-primary); }
}

.inline-select {
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    background: transparent;
    padding: 0 8px;
    &:hover { background: var(--tf-bg-card-hover); border-radius: var(--tf-radius-sm); }
  }
  :deep(.el-input__inner) {
    font-size: 13px;
    text-align: center;
  }
}
</style>
