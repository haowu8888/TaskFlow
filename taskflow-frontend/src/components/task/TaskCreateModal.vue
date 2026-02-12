<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('task.createTask')"
    width="560px"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item :label="$t('task.taskTitle')" prop="title">
        <el-input v-model="form.title" :placeholder="$t('task.enterTitle')" />
      </el-form-item>
      <el-form-item :label="$t('task.description')">
        <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('task.enterDescription')" />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="$t('task.priority')">
            <el-select v-model="form.priority" style="width: 100%">
              <el-option :label="$t('priority.urgent')" value="URGENT" />
              <el-option :label="$t('priority.high')" value="HIGH" />
              <el-option :label="$t('priority.medium')" value="MEDIUM" />
              <el-option :label="$t('priority.low')" value="LOW" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('task.status')">
            <el-select v-model="form.status" style="width: 100%">
              <el-option :label="$t('status.todo')" value="TODO" />
              <el-option :label="$t('status.inProgress')" value="IN_PROGRESS" />
              <el-option :label="$t('status.inReview')" value="IN_REVIEW" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- Board Column Selector -->
      <el-form-item :label="$t('task.boardColumn')" v-if="allBoardColumns.length > 0">
        <el-select v-model="form.boardColumnId" :placeholder="$t('task.selectBoardColumn')" clearable style="width: 100%">
          <el-option-group v-for="board in boardsWithColumns" :key="board.id" :label="board.name">
            <el-option
              v-for="col in board.columns"
              :key="col.id"
              :label="col.name"
              :value="col.id"
            >
              <div class="column-option">
                <span class="column-color-dot" :style="{ background: col.color || '#165DFF' }"></span>
                <span>{{ col.name }}</span>
              </div>
            </el-option>
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="$t('task.startDate')">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" :placeholder="$t('task.selectStartDate')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('task.dueDate')">
            <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" :placeholder="$t('task.selectDueDate')" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="$t('task.assignee')">
        <el-select v-model="form.assigneeId" :placeholder="$t('task.selectAssignee')" clearable style="width: 100%">
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
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">{{ $t('common.create') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance } from 'element-plus'
import { useTaskStore } from '@/stores/task'
import { useWorkspaceStore } from '@/stores/workspace'
import { boardApi } from '@/api/board'
import type { Board, BoardColumn } from '@/types'

const { t } = useI18n()
const props = defineProps<{
  modelValue: boolean
  boardColumnId?: number
  boardColumns?: BoardColumn[]
  initialDueDate?: string
}>()
const emit = defineEmits(['update:modelValue', 'created'])

const taskStore = useTaskStore()
const workspaceStore = useWorkspaceStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const fetchedBoards = ref<Board[]>([])

const form = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  startDate: '',
  dueDate: '',
  assigneeId: undefined as number | undefined,
  boardColumnId: undefined as number | undefined
})

const rules = {
  title: [{ required: true, message: () => t('task.titleRequired'), trigger: 'blur' }]
}

// If parent provides boardColumns (board context), use those; otherwise use fetched boards
const boardsWithColumns = computed(() => {
  if (props.boardColumns && props.boardColumns.length > 0) {
    return [{ id: 0, name: t('board.title'), columns: props.boardColumns }]
  }
  return fetchedBoards.value.filter(b => b.columns && b.columns.length > 0)
})

const allBoardColumns = computed(() => {
  return boardsWithColumns.value.flatMap(b => b.columns || [])
})

watch(() => props.modelValue, async (val) => {
  if (val) {
    if (workspaceStore.currentWorkspace) {
      workspaceStore.fetchMembers(workspaceStore.currentWorkspace.id)
      // Load boards if not provided by parent
      if (!props.boardColumns || props.boardColumns.length === 0) {
        try {
          const res = await boardApi.list(workspaceStore.currentWorkspace.id)
          fetchedBoards.value = res.data
        } catch {}
      }
    }
    if (props.boardColumnId) {
      form.boardColumnId = props.boardColumnId
    }
    if (props.initialDueDate) {
      form.dueDate = props.initialDueDate
    }
  }
})

function resetForm() {
  Object.assign(form, {
    title: '', description: '', priority: 'MEDIUM', status: 'TODO',
    startDate: '', dueDate: '', assigneeId: undefined, boardColumnId: undefined
  })
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !workspaceStore.currentWorkspace) return
  loading.value = true
  try {
    await taskStore.createTask(workspaceStore.currentWorkspace.id, {
      title: form.title,
      description: form.description || undefined,
      priority: form.priority as any,
      status: form.status as any,
      startDate: form.startDate || undefined,
      dueDate: form.dueDate || undefined,
      assigneeId: form.assigneeId || undefined,
      boardColumnId: form.boardColumnId || undefined
    })
    ElMessage.success(t('task.taskCreated'))
    resetForm()
    emit('created')
  } catch {} finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.assignee-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
.column-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
.column-color-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
