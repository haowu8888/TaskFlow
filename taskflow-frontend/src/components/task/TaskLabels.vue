<template>
  <div class="task-labels">
    <div class="labels-list">
      <el-tag
        v-for="label in taskLabels"
        :key="label.id"
        :color="label.color"
        closable
        :disable-transitions="false"
        effect="dark"
        size="small"
        class="label-tag"
        @close="removeLabel(label.id)"
      >
        {{ label.name }}
      </el-tag>
      <el-popover v-if="canEdit" :width="240" trigger="click" placement="bottom-start">
        <template #reference>
          <el-button size="small" link class="add-label-btn">
            <el-icon><Plus /></el-icon> {{ $t('label.addLabel') }}
          </el-button>
        </template>
        <div class="label-popover">
          <el-input v-model="searchLabel" :placeholder="$t('label.searchOrCreate')" size="small" clearable class="label-search" />
          <div class="label-options">
            <div
              v-for="label in filteredLabels"
              :key="label.id"
              class="label-option"
              @click="toggleLabel(label)"
            >
              <span class="label-color-dot" :style="{ background: label.color }"></span>
              <span class="label-option-name">{{ label.name }}</span>
              <el-icon v-if="isSelected(label.id)" class="label-check"><Select /></el-icon>
            </div>
          </div>
          <div v-if="searchLabel.trim() && !exactMatch" class="create-label-row">
            <el-button size="small" type="primary" link @click="createAndAddLabel">
              {{ $t('label.create') }} "{{ searchLabel.trim() }}"
            </el-button>
          </div>
        </div>
      </el-popover>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Select } from '@element-plus/icons-vue'
import { labelApi } from '@/api/label'
import { useWorkspaceStore } from '@/stores/workspace'
import { usePermission } from '@/composables/usePermission'
import type { Label } from '@/types'

const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const { canEdit } = usePermission()

const props = defineProps<{ taskId: number; labels?: Label[] }>()
const emit = defineEmits<{ 'changed': [] }>()

const taskLabels = ref<Label[]>([...(props.labels || [])])
const allLabels = ref<Label[]>([])
const searchLabel = ref('')

const filteredLabels = computed(() => {
  if (!searchLabel.value.trim()) return allLabels.value
  const q = searchLabel.value.toLowerCase()
  return allLabels.value.filter(l => l.name.toLowerCase().includes(q))
})

const exactMatch = computed(() => {
  const q = searchLabel.value.trim().toLowerCase()
  return allLabels.value.some(l => l.name.toLowerCase() === q)
})

function isSelected(labelId: number): boolean {
  return taskLabels.value.some(l => l.id === labelId)
}

async function loadLabels() {
  if (!workspaceStore.currentWorkspace) return
  try {
    const res = await labelApi.list(workspaceStore.currentWorkspace.id)
    allLabels.value = res.data
  } catch {}
  try {
    const res = await labelApi.getTaskLabels(props.taskId)
    taskLabels.value = res.data
  } catch {}
}

async function toggleLabel(label: Label) {
  try {
    if (isSelected(label.id)) {
      await labelApi.removeFromTask(props.taskId, label.id)
      taskLabels.value = taskLabels.value.filter(l => l.id !== label.id)
    } else {
      await labelApi.addToTask(props.taskId, label.id)
      taskLabels.value.push(label)
    }
    emit('changed')
  } catch {}
}

async function removeLabel(labelId: number) {
  try {
    await labelApi.removeFromTask(props.taskId, labelId)
    taskLabels.value = taskLabels.value.filter(l => l.id !== labelId)
    emit('changed')
  } catch {}
}

async function createAndAddLabel() {
  if (!searchLabel.value.trim() || !workspaceStore.currentWorkspace) return
  const colors = ['#165DFF', '#00b42a', '#ff7d00', '#f53f3f', '#722ed1', '#eb2f96', '#13c2c2', '#faad14']
  const randomColor = colors[Math.floor(Math.random() * colors.length)]
  try {
    const res = await labelApi.create(workspaceStore.currentWorkspace.id, {
      name: searchLabel.value.trim(),
      color: randomColor
    })
    allLabels.value.push(res.data)
    await labelApi.addToTask(props.taskId, res.data.id)
    taskLabels.value.push(res.data)
    searchLabel.value = ''
    emit('changed')
  } catch {}
}

onMounted(loadLabels)
</script>

<style lang="scss" scoped>
.labels-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.label-tag {
  border: none;
  font-size: 12px;
}

.add-label-btn {
  font-size: 12px;
  color: var(--tf-text-secondary);
  &:hover { color: var(--tf-color-primary); }
}

.label-popover {
  max-height: 280px;
}

.label-search {
  margin-bottom: 8px;
}

.label-options {
  max-height: 200px;
  overflow-y: auto;
}

.label-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  cursor: pointer;
  border-radius: var(--tf-radius-sm);
  transition: background var(--tf-transition-fast);

  &:hover { background: var(--tf-bg-card-hover); }
}

.label-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}

.label-option-name {
  flex: 1;
  font-size: 13px;
  color: var(--tf-text-primary);
}

.label-check {
  color: var(--tf-color-primary);
}

.create-label-row {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--tf-border-color-lighter);
}
</style>
