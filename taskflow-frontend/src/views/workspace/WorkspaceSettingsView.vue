<template>
  <div class="workspace-settings-view">
    <h2 class="page-title">{{ $t('workspace.title') }}</h2>

    <el-row :gutter="24">
      <el-col :span="14">
        <el-card shadow="never" class="mb-16">
          <template #header><span class="card-title">{{ $t('workspace.general') }}</span></template>
          <el-form :model="wsForm" label-position="top" v-if="workspaceStore.currentWorkspace">
            <el-form-item :label="$t('workspace.workspaceName')">
              <el-input v-model="wsForm.name" />
            </el-form-item>
            <el-form-item :label="$t('workspace.description')">
              <el-input v-model="wsForm.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveWorkspace" :loading="saving" :disabled="!canAdmin">{{ $t('workspace.saveChanges') }}</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="flex-between">
              <span class="card-title">{{ $t('workspace.members') }}</span>
              <el-button v-if="canAdmin" type="primary" size="small" @click="showInvite = true">
                <el-icon><Plus /></el-icon> {{ $t('common.invite') }}
              </el-button>
            </div>
          </template>
          <el-table :data="workspaceStore.members" stripe>
            <el-table-column :label="$t('workspace.user')" min-width="180">
              <template #default="{ row }">
                <div class="member-cell">
                  <el-avatar :size="28">{{ row.username?.charAt(0)?.toUpperCase() }}</el-avatar>
                  <div>
                    <div class="member-name">{{ row.username }}</div>
                    <div class="member-email">{{ row.email }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="$t('workspace.role')" width="160">
              <template #default="{ row }">
                <el-select v-model="row.role" size="small" @change="updateRole(row.userId, row.role)" :disabled="row.role === 'OWNER' || !canAdmin">
                  <el-option :label="$t('role.owner')" value="OWNER" disabled />
                  <el-option :label="$t('role.admin')" value="ADMIN" />
                  <el-option :label="$t('role.member')" value="MEMBER" />
                  <el-option :label="$t('role.viewer')" value="VIEWER" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('workspace.joined')" width="120">
              <template #default="{ row }">
                <span class="text-secondary">{{ formatDate(row.joinedAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="" width="60">
              <template #default="{ row }">
                <el-button v-if="row.role !== 'OWNER' && canAdmin" type="danger" link size="small" @click="removeMember(row.userId)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="mb-16">
          <template #header><span class="card-title">{{ $t('workspace.workspaces') }}</span></template>
          <div class="workspace-list">
            <div
              v-for="ws in workspaceStore.workspaces"
              :key="ws.id"
              class="workspace-item"
              :class="{ active: ws.id === workspaceStore.currentWorkspace?.id }"
              @click="switchWorkspace(ws)"
            >
              <div class="ws-icon">{{ ws.name.charAt(0).toUpperCase() }}</div>
              <div class="ws-info">
                <div class="ws-name">{{ ws.name }}</div>
                <div class="ws-members">{{ ws.memberCount }} {{ $t('workspace.member') }}</div>
              </div>
            </div>
          </div>
          <el-button type="primary" link @click="showCreateWs = true" style="margin-top: 12px;">
            <el-icon><Plus /></el-icon> {{ $t('workspace.createWorkspace') }}
          </el-button>
        </el-card>

        <el-card v-if="isOwner" shadow="never">
          <template #header><span class="card-title danger-text">{{ $t('workspace.dangerZone') }}</span></template>
          <p class="text-secondary" style="margin-bottom: 12px;">{{ $t('workspace.deleteWarning') }}</p>
          <el-button type="danger" @click="deleteWorkspace">{{ $t('workspace.deleteWorkspace') }}</el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- Invite Member Dialog -->
    <el-dialog v-model="showInvite" :title="$t('workspace.inviteMember')" width="400px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item :label="$t('workspace.searchUser')">
          <el-input v-model="searchKeyword" :placeholder="$t('workspace.searchPlaceholder')" @input="searchUsers" />
        </el-form-item>
        <div class="search-results" v-if="searchResults.length > 0">
          <div v-for="user in searchResults" :key="user.id" class="search-result-item" @click="selectUser(user)">
            <el-avatar :size="28">{{ user.username.charAt(0).toUpperCase() }}</el-avatar>
            <div>
              <div>{{ user.username }}</div>
              <div class="text-secondary" style="font-size: 12px;">{{ user.email }}</div>
            </div>
          </div>
        </div>
        <el-form-item :label="$t('workspace.selectedUser')" v-if="selectedUser">
          <el-tag closable @close="selectedUser = null">{{ selectedUser.username }}</el-tag>
        </el-form-item>
        <el-form-item :label="$t('workspace.role')">
          <el-select v-model="inviteRole" style="width: 100%">
            <el-option :label="$t('role.admin')" value="ADMIN" />
            <el-option :label="$t('role.member')" value="MEMBER" />
            <el-option :label="$t('role.viewer')" value="VIEWER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInvite = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleInvite" :disabled="!selectedUser">{{ $t('common.invite') }}</el-button>
      </template>
    </el-dialog>

    <!-- Create Workspace Dialog -->
    <el-dialog v-model="showCreateWs" :title="$t('workspace.createWorkspace')" width="400px" destroy-on-close>
      <el-form :model="createWsForm" label-position="top">
        <el-form-item :label="$t('common.name')">
          <el-input v-model="createWsForm.name" :placeholder="$t('workspace.workspaceName2')" />
        </el-form-item>
        <el-form-item :label="$t('common.description')">
          <el-input v-model="createWsForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateWs = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreateWs">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { useWorkspaceStore } from '@/stores/workspace'
import { usePermission } from '@/composables/usePermission'
import { workspaceApi } from '@/api/workspace'
import { authApi } from '@/api/auth'
import type { User, Workspace, MemberRole } from '@/types'

const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const { canEdit, canAdmin, isOwner } = usePermission()
const saving = ref(false)
const showInvite = ref(false)
const showCreateWs = ref(false)
const searchKeyword = ref('')
const searchResults = ref<User[]>([])
const selectedUser = ref<User | null>(null)
const inviteRole = ref<MemberRole>('MEMBER')

const wsForm = reactive({ name: '', description: '' })
const createWsForm = reactive({ name: '', description: '' })

function loadSettings() {
  if (workspaceStore.currentWorkspace) {
    wsForm.name = workspaceStore.currentWorkspace.name
    wsForm.description = workspaceStore.currentWorkspace.description || ''
    workspaceStore.fetchMembers(workspaceStore.currentWorkspace.id)
  }
}

async function saveWorkspace() {
  if (!workspaceStore.currentWorkspace) return
  saving.value = true
  try {
    await workspaceApi.update(workspaceStore.currentWorkspace.id, { name: wsForm.name, description: wsForm.description })
    ElMessage.success(t('workspace.workspaceUpdated'))
    await workspaceStore.fetchWorkspaces()
  } catch {} finally { saving.value = false }
}

async function searchUsers() {
  if (searchKeyword.value.length < 2) { searchResults.value = []; return }
  try {
    const res = await authApi.searchUsers(searchKeyword.value)
    searchResults.value = res.data
  } catch {}
}

function selectUser(user: User) {
  selectedUser.value = user
  searchResults.value = []
  searchKeyword.value = ''
}

async function handleInvite() {
  if (!selectedUser.value || !workspaceStore.currentWorkspace) return
  try {
    await workspaceStore.inviteMember(workspaceStore.currentWorkspace.id, { userId: selectedUser.value.id, role: inviteRole.value })
    ElMessage.success(t('workspace.memberInvited'))
    showInvite.value = false
    selectedUser.value = null
  } catch {}
}

async function updateRole(userId: number, role: MemberRole) {
  if (!workspaceStore.currentWorkspace) return
  try {
    await workspaceStore.updateMemberRole(workspaceStore.currentWorkspace.id, userId, role)
    ElMessage.success(t('workspace.roleUpdated'))
  } catch {}
}

async function removeMember(userId: number) {
  if (!workspaceStore.currentWorkspace) return
  try {
    await ElMessageBox.confirm(t('workspace.removeMemberConfirm'), t('common.confirm'), { type: 'warning' })
    await workspaceStore.removeMember(workspaceStore.currentWorkspace.id, userId)
    ElMessage.success(t('workspace.memberRemoved'))
  } catch {}
}

function switchWorkspace(ws: Workspace) {
  workspaceStore.setCurrentWorkspace(ws)
}

async function handleCreateWs() {
  if (!createWsForm.name.trim()) return
  try {
    await workspaceStore.createWorkspace(createWsForm)
    ElMessage.success(t('workspace.workspaceCreated'))
    showCreateWs.value = false
    createWsForm.name = ''; createWsForm.description = ''
  } catch {}
}

async function deleteWorkspace() {
  if (!workspaceStore.currentWorkspace) return
  try {
    await ElMessageBox.confirm(t('workspace.deleteConfirm'), t('workspace.deleteWorkspace'), { type: 'error', confirmButtonText: t('workspace.deleteWorkspace') })
    await workspaceApi.delete(workspaceStore.currentWorkspace.id)
    ElMessage.success(t('workspace.workspaceDeleted'))
    await workspaceStore.fetchWorkspaces()
  } catch {}
}

function formatDate(date: string) { return dayjs(date).format('MMM DD, YYYY') }

watch(() => workspaceStore.currentWorkspace, loadSettings)
onMounted(loadSettings)
</script>

<style lang="scss" scoped>
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; color: var(--tf-text-primary); }
.card-title { font-weight: 600; color: var(--tf-text-primary); }
.danger-text { color: var(--tf-color-danger); }
.text-secondary { color: var(--tf-text-secondary); font-size: 13px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.mb-16 { margin-bottom: 16px; }

.member-cell { display: flex; align-items: center; gap: 10px; }
.member-name { font-weight: 500; font-size: 14px; color: var(--tf-text-primary); }
.member-email { color: var(--tf-text-secondary); font-size: 12px; }

.workspace-list { }
.workspace-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 12px;
  border-radius: var(--tf-radius-md); cursor: pointer; transition: all var(--tf-transition-fast);
  &:hover { background: var(--tf-bg-card-hover); }
  &.active { background: var(--tf-color-primary-light); }
}
.ws-icon {
  width: 36px; height: 36px; border-radius: var(--tf-radius-sm);
  background: var(--tf-gradient-primary);
  color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 600;
}
.ws-info { flex: 1; }
.ws-name { font-weight: 500; color: var(--tf-text-primary); }
.ws-members { color: var(--tf-text-secondary); font-size: 12px; }

.search-results { max-height: 200px; overflow-y: auto; margin-bottom: 12px; }
.search-result-item {
  display: flex; align-items: center; gap: 10px; padding: 8px; border-radius: var(--tf-radius-sm); cursor: pointer;
  transition: background var(--tf-transition-fast);
  &:hover { background: var(--tf-bg-card-hover); }
}
</style>
