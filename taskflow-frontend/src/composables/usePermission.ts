import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useWorkspaceStore } from '@/stores/workspace'
import type { MemberRole } from '@/types'

const ROLE_HIERARCHY: Record<MemberRole, number> = {
  OWNER: 4,
  ADMIN: 3,
  MEMBER: 2,
  VIEWER: 1
}

export function usePermission() {
  const authStore = useAuthStore()
  const workspaceStore = useWorkspaceStore()

  const currentMember = computed(() => {
    if (!authStore.user) return null
    return workspaceStore.members.find(m => m.userId === authStore.user!.id) || null
  })

  const currentRole = computed<MemberRole | null>(() => {
    return currentMember.value?.role || null
  })

  function hasRole(minRole: MemberRole): boolean {
    if (!currentRole.value) return false
    return ROLE_HIERARCHY[currentRole.value] >= ROLE_HIERARCHY[minRole]
  }

  const canEdit = computed(() => hasRole('MEMBER'))
  const canAdmin = computed(() => hasRole('ADMIN'))
  const isOwner = computed(() => hasRole('OWNER'))
  const canView = computed(() => hasRole('VIEWER'))

  return { currentRole, canEdit, canAdmin, isOwner, canView, hasRole }
}
