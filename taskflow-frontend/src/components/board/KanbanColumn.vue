<template>
  <div class="kanban-column">
    <div class="column-header" :style="{ borderTopColor: column.color || '#409EFF' }">
      <div class="column-header-left">
        <span class="column-color-dot" :style="{ background: column.color || '#409EFF' }"></span>
        <span class="column-name">{{ column.name }}</span>
        <el-tag size="small" type="info" round>{{ columnTasks.length }}</el-tag>
        <el-tag v-if="column.wipLimit > 0" size="small" :type="isOverWip ? 'danger' : 'info'" round>
          {{ $t('board.wip') }}: {{ column.wipLimit }}
        </el-tag>
      </div>
      <div class="column-header-actions">
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
      @end="handleDragEnd"
    >
      <KanbanCard
        v-for="task in columnTasks"
        :key="task.id"
        :task="task"
        @click="$emit('task-click', task)"
      />
    </VueDraggable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MoreFilled } from '@element-plus/icons-vue'
import { VueDraggable } from 'vue-draggable-plus'
import KanbanCard from '@/components/board/KanbanCard.vue'
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
}>()

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
  background: #F5F7FA;
  border-radius: 8px;
  border-top: 3px solid #409EFF;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 200px);
}

.column-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #E4E7ED;
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
}

.column-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.column-body {
  padding: 12px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 60px;
}

.ghost-card {
  opacity: 0.5;
  background: #E6F0FF;
  border: 2px dashed #409EFF;
  border-radius: 8px;
}
</style>
