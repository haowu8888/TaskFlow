<template>
  <div class="board-detail-view">
    <div class="page-header flex-between">
      <div class="header-left">
        <el-button text @click="router.push('/boards')">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">{{ boardStore.currentBoard?.name || $t('board.title') }}</h2>
        <el-tag v-if="boardStore.currentBoard?.description" type="info" size="small">
          {{ boardStore.currentBoard.description }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button v-if="canEdit" type="primary" @click="openCreateTask()">
          <el-icon><Plus /></el-icon> {{ $t('task.newTask') }}
        </el-button>
        <el-button v-if="canAdmin" @click="showAddColumn = true">
          <el-icon><Plus /></el-icon> {{ $t('board.addColumn') }}
        </el-button>
      </div>
    </div>

    <div class="kanban-container" v-loading="boardStore.loading">
      <VueDraggable
        v-model="columns"
        class="kanban-columns"
        group="kanban-columns"
        :animation="200"
        handle=".column-header"
        ghost-class="ghost-column"
        @end="handleColumnReorder"
      >
        <KanbanColumn
          v-for="column in columns"
          :key="column.id"
          :column="column"
          :board-id="boardId"
          @task-click="openTask"
          @task-moved="handleTaskMoved"
          @edit-column="handleEditColumn"
          @delete-column="handleDeleteColumn"
          @add-task="openCreateTaskForColumn"
        />
      </VueDraggable>

      <div v-if="!boardStore.loading && columns.length === 0" class="empty-board">
        <el-empty :description="$t('board.noColumns')">
          <el-button type="primary" @click="showAddColumn = true">{{ $t('board.addColumn') }}</el-button>
        </el-empty>
      </div>
    </div>

    <!-- Add Column Dialog -->
    <el-dialog v-model="showAddColumn" :title="$t('board.addColumn')" width="400px" destroy-on-close>
      <el-form :model="colForm" label-position="top">
        <el-form-item :label="$t('board.columnName')">
          <el-input v-model="colForm.name" :placeholder="$t('board.columnNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('board.color')">
          <el-color-picker v-model="colForm.color" />
        </el-form-item>
        <el-form-item :label="$t('board.wipLimit')">
          <el-input-number v-model="colForm.wipLimit" :min="0" :max="100" />
          <span class="wip-hint">{{ $t('board.wipLimitHint') }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddColumn = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="addColumn" :loading="addingColumn">{{ $t('board.addColumn') }}</el-button>
      </template>
    </el-dialog>

    <!-- Edit Column Dialog -->
    <el-dialog v-model="showEditColumn" :title="$t('board.editColumn')" width="400px" destroy-on-close>
      <el-form :model="editColForm" label-position="top">
        <el-form-item :label="$t('board.columnName')">
          <el-input v-model="editColForm.name" />
        </el-form-item>
        <el-form-item :label="$t('board.color')">
          <el-color-picker v-model="editColForm.color" />
        </el-form-item>
        <el-form-item :label="$t('board.wipLimit')">
          <el-input-number v-model="editColForm.wipLimit" :min="0" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditColumn = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="updateColumn" :loading="updatingColumn">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <TaskDetailDrawer
      v-model="showTaskDetail"
      :task-id="selectedTaskId"
      @updated="loadBoard"
      @deleted="loadBoard"
    />

    <TaskCreateModal
      v-model="showCreateTask"
      :board-column-id="createTaskColumnId"
      :board-columns="columns"
      @created="handleBoardTaskCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import KanbanColumn from '@/components/board/KanbanColumn.vue'
import TaskDetailDrawer from '@/components/task/TaskDetailDrawer.vue'
import TaskCreateModal from '@/components/task/TaskCreateModal.vue'
import { useBoardStore } from '@/stores/board'
import { useTaskStore } from '@/stores/task'
import { usePermission } from '@/composables/usePermission'
import { boardApi } from '@/api/board'
import { taskApi } from '@/api/task'
import type { Task, BoardColumn } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const taskStore = useTaskStore()
const { canEdit, canAdmin } = usePermission()

const boardId = ref(0)
const columns = ref<BoardColumn[]>([])
const showAddColumn = ref(false)
const showEditColumn = ref(false)
const showTaskDetail = ref(false)
const showCreateTask = ref(false)
const selectedTaskId = ref(0)
const addingColumn = ref(false)
const updatingColumn = ref(false)
const editingColumnId = ref(0)
const createTaskColumnId = ref<number | undefined>(undefined)

const colForm = reactive({ name: '', color: '#165DFF', wipLimit: 0 })
const editColForm = reactive({ name: '', color: '#165DFF', wipLimit: 0 })

let reloadTimer: ReturnType<typeof setTimeout> | null = null

async function loadBoard() {
  const id = Number(route.params.id)
  if (!id) return
  boardId.value = id
  const board = await boardStore.fetchBoard(id)
  if (board?.columns) {
    columns.value = board.columns.map(c => ({
      ...c,
      tasks: c.tasks || []
    }))
  }
}

// Watch taskEventVersion to reload board (debounced 2s)
watch(() => taskStore.taskEventVersion, () => {
  if (reloadTimer) clearTimeout(reloadTimer)
  reloadTimer = setTimeout(() => {
    loadBoard()
  }, 2000)
})

async function addColumn() {
  if (!colForm.name.trim()) return
  addingColumn.value = true
  try {
    await boardApi.addColumn(boardId.value, {
      name: colForm.name,
      color: colForm.color,
      wipLimit: colForm.wipLimit
    })
    ElMessage.success(t('board.columnAdded'))
    showAddColumn.value = false
    colForm.name = ''
    colForm.color = '#165DFF'
    colForm.wipLimit = 0
    await loadBoard()
  } catch {
    // Error handled by API interceptor
  } finally {
    addingColumn.value = false
  }
}

function handleEditColumn(column: BoardColumn) {
  editingColumnId.value = column.id
  editColForm.name = column.name
  editColForm.color = column.color
  editColForm.wipLimit = column.wipLimit
  showEditColumn.value = true
}

async function updateColumn() {
  if (!editColForm.name.trim()) return
  updatingColumn.value = true
  try {
    await boardApi.updateColumn(editingColumnId.value, {
      name: editColForm.name,
      color: editColForm.color,
      wipLimit: editColForm.wipLimit
    })
    ElMessage.success(t('board.columnUpdated'))
    showEditColumn.value = false
    await loadBoard()
  } catch {
    // Error handled by API interceptor
  } finally {
    updatingColumn.value = false
  }
}

async function handleDeleteColumn(columnId: number) {
  try {
    await ElMessageBox.confirm(
      t('board.deleteColumnConfirm'),
      t('common.confirm'),
      { type: 'warning' }
    )
    await boardApi.deleteColumn(columnId)
    ElMessage.success(t('board.columnDeleted'))
    await loadBoard()
  } catch {
    // User cancelled or delete failed
  }
}

async function handleColumnReorder() {
  const items = columns.value.map((col, index) => ({
    id: col.id,
    position: index
  }))
  try {
    await boardApi.reorderColumns(boardId.value, items)
  } catch {
    await loadBoard()
  }
}

async function handleTaskMoved(taskId: number, columnId: number, position: number) {
  try {
    const res = await taskApi.move(taskId, { boardColumnId: columnId, position })
    taskStore.updateTaskInList(taskId, res.data)
    taskStore.notifyTaskChange()
  } catch {
    await loadBoard()
  }
}

function openTask(task: Task) {
  selectedTaskId.value = task.id
  showTaskDetail.value = true
}

function openCreateTask() {
  createTaskColumnId.value = columns.value.length > 0 ? columns.value[0].id : undefined
  showCreateTask.value = true
}

function openCreateTaskForColumn(columnId: number) {
  createTaskColumnId.value = columnId
  showCreateTask.value = true
}

function handleBoardTaskCreated() {
  showCreateTask.value = false
  loadBoard()
}

onMounted(loadBoard)
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: var(--tf-text-primary);
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.kanban-container {
  overflow-x: auto;
  padding-bottom: 20px;
}

.kanban-columns {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 200px);
  align-items: flex-start;
}

.empty-board {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.ghost-column {
  opacity: 0.5;
  background: var(--tf-bg-card-hover);
  border: 2px dashed var(--tf-color-primary);
  border-radius: var(--tf-radius-md);
}

.wip-hint {
  margin-left: 8px;
  font-size: 12px;
  color: var(--tf-text-secondary);
}
</style>
