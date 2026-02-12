<template>
  <div class="task-filters">
    <el-input
      v-model="keyword"
      placeholder="Search tasks..."
      clearable
      style="width: 200px"
      @clear="emitChange"
      @keyup.enter="emitChange"
    >
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>
    <el-select v-model="status" placeholder="Status" clearable style="width: 140px" @change="emitChange">
      <el-option label="To Do" value="TODO" />
      <el-option label="In Progress" value="IN_PROGRESS" />
      <el-option label="In Review" value="IN_REVIEW" />
      <el-option label="Done" value="DONE" />
      <el-option label="Cancelled" value="CANCELLED" />
    </el-select>
    <el-select v-model="priority" placeholder="Priority" clearable style="width: 140px" @change="emitChange">
      <el-option label="Urgent" value="URGENT" />
      <el-option label="High" value="HIGH" />
      <el-option label="Medium" value="MEDIUM" />
      <el-option label="Low" value="LOW" />
    </el-select>
    <el-button @click="handleReset">Reset</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

const emit = defineEmits<{
  'filter-change': [params: { status?: string; priority?: string; keyword?: string }]
}>()

const keyword = ref('')
const status = ref('')
const priority = ref('')

function emitChange() {
  emit('filter-change', {
    status: status.value || undefined,
    priority: priority.value || undefined,
    keyword: keyword.value || undefined
  })
}

function handleReset() {
  keyword.value = ''
  status.value = ''
  priority.value = ''
  emitChange()
}
</script>

<style lang="scss" scoped>
.task-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
