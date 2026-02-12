<template>
  <div class="task-subtasks">
    <div v-for="subtask in localSubtasks" :key="subtask.id" class="subtask-item">
      <el-checkbox :model-value="!!subtask.isCompleted" @change="toggleSubtask(subtask.id)" />
      <span class="subtask-title" :class="{ completed: subtask.isCompleted }">{{ subtask.title }}</span>
      <el-button type="danger" link size="small" @click="deleteSubtask(subtask.id)">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
    <div class="add-subtask">
      <el-input v-model="newTitle" :placeholder="$t('task.addSubtask')" size="small" @keyup.enter="addSubtask">
        <template #append>
          <el-button @click="addSubtask" :disabled="!newTitle.trim()">{{ $t('common.add') }}</el-button>
        </template>
      </el-input>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Close } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'
import type { Subtask } from '@/types'

const { t } = useI18n()

const props = defineProps<{ taskId: number; subtasks: Subtask[] }>()
const localSubtasks = ref<Subtask[]>([...props.subtasks])
const newTitle = ref('')

async function addSubtask() {
  if (!newTitle.value.trim()) return
  try {
    const res = await taskApi.createSubtask(props.taskId, { title: newTitle.value })
    localSubtasks.value.push(res.data)
    newTitle.value = ''
  } catch {
    // Error handled by API interceptor
  }
}

async function toggleSubtask(id: number) {
  try {
    const res = await taskApi.toggleSubtask(id)
    const idx = localSubtasks.value.findIndex(s => s.id === id)
    if (idx !== -1) localSubtasks.value[idx] = res.data
  } catch {
    // Error handled by API interceptor
  }
}

async function deleteSubtask(id: number) {
  try {
    await taskApi.deleteSubtask(id)
    localSubtasks.value = localSubtasks.value.filter(s => s.id !== id)
  } catch {
    // Error handled by API interceptor
  }
}
</script>

<style lang="scss" scoped>
.subtask-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #EBEEF5;
}

.subtask-title {
  flex: 1;
  font-size: 13px;

  &.completed {
    text-decoration: line-through;
    color: #C0C4CC;
  }
}

.add-subtask {
  margin-top: 8px;
}
</style>
