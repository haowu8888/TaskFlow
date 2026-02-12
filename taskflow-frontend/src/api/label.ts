import api from './index'
import type { ApiResponse, Label } from '@/types'

export const labelApi = {
  list(workspaceId: number) {
    return api.get<any, ApiResponse<Label[]>>(`/workspaces/${workspaceId}/labels`)
  },
  create(workspaceId: number, data: { name: string; color: string }) {
    return api.post<any, ApiResponse<Label>>(`/workspaces/${workspaceId}/labels`, data)
  },
  update(labelId: number, data: { name?: string; color?: string }) {
    return api.put<any, ApiResponse<Label>>(`/labels/${labelId}`, data)
  },
  delete(labelId: number) {
    return api.delete<any, ApiResponse<void>>(`/labels/${labelId}`)
  },
  getTaskLabels(taskId: number) {
    return api.get<any, ApiResponse<Label[]>>(`/tasks/${taskId}/labels`)
  },
  addToTask(taskId: number, labelId: number) {
    return api.post<any, ApiResponse<void>>(`/tasks/${taskId}/labels/${labelId}`)
  },
  removeFromTask(taskId: number, labelId: number) {
    return api.delete<any, ApiResponse<void>>(`/tasks/${taskId}/labels/${labelId}`)
  }
}
