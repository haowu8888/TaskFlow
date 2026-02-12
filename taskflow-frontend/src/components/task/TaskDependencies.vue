<template>
  <div class="task-dependencies">
    <div v-if="dependencies.length > 0" class="dep-list">
      <div v-for="dep in dependencies" :key="dep.id" class="dep-item">
        <div class="dep-info" @click="$emit('task-click', dep.relatedTaskId)">
          <el-icon class="dep-icon">
            <TopRight v-if="dep.direction === 'predecessor'" />
            <BottomLeft v-else />
          </el-icon>
          <span class="dep-direction">{{ dep.direction === 'predecessor' ? $t('task.blockedBy') : $t('task.blocks') }}</span>
          <span class="dep-task-title">{{ dep.relatedTaskTitle || `#${dep.relatedTaskId}` }}</span>
        </div>
        <el-button v-if="canEdit" type="danger" link size="small" @click="removeDep(dep)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    <div v-else class="dep-empty">{{ $t('task.noDependencies') }}</div>

    <div v-if="canEdit" class="add-dep">
      <el-select
        v-model="selectedTaskId"
        filterable
        remote
        :remote-method="searchTasks"
        :placeholder="$t('task.searchTaskToLink')"
        :loading="searching"
        clearable
        style="flex: 1"
        size="small"
      >
        <el-option v-for="t in searchResults" :key="t.id" :label="t.title" :value="t.id">
          <span class="search-option">
            <span class="search-option-id">#{{ t.id }}</span>
            <span>{{ t.title }}</span>
          </span>
        </el-option>
      </el-select>
      <el-select v-model="depType" size="small" style="width: 120px">
        <el-option :label="$t('task.blockedBy')" value="blockedBy" />
        <el-option :label="$t('task.blocks')" value="blocks" />
      </el-select>
      <el-button type="primary" size="small" @click="addDep" :disabled="!selectedTaskId">
        {{ $t('common.add') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Delete, TopRight, BottomLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { taskApi } from '@/api/task'
import { useWorkspaceStore } from '@/stores/workspace'
import { usePermission } from '@/composables/usePermission'
import type { TaskDependency, Task } from '@/types'

interface DependencyDisplay {
  id: number
  direction: 'predecessor' | 'successor'
  relatedTaskId: number
  relatedTaskTitle?: string
  raw: TaskDependency
}

const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const { canEdit } = usePermission()

const props = defineProps<{ taskId: number }>()
const emit = defineEmits<{ 'task-click': [taskId: number]; 'changed': [] }>()

const dependencies = ref<DependencyDisplay[]>([])
const selectedTaskId = ref<number | null>(null)
const depType = ref<'blockedBy' | 'blocks'>('blockedBy')
const searching = ref(false)
const searchResults = ref<Task[]>([])

async function loadDependencies() {
  try {
    const res = await taskApi.getDependencies(props.taskId)
    dependencies.value = res.data.map((dep: TaskDependency) => {
      if (dep.successorTaskId === props.taskId) {
        return {
          id: dep.id,
          direction: 'predecessor' as const,
          relatedTaskId: dep.predecessorTaskId,
          raw: dep
        }
      } else {
        return {
          id: dep.id,
          direction: 'successor' as const,
          relatedTaskId: dep.successorTaskId,
          raw: dep
        }
      }
    })
    // Fetch task titles
    for (const dep of dependencies.value) {
      try {
        const taskRes = await taskApi.get(dep.relatedTaskId)
        dep.relatedTaskTitle = taskRes.data.title
      } catch {}
    }
  } catch {}
}

async function searchTasks(query: string) {
  if (!query || query.length < 2 || !workspaceStore.currentWorkspace) {
    searchResults.value = []
    return
  }
  searching.value = true
  try {
    const res = await taskApi.list(workspaceStore.currentWorkspace.id, { keyword: query, size: 10 })
    searchResults.value = res.data.records.filter((t: Task) => t.id !== props.taskId)
  } catch {} finally {
    searching.value = false
  }
}

async function addDep() {
  if (!selectedTaskId.value) return
  try {
    const data = depType.value === 'blockedBy'
      ? { predecessorTaskId: selectedTaskId.value, successorTaskId: props.taskId }
      : { predecessorTaskId: props.taskId, successorTaskId: selectedTaskId.value }
    await taskApi.addDependency(props.taskId, data)
    ElMessage.success(t('task.dependencyAdded'))
    selectedTaskId.value = null
    searchResults.value = []
    await loadDependencies()
    emit('changed')
  } catch {}
}

async function removeDep(dep: DependencyDisplay) {
  try {
    await taskApi.removeDependency(props.taskId, dep.raw.predecessorTaskId, dep.raw.successorTaskId)
    ElMessage.success(t('task.dependencyRemoved'))
    await loadDependencies()
    emit('changed')
  } catch {}
}

onMounted(loadDependencies)
</script>

<style lang="scss" scoped>
.dep-list {
  margin-bottom: 12px;
}

.dep-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border: 1px solid var(--tf-border-color-lighter);
  border-radius: var(--tf-radius-sm);
  margin-bottom: 6px;
  transition: all var(--tf-transition-fast);

  &:hover {
    border-color: var(--tf-color-primary);
    background: var(--tf-color-primary-lighter);
  }
}

.dep-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
}

.dep-icon {
  color: var(--tf-text-secondary);
  flex-shrink: 0;
}

.dep-direction {
  font-size: 12px;
  color: var(--tf-text-secondary);
  flex-shrink: 0;
}

.dep-task-title {
  font-size: 13px;
  color: var(--tf-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dep-empty {
  color: var(--tf-text-placeholder);
  font-size: 13px;
  margin-bottom: 12px;
}

.add-dep {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-option {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-option-id {
  color: var(--tf-text-secondary);
  font-size: 12px;
}
</style>
