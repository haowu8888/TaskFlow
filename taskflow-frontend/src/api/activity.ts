import api from './index'
import type { ApiResponse, TaskActivity } from '@/types'

export const activityApi = {
  getTaskActivities(taskId: number) {
    return api.get<any, ApiResponse<TaskActivity[]>>(`/tasks/${taskId}/activities`)
  }
}
