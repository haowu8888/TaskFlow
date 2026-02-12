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
          @blur="updateField('title', task.title)"
        />
      </div>

      <div class="detail-section">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="field-label">{{ $t('task.status') }}</div>
            <el-select v-model="task.status" @change="updateField('status', task.status)" style="width: 100%">
              <el-option :label="$t('status.todo')" value="TODO" />
              <el-option :label="$t('status.inProgress')" value="IN_PROGRESS" />
              <el-option :label="$t('status.inReview')" value="IN_REVIEW" />
              <el-option :label="$t('status.done')" value="DONE" />
              <el-option :label="$t('status.cancelled')" value="CANCELLED" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <div class="field-label">{{ $t('task.priority') }}</div>
            <el-select v-model="task.priority" @change="updateField('priority', task.priority)" style="width: 100%">
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

      <div class="detail-section">
        <div class="field-label">{{ $t('task.progress') }}</div>
        <el-slider v-model="task.progress" @change="updateField('progress', task.progress)" :max="100" :step="5" show-stops />
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
          @blur="updateField('description', task.description)"
          :placeholder="$t('task.addDescription')"
        />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.subtasks') }}</div>
        <TaskSubtasks :task-id="task.id" :subtasks="task.subtasks || []" />
      </div>

      <div class="detail-section">
        <div class="field-label">{{ $t('task.comments') }}</div>
        <TaskComments :task-id="task.id" />
      </div>

      <div class="detail-section delete-section">
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
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import TaskSubtasks from '@/components/task/TaskSubtasks.vue'
import TaskComments from '@/components/task/TaskComments.vue'
import { taskApi } from '@/api/task'
import { useWorkspaceStore } from '@/stores/workspace'
import type { Task } from '@/types'

const { t } = useI18n()
const props = defineProps<{ modelValue: boolean; taskId: number }>()
const emit = defineEmits(['update:modelValue', 'updated', 'deleted'])

const workspaceStore = useWorkspaceStore()
const task = ref<Task | null>(null)
const loading = ref(false)

async function loadTask(id: number) {
  loading.value = true
  try {
    const res = await taskApi.get(id)
    task.value = res.data
    if (workspaceStore.currentWorkspace) {
      workspaceStore.fetchMembers(workspaceStore.currentWorkspace.id)
    }
  } catch {
    // Error handled by API interceptor
  } finally {
    loading.value = false
  }
}

watch(() => props.taskId, (id) => {
  if (id && props.modelValue) {
    loadTask(id)
  }
})

watch(() => props.modelValue, (val) => {
  if (val && props.taskId) {
    loadTask(props.taskId)
  } else if (!val) {
    task.value = null
  }
})

async function updateField(field: string, value: any) {
  if (!task.value) return
  try {
    await taskApi.update(task.value.id, { [field]: value })
    emit('updated')
  } catch {
    // Error handled by API interceptor
  }
}

async function handleDelete() {
  if (!task.value) return
  try {
    await taskApi.delete(task.value.id)
    ElMessage.success(t('task.taskDeleted'))
    emit('update:modelValue', false)
    emit('deleted')
  } catch {
    // Error handled by API interceptor
  }
}
</script>

<style lang="scss" scoped>
.task-detail {
  padding: 0 4px;
}

.detail-section {
  margin-bottom: 20px;
}

.field-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

.title-input {
  :deep(.el-input__inner) {
    font-size: 18px;
    font-weight: 600;
    border: none;
    padding: 0;
  }
}

.assignee-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.delete-section {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
}

.loading-placeholder {
  min-height: 200px;
}
</style>
