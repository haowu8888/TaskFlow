<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    width="560px"
    :show-close="false"
    class="global-search-dialog"
    top="15vh"
    destroy-on-close
    @opened="focusInput"
  >
    <div class="search-input-wrap">
      <el-icon :size="18" class="search-icon"><Search /></el-icon>
      <input
        ref="inputRef"
        v-model="query"
        :placeholder="$t('search.placeholder')"
        class="search-input"
        @input="handleSearch"
        @keydown.down.prevent="moveDown"
        @keydown.up.prevent="moveUp"
        @keydown.enter.prevent="selectCurrent"
        @keydown.esc="$emit('update:modelValue', false)"
      />
      <kbd class="search-kbd">ESC</kbd>
    </div>

    <div class="search-results" v-if="results.length > 0">
      <div
        v-for="(item, index) in results"
        :key="item.id"
        class="search-result-item"
        :class="{ active: index === activeIndex }"
        @click="goToTask(item)"
        @mouseenter="activeIndex = index"
      >
        <div class="result-left">
          <PriorityBadge :priority="item.priority" />
          <span class="result-title">{{ item.title }}</span>
        </div>
        <StatusTag :status="item.status" />
      </div>
    </div>
    <div v-else-if="query.length >= 2 && !searching" class="search-empty">
      {{ $t('search.noResults') }}
    </div>
    <div v-else-if="searching" class="search-empty">
      {{ $t('common.loading') }}
    </div>

    <div class="search-footer">
      <span class="search-hint">
        <kbd>&uarr;</kbd><kbd>&darr;</kbd> {{ $t('search.navigate') }}
        <kbd>&crarr;</kbd> {{ $t('search.select') }}
      </span>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search } from '@element-plus/icons-vue'
import PriorityBadge from '@/components/common/PriorityBadge.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { taskApi } from '@/api/task'
import type { Task } from '@/types'

const { t } = useI18n()
const router = useRouter()
const workspaceStore = useWorkspaceStore()

defineProps<{ modelValue: boolean }>()
const emit = defineEmits(['update:modelValue'])

const inputRef = ref<HTMLInputElement>()
const query = ref('')
const results = ref<Task[]>([])
const activeIndex = ref(0)
const searching = ref(false)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

function focusInput() {
  nextTick(() => inputRef.value?.focus())
}

function handleSearch() {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (query.value.length < 2) {
    results.value = []
    return
  }
  debounceTimer = setTimeout(async () => {
    if (!workspaceStore.currentWorkspace) return
    searching.value = true
    try {
      const res = await taskApi.list(workspaceStore.currentWorkspace.id, {
        keyword: query.value,
        size: 10
      })
      results.value = res.data.records
      activeIndex.value = 0
    } catch {} finally {
      searching.value = false
    }
  }, 300)
}

function moveDown() {
  if (activeIndex.value < results.value.length - 1) activeIndex.value++
}

function moveUp() {
  if (activeIndex.value > 0) activeIndex.value--
}

function selectCurrent() {
  if (results.value[activeIndex.value]) {
    goToTask(results.value[activeIndex.value])
  }
}

function goToTask(task: Task) {
  emit('update:modelValue', false)
  router.push(`/tasks?taskId=${task.id}`)
  query.value = ''
  results.value = []
}
</script>

<style lang="scss" scoped>
.search-input-wrap {
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--tf-border-color-lighter);
}

.search-icon {
  color: var(--tf-text-secondary);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 14px 12px;
  font-size: 16px;
  background: transparent;
  color: var(--tf-text-primary);

  &::placeholder {
    color: var(--tf-text-placeholder);
  }
}

.search-kbd {
  padding: 2px 6px;
  font-size: 11px;
  background: var(--tf-bg-card-hover);
  border: 1px solid var(--tf-border-color-light);
  border-radius: 4px;
  color: var(--tf-text-secondary);
  font-family: inherit;
}

.search-results {
  max-height: 360px;
  overflow-y: auto;
  padding: 8px;
}

.search-result-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: var(--tf-radius-sm);
  cursor: pointer;
  transition: all var(--tf-transition-fast);

  &:hover, &.active {
    background: var(--tf-color-primary-lighter);
  }
}

.result-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 14px;
  color: var(--tf-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-empty {
  padding: 24px;
  text-align: center;
  color: var(--tf-text-secondary);
  font-size: 14px;
}

.search-footer {
  padding: 10px 16px;
  border-top: 1px solid var(--tf-border-color-lighter);
}

.search-hint {
  font-size: 12px;
  color: var(--tf-text-placeholder);

  kbd {
    padding: 1px 4px;
    font-size: 11px;
    background: var(--tf-bg-card-hover);
    border: 1px solid var(--tf-border-color-light);
    border-radius: 3px;
    margin: 0 2px;
    font-family: inherit;
  }
}
</style>

<style lang="scss">
.global-search-dialog {
  .el-dialog__header { display: none; }
  .el-dialog__body { padding: 0; }
  .el-dialog { border-radius: var(--tf-radius-lg); overflow: hidden; }
}
</style>
