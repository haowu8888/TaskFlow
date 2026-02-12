<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('task.taskDetail')"
    size="640px"
    destroy-on-close
  >
    <div v-if="task" class="task-detail" v-loading="loading">
      <div class="detail-section">
        <el-input
          v-model="task.title"
          class="title-input"
          :readonly="!canEdit"
          @blur="canEdit && updateField('title', task.title)"
        />
      </div>

      <div class="detail-section">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="field-label">{{ $t('task.status') }}</div>
            <el-select v-model="task.status" :disabled="!canEdit" @change="updateField('status', task.status)" style="width: 100%">
              <el-option :label="$t('status.todo')" value="TODO" />
              <el-option :label="$t('status.inProgress')" value="IN_PROGRESS" />
              <el-option :label="$t('status.inReview')" value="IN_REVIEW" />
              <el-option :label="$t('status.done')" value="DONE" />
              <el-option :label="$t('status.cancelled')" value="CANCELLED" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <div class="field-label">{{ $t('task.priority') }}</div>
            <el-select v-model="task.priority" :disabled="!canEdit" @change="updateField('priority', task.priority)" style="width: 100%">
              <el-option :label="$t('priority.urgent')" value="URGENT" />
              <el-option :label="$t('priority.high')" value="HIGH" />
              <el-option :label="$t('priority.medium')" value="MEDIUM" />
              <el-option :label="$t('priority.low')" value="LOW" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <div class="field-label">{{ $t('task.assignee') }}</div>
            <el-select
              v-model="task.assigneeId"
              :placeholder="$t('common.unassigned')"
              :disabled="!canEdit"
              clearable
              style="width: 100%"
              @change="updateField('assigneeId', task.assigneeId || null)"
            >
              <el-option
                v-for="member in workspaceStore.members"
                :key="member.userId"
                :label="member.username"
                :value="member.userId"
              >
                <div class="assignee-option">
                  <el-avatar :size="20">{{ member.username?.charAt(0)?.toUpperCase() }}</el-avatar>
                  <span>{{ member.username }}</span>
                </div>
              </el-option>
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- Board Column Assignment -->
      <div class="detail-section" v-if="allBoardColumns.length > 0">
        <div class="field-label">{{ $t('task.boardColumn') }}</div>
        <el-select
          v-model="task.boardColumnId"
          :placeholder="$t('task.selectBoardColumn')"
          :disabled="!canEdit"
          clearable
          style="width: 100%"
          @change="updateField('boardColumnId', task.boardColumnId || null)"
        >
          <el-option-group v-for="board in boardsWithColumns" :key="board.id" :label="board.name">
            <el-option v-for="col in board.columns" :key="col.id" :label="col.name" :value="col.id">
              <div class="column-option">
                <span class="column-color-dot" :style="{ background: col.color || '#165DFF' }"></span>
                <span>{{ col.name }}</span>
              </div>
            </el-option>
          </el-option-group>
        </el-select>
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('label.labels') }}</div>
        <TaskLabels :task-id="task.id" :labels="task.labels" @changed="emit('updated')" />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.progress') }}</div>
        <el-slider v-model="task.progress" :disabled="!canEdit" @change="updateField('progress', task.progress)" :max="100" :step="5" show-stops />
      </div>

      <div class="detail-section">
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="field-label">{{ $t('task.startDate') }}</div>
            <el-date-picker
              v-model="task.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="$t('task.selectStartDate')"
              :disabled="!canEdit"
              style="width: 100%"
              @change="updateField('startDate', task.startDate)"
            />
          </el-col>
          <el-col :span="12">
            <div class="field-label">{{ $t('task.dueDate') }}</div>
            <el-date-picker
              v-model="task.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              :placeholder="$t('task.selectDueDate')"
              :disabled="!canEdit"
              style="width: 100%"
              @change="updateField('dueDate', task.dueDate)"
            />
          </el-col>
        </el-row>
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.description') }}</div>
        <el-input
          v-model="task.description"
          type="textarea"
          :rows="4"
          :readonly="!canEdit"
          @blur="canEdit && updateField('description', task.description)"
          :placeholder="$t('task.addDescription')"
        />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.dependencies') }}</div>
        <TaskDependencies :task-id="task.id" @changed="emit('updated')" />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.subtasks') }}</div>
        <TaskSubtasks :task-id="task.id" :subtasks="task.subtasks || []" />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.comments') }}</div>
        <TaskComments :task-id="task.id" />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('activity.title') }}</div>
        <TaskActivityLog :task-id="task.id" />
      </div>

      <div v-if="canEdit" class="detail-section delete-section">
        <el-popconfirm
          :title="$t('task.deleteConfirm')"
          :confirm-button-text="$t('common.delete')"
          :cancel-button-text="$t('common.cancel')"
          confirm-button-type="danger"
          @confirm="handleDelete"
        >
          <template #reference>
            <el-button type="danger" plain>{{ $t('task.deleteTask') }}</el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>
    <div v-else-if="loading" v-loading="true" class="loading-placeholder"></div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import TaskSubtasks from '@/components/task/TaskSubtasks.vue'
import TaskComments from '@/components/task/TaskComments.vue'
import TaskDependencies from '@/components/task/TaskDependencies.vue'
import TaskLabels from '@/components/task/TaskLabels.vue'
import TaskActivityLog from '@/components/task/TaskActivityLog.vue'
import { taskApi } from '@/api/task'
import { boardApi } from '@/api/board'
import { useWorkspaceStore } from '@/stores/workspace'
import { useTaskStore } from '@/stores/task'
import { usePermission } from '@/composables/usePermission'
import type { Task, Board } from '@/types'

const { t } = useI18n()
const props = defineProps<{ modelValue: boolean; taskId: number }>()
const emit = defineEmits(['update:modelValue', 'updated', 'deleted'])

const workspaceStore = useWorkspaceStore()
const taskStore = useTaskStore()
const { canEdit } = usePermission()
const task = ref<Task | null>(null)
const loading = ref(false)
const boards = ref<Board[]>([])

const boardsWithColumns = computed(() => {
  return boards.value.filter(b => b.columns && b.columns.length > 0)
})

const allBoardColumns = computed(() => {
  return boardsWithColumns.value.flatMap(b => b.columns || [])
})

async function loadTask(id: number) {
  loading.value = true
  try {
    const res = await taskApi.get(id)
    task.value = res.data
    if (workspaceStore.currentWorkspace) {
      workspaceStore.fetchMembers(workspaceStore.currentWorkspace.id)
      // Load boards for column selector
      try {
        const boardRes = await boardApi.list(workspaceStore.currentWorkspace.id)
        boards.value = boardRes.data
      } catch {}
    }
  } catch {} finally {
    loading.value = false
  }
}

watch(() => props.taskId, (id) => {
  if (id && props.modelValue) loadTask(id)
})

watch(() => props.modelValue, (val) => {
  if (val && props.taskId) loadTask(props.taskId)
  else if (!val) { task.value = null; boards.value = [] }
})

async function updateField(field: string, value: any) {
  if (!task.value) return
  try {
    const res = await taskApi.update(task.value.id, { [field]: value })
    taskStore.updateTaskInList(task.value.id, res.data)
    taskStore.notifyTaskChange()
    emit('updated')
  } catch {}
}

async function handleDelete() {
  if (!task.value) return
  try {
    await taskApi.delete(task.value.id)
    taskStore.tasks = taskStore.tasks.filter(t => t.id !== task.value!.id)
    taskStore.notifyTaskChange()
    ElMessage.success(t('task.taskDeleted'))
    emit('update:modelValue', false)
    emit('deleted')
  } catch {}
}
</script>

<style lang="scss" scoped>
.task-detail { padding: 0 4px; }
.detail-section { margin-bottom: 22px; }
.field-label {
  font-size: 12px; color: var(--tf-text-secondary); margin-bottom: 8px;
  font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;
}
.title-input {
  :deep(.el-input__inner) { font-size: 18px; font-weight: 600; border: none; padding: 0; }
}
.assignee-option { display: flex; align-items: center; gap: 8px; }
.column-option { display: flex; align-items: center; gap: 8px; }
.column-color-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; box-shadow: 0 0 0 2px rgba(0,0,0,0.05); }
.delete-section {
  margin-top: 32px; padding-top: 20px;
  border-top: 1px solid var(--tf-border-color-lighter);
}
.loading-placeholder { min-height: 200px; }
</style>
