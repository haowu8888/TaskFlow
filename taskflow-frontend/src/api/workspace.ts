import api from './index'
import type { ApiResponse, Workspace, WorkspaceCreateRequest, WorkspaceMember, MemberInviteRequest, MemberRole } from '@/types'

export const workspaceApi = {
  create(data: WorkspaceCreateRequest) {
    return api.post<any, ApiResponse<Workspace>>('/workspaces', data)
  },
  list() {
    return api.get<any, ApiResponse<Workspace[]>>('/workspaces')
  },
  get(id: number) {
    return api.get<any, ApiResponse<Workspace>>(`/workspaces/${id}`)
  },
  update(id: number, data: Partial<Workspace>) {
    return api.put<any, ApiResponse<Workspace>>(`/workspaces/${id}`, data)
  },
  delete(id: number) {
    return api.delete<any, ApiResponse<void>>(`/workspaces/${id}`)
  },
  inviteMember(workspaceId: number, data: MemberInviteRequest) {
    return api.post<any, ApiResponse<void>>(`/workspaces/${workspaceId}/members`, data)
  },
  getMembers(workspaceId: number) {
    return api.get<any, ApiResponse<WorkspaceMember[]>>(`/workspaces/${workspaceId}/members`)
  },
  updateMemberRole(workspaceId: number, userId: number, role: MemberRole) {
    return api.put<any, ApiResponse<void>>(`/workspaces/${workspaceId}/members/${userId}/role`, { role })
  },
  removeMember(workspaceId: number, userId: number) {
    return api.delete<any, ApiResponse<void>>(`/workspaces/${workspaceId}/members/${userId}`)
  }
}
