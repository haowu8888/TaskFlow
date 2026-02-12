import { defineStore } from 'pinia'
import { ref } from 'vue'
import { taskApi } from '@/api/task'
import { useWorkspaceStore } from '@/stores/workspace'
import type { Task, TaskCreateRequest, TaskUpdateRequest, TaskFilterParams } from '@/types'

export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const currentTask = ref<Task | null>(null)
  const pagination = ref({ total: 0, current: 1, size: 20, pages: 0 })
  const loading = ref(false)
  const lastFilterParams = ref<TaskFilterParams | undefined>()

  async function fetchTasks(workspaceId: number, params?: TaskFilterParams) {
    loading.value = true
    lastFilterParams.value = params
    try {
      const res = await taskApi.list(workspaceId, params)
      const pageData = res.data
      tasks.value = pageData.records
      pagination.value = { total: pageData.total, current: pageData.current, size: pageData.size, pages: pageData.pages }
    } finally {
      loading.value = false
    }
  }

  async function fetchTask(taskId: number) {
    const res = await taskApi.get(taskId)
    currentTask.value = res.data
    return res.data
  }

  async function createTask(workspaceId: number, data: TaskCreateRequest) {
    const res = await taskApi.create(workspaceId, data)
    return res.data
  }

  async function updateTask(taskId: number, data: TaskUpdateRequest) {
    const res = await taskApi.update(taskId, data)
    currentTask.value = res.data
    return res.data
  }

  async function deleteTask(taskId: number) {
    await taskApi.delete(taskId)
    tasks.value = tasks.value.filter(t => t.id !== taskId)
  }

  async function refreshCurrentView() {
    const workspaceStore = useWorkspaceStore()
    if (workspaceStore.currentWorkspace) {
      await fetchTasks(workspaceStore.currentWorkspace.id, lastFilterParams.value)
    }
  }

  return { tasks, currentTask, pagination, loading, fetchTasks, fetchTask, createTask, updateTask, deleteTask, refreshCurrentView }
})
