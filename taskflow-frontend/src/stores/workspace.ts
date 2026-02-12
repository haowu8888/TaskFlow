import { defineStore } from 'pinia'
import { ref } from 'vue'
import { workspaceApi } from '@/api/workspace'
import type { Workspace, WorkspaceMember, WorkspaceCreateRequest, MemberInviteRequest, MemberRole } from '@/types'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const currentWorkspace = ref<Workspace | null>(null)
  const members = ref<WorkspaceMember[]>([])

  async function fetchWorkspaces() {
    const res = await workspaceApi.list()
    workspaces.value = res.data
    if (!currentWorkspace.value && workspaces.value.length > 0) {
      const savedId = localStorage.getItem('currentWorkspaceId')
      const saved = savedId ? workspaces.value.find(w => w.id === Number(savedId)) : null
      currentWorkspace.value = saved || workspaces.value[0]
    }
  }

  async function createWorkspace(data: WorkspaceCreateRequest) {
    const res = await workspaceApi.create(data)
    workspaces.value.push(res.data)
    setCurrentWorkspace(res.data)
  }

  function setCurrentWorkspace(workspace: Workspace) {
    currentWorkspace.value = workspace
    localStorage.setItem('currentWorkspaceId', String(workspace.id))
  }

  async function fetchMembers(workspaceId: number) {
    const res = await workspaceApi.getMembers(workspaceId)
    members.value = res.data
  }

  async function inviteMember(workspaceId: number, data: MemberInviteRequest) {
    await workspaceApi.inviteMember(workspaceId, data)
    await fetchMembers(workspaceId)
  }

  async function updateMemberRole(workspaceId: number, userId: number, role: MemberRole) {
    await workspaceApi.updateMemberRole(workspaceId, userId, role)
    await fetchMembers(workspaceId)
  }

  async function removeMember(workspaceId: number, userId: number) {
    await workspaceApi.removeMember(workspaceId, userId)
    await fetchMembers(workspaceId)
  }

  return {
    workspaces, currentWorkspace, members,
    fetchWorkspaces, createWorkspace, setCurrentWorkspace,
    fetchMembers, inviteMember, updateMemberRole, removeMember
  }
})
