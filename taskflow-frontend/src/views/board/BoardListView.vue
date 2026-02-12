<template>
  <div class="board-list-view">
    <div class="page-header flex-between">
      <h2 class="page-title">{{ $t('board.title') }}</h2>
      <el-button v-if="canEdit" type="primary" @click="showCreate = true"><el-icon><Plus /></el-icon> {{ $t('board.newBoard') }}</el-button>
    </div>
    <el-row :gutter="20" v-if="boardStore.boards.length > 0">
      <el-col :span="6" v-for="board in boardStore.boards" :key="board.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" class="board-card cursor-pointer" @click="router.push(`/boards/${board.id}`)">
          <div class="board-color-bar">
            <span
              v-for="col in (board.columns || []).slice(0, 6)"
              :key="col.id"
              class="color-segment"
              :style="{ background: col.color || '#165DFF' }"
            ></span>
          </div>
          <div v-if="canAdmin" class="board-actions" @click.stop>
            <el-dropdown trigger="click" @command="(cmd: string) => handleBoardCommand(cmd, board)">
              <el-button link size="small" class="board-more-btn">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="board-icon">
            <el-icon :size="32" color="#667eea"><Grid /></el-icon>
          </div>
          <h3 class="board-name">{{ board.name }}</h3>
          <p class="board-desc">{{ board.description || $t('board.noDescription') }}</p>
          <div class="board-meta">
            <el-tag size="small" type="info">{{ board.columns?.length || 0 }} {{ $t('board.columns') }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else :description="$t('board.noBoards')" />

    <!-- Create Board Dialog -->
    <el-dialog v-model="showCreate" :title="$t('board.createBoard')" width="400px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="$t('common.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('board.boardName')" />
        </el-form-item>
        <el-form-item :label="$t('common.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="$t('board.boardDescription')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <!-- Edit Board Dialog -->
    <el-dialog v-model="showEdit" :title="$t('board.editBoard')" width="400px" destroy-on-close>
      <el-form :model="editForm" label-position="top">
        <el-form-item :label="$t('common.name')">
          <el-input v-model="editForm.name" :placeholder="$t('board.boardName')" />
        </el-form-item>
        <el-form-item :label="$t('common.description')">
          <el-input v-model="editForm.description" type="textarea" :rows="2" :placeholder="$t('board.boardDescription')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editing">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Plus, Grid, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useBoardStore } from '@/stores/board'
import { useWorkspaceStore } from '@/stores/workspace'
import { usePermission } from '@/composables/usePermission'
import { boardApi } from '@/api/board'
import type { Board } from '@/types'

const { t } = useI18n()
const router = useRouter()
const boardStore = useBoardStore()
const workspaceStore = useWorkspaceStore()
const { canEdit, canAdmin } = usePermission()
const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const editing = ref(false)
const editingBoardId = ref(0)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', description: '' })
const editForm = reactive({ name: '', description: '' })
const rules = { name: [{ required: true, message: () => t('board.nameRequired'), trigger: 'blur' }] }

async function loadBoards() {
  if (workspaceStore.currentWorkspace) {
    await boardStore.fetchBoards(workspaceStore.currentWorkspace.id)
  }
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || !workspaceStore.currentWorkspace) return
  creating.value = true
  try {
    await boardStore.createBoard(workspaceStore.currentWorkspace.id, form)
    ElMessage.success(t('board.boardCreated'))
    showCreate.value = false
    form.name = ''; form.description = ''
  } catch {} finally { creating.value = false }
}

function handleBoardCommand(command: string, board: Board) {
  if (command === 'edit') {
    editingBoardId.value = board.id
    editForm.name = board.name
    editForm.description = board.description || ''
    showEdit.value = true
  } else if (command === 'delete') {
    handleDeleteBoard(board)
  }
}

async function handleEdit() {
  if (!editForm.name.trim()) return
  editing.value = true
  try {
    await boardApi.update(editingBoardId.value, { name: editForm.name, description: editForm.description })
    ElMessage.success(t('board.boardUpdated'))
    showEdit.value = false
    await loadBoards()
  } catch {} finally { editing.value = false }
}

async function handleDeleteBoard(board: Board) {
  try {
    await ElMessageBox.confirm(
      t('board.deleteBoardConfirm'),
      t('common.confirm'),
      { type: 'warning', confirmButtonText: t('common.delete') }
    )
    await boardApi.delete(board.id)
    ElMessage.success(t('board.boardDeleted'))
    await loadBoards()
  } catch {}
}

watch(() => workspaceStore.currentWorkspace, loadBoards)
onMounted(loadBoards)
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: var(--tf-text-primary); }
.board-card {
  text-align: center; padding: 24px 20px; position: relative;
  transition: all var(--tf-transition-normal);
  &:hover { transform: translateY(-4px); box-shadow: var(--tf-shadow-hover); }
  &:hover .board-actions { opacity: 1; }
}
.board-actions {
  position: absolute; top: 12px; right: 12px;
  opacity: 0; transition: opacity var(--tf-transition-fast);
}
.board-more-btn {
  padding: 4px; border-radius: var(--tf-radius-sm);
  &:hover { background: var(--tf-bg-card-hover); }
}
.board-color-bar {
  display: flex; height: 4px; border-radius: 2px; overflow: hidden; margin-bottom: 16px; gap: 2px;
}
.color-segment { flex: 1; min-width: 8px; border-radius: 1px; }
.board-icon {
  margin-bottom: 12px;
  :deep(.el-icon) { color: var(--tf-color-primary); }
}
.board-name { font-size: 16px; font-weight: 600; margin-bottom: 8px; color: var(--tf-text-primary); }
.board-desc { color: var(--tf-text-secondary); font-size: 13px; margin-bottom: 12px; min-height: 36px; line-height: 1.5; }
.board-meta { display: flex; justify-content: center; gap: 8px; }
</style>
