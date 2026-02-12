import api from './index'
import type { ApiResponse, Task, TaskCreateRequest, TaskUpdateRequest, TaskMoveRequest, TaskFilterParams, PageResult, Subtask, SubtaskCreateRequest, Comment, TaskDependency } from '@/types'

export const taskApi = {
  create(workspaceId: number, data: TaskCreateRequest) {
    return api.post<any, ApiResponse<Task>>(`/workspaces/${workspaceId}/tasks`, data)
  },
  list(workspaceId: number, params?: TaskFilterParams) {
    return api.get<any, ApiResponse<PageResult<Task>>>(`/workspaces/${workspaceId}/tasks`, { params })
  },
  get(taskId: number) {
    return api.get<any, ApiResponse<Task>>(`/tasks/${taskId}`)
  },
  update(taskId: number, data: TaskUpdateRequest) {
    return api.put<any, ApiResponse<Task>>(`/tasks/${taskId}`, data)
  },
  delete(taskId: number) {
    return api.delete<any, ApiResponse<void>>(`/tasks/${taskId}`)
  },
  updateStatus(taskId: number, status: string) {
    return api.put<any, ApiResponse<Task>>(`/tasks/${taskId}/status`, { status })
  },
  move(taskId: number, data: TaskMoveRequest) {
    return api.put<any, ApiResponse<Task>>(`/tasks/${taskId}/move`, data)
  },
  getCalendarTasks(workspaceId: number, start: string, end: string) {
    return api.get<any, ApiResponse<Task[]>>(`/workspaces/${workspaceId}/tasks/calendar`, { params: { start, end } })
  },
  getGanttTasks(workspaceId: number) {
    return api.get<any, ApiResponse<Task[]>>(`/workspaces/${workspaceId}/tasks/gantt`)
  },
  createSubtask(taskId: number, data: SubtaskCreateRequest) {
    return api.post<any, ApiResponse<Subtask>>(`/tasks/${taskId}/subtasks`, data)
  },
  updateSubtask(subtaskId: number, data: Partial<Subtask>) {
    return api.put<any, ApiResponse<Subtask>>(`/subtasks/${subtaskId}`, data)
  },
  deleteSubtask(subtaskId: number) {
    return api.delete<any, ApiResponse<void>>(`/subtasks/${subtaskId}`)
  },
  toggleSubtask(subtaskId: number) {
    return api.put<any, ApiResponse<Subtask>>(`/subtasks/${subtaskId}/toggle`)
  },
  reorderSubtasks(items: Array<{ id: number; position: number }>) {
    return api.put<any, ApiResponse<void>>('/subtasks/reorder', { items })
  },
  createComment(taskId: number, content: string) {
    return api.post<any, ApiResponse<Comment>>(`/tasks/${taskId}/comments`, { content })
  },
  listComments(taskId: number) {
    return api.get<any, ApiResponse<Comment[]>>(`/tasks/${taskId}/comments`)
  },
  updateComment(commentId: number, content: string) {
    return api.put<any, ApiResponse<Comment>>(`/comments/${commentId}`, { content })
  },
  deleteComment(commentId: number) {
    return api.delete<any, ApiResponse<void>>(`/comments/${commentId}`)
  },
  addDependency(taskId: number, data: { predecessorTaskId: number; successorTaskId: number; dependencyType?: string }) {
    return api.post<any, ApiResponse<TaskDependency>>(`/tasks/${taskId}/dependencies`, data)
  },
  removeDependency(taskId: number, predecessorId: number, successorId: number) {
    return api.delete<any, ApiResponse<void>>(`/tasks/${taskId}/dependencies`, { params: { predecessorId, successorId } })
  },
  getDependencies(taskId: number) {
    return api.get<any, ApiResponse<TaskDependency[]>>(`/tasks/${taskId}/dependencies`)
  }
}
