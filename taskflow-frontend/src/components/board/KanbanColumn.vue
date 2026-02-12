<template>
  <div class="kanban-column" :class="{ 'wip-exceeded': isOverWip }">
    <div class="column-header" :style="{ borderTopColor: column.color || '#165DFF' }">
      <div class="column-header-left">
        <span class="column-color-dot" :style="{ background: column.color || '#165DFF' }"></span>
        <span class="column-name">{{ column.name }}</span>
        <el-tag size="small" :type="isOverWip ? 'danger' : 'info'" round>
          {{ columnTasks.length }}<template v-if="column.wipLimit > 0"> / {{ column.wipLimit }}</template>
        </el-tag>
        <el-icon v-if="isOverWip" class="wip-warning-icon" :size="16"><WarningFilled /></el-icon>
      </div>
      <div v-if="canAdmin" class="column-header-actions">
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button link size="small">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit">{{ $t('board.editColumn') }}</el-dropdown-item>
              <el-dropdown-item command="delete" divided>{{ $t('board.deleteColumn') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <VueDraggable
      v-model="columnTasks"
      class="column-body"
      group="kanban-tasks"
      :animation="200"
      ghost-class="ghost-card"
      :disabled="!canEdit"
      @end="handleDragEnd"
    >
      <KanbanCard
        v-for="task in columnTasks"
        :key="task.id"
        :task="task"
        @click="$emit('task-click', task)"
      />
    </VueDraggable>

    <div v-if="canEdit" class="column-footer">
      <el-button text class="add-task-btn" @click="$emit('add-task', column.id)">
        <el-icon><Plus /></el-icon> {{ $t('task.newTask') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MoreFilled, Plus, WarningFilled } from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import KanbanCard from '@/components/board/KanbanCard.vue'
import { usePermission } from '@/composables/usePermission'
import type { BoardColumn, Task } from '@/types'

const props = defineProps<{
  column: BoardColumn
  boardId: number
}>()

const emit = defineEmits<{
  'task-click': [task: Task]
  'task-moved': [taskId: number, columnId: number, position: number]
  'edit-column': [column: BoardColumn]
  'delete-column': [columnId: number]
  'add-task': [columnId: number]
}>()

const { canEdit, canAdmin } = usePermission()

const columnTasks = ref<Task[]>([...(props.column.tasks || [])])

watch(() => props.column.tasks, (newTasks) => {
  columnTasks.value = [...(newTasks || [])]
}, { deep: true })

const isOverWip = computed(() => {
  return props.column.wipLimit > 0 && columnTasks.value.length > props.column.wipLimit
})

function handleDragEnd(evt: any) {
  if (evt.to !== evt.from || evt.oldIndex !== evt.newIndex) {
    const taskEl = columnTasks.value[evt.newIndex]
    if (taskEl) {
      emit('task-moved', taskEl.id, props.column.id, evt.newIndex)
    }
  }
}

function handleCommand(command: string) {
  if (command === 'edit') {
    emit('edit-column', props.column)
  } else if (command === 'delete') {
    emit('delete-column', props.column.id)
  }
}
</script>

<style lang="scss" scoped>
.kanban-column {
  min-width: 300px;
  max-width: 300px;
  background: var(--tf-bg-card);
  border-radius: var(--tf-radius-lg);
  border: 1px solid var(--tf-border-color-lighter);
  border-top: 3px solid var(--tf-color-primary);
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 200px);
  box-shadow: var(--tf-shadow-card);
  transition: all var(--tf-transition-normal);

  &:hover {
    box-shadow: var(--tf-shadow-base);
  }

  &.wip-exceeded {
    border-color: var(--tf-color-danger);
    border-top-color: var(--tf-color-danger);
    background: rgba(245, 63, 63, 0.02);

    .column-body {
      background: rgba(245, 63, 63, 0.04);
    }
  }
}

.wip-warning-icon {
  color: var(--tf-color-danger);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.column-header {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--tf-border-color-lighter);
}

.column-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-color-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(0,0,0,0.05);
}

.column-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--tf-text-primary);
}

.column-body {
  padding: 12px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 60px;
  background: var(--tf-bg-card-hover);
}

.column-footer {
  padding: 8px 12px;
  border-top: 1px solid var(--tf-border-color-lighter);
}

.add-task-btn {
  width: 100%;
  color: var(--tf-text-secondary);
  font-weight: 500;
  transition: all var(--tf-transition-fast);
  &:hover { color: var(--tf-color-primary); }
}

.ghost-card {
  opacity: 0.5;
  background: var(--tf-color-primary-lighter);
  border: 2px dashed var(--tf-color-primary);
  border-radius: var(--tf-radius-md);
}
</style>
