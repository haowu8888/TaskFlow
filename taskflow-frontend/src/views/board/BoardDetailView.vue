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
      <el-button @click="showAddColumn = true">
        <el-icon><Plus /></el-icon> {{ $t('board.addColumn') }}
      </el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import KanbanColumn from '@/components/board/KanbanColumn.vue'
import TaskDetailDrawer from '@/components/task/TaskDetailDrawer.vue'
import { useBoardStore } from '@/stores/board'
import { boardApi } from '@/api/board'
import { taskApi } from '@/api/task'
import type { Task, BoardColumn } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()

const boardId = ref(0)
const columns = ref<BoardColumn[]>([])
const showAddColumn = ref(false)
const showEditColumn = ref(false)
const showTaskDetail = ref(false)
const selectedTaskId = ref(0)
const addingColumn = ref(false)
const updatingColumn = ref(false)
const editingColumnId = ref(0)

const colForm = reactive({ name: '', color: '#409EFF', wipLimit: 0 })
const editColForm = reactive({ name: '', color: '#409EFF', wipLimit: 0 })

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
    colForm.color = '#409EFF'
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
    await taskApi.move(taskId, { boardColumnId: columnId, position })
  } catch {
    await loadBoard()
  }
}

function openTask(task: Task) {
  selectedTaskId.value = task.id
  showTaskDetail.value = true
}

onMounted(loadBoard)
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  margin: 0;
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
  background: #ECF5FF;
  border: 2px dashed #409EFF;
  border-radius: 8px;
}

.wip-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
