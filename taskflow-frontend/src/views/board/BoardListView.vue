<template>
  <div class="board-list-view">
    <div class="page-header flex-between">
      <h2 class="page-title">{{ $t('board.title') }}</h2>
      <el-button type="primary" @click="showCreate = true"><el-icon><Plus /></el-icon> {{ $t('board.newBoard') }}</el-button>
    </div>
    <el-row :gutter="20" v-if="boardStore.boards.length > 0">
      <el-col :span="6" v-for="board in boardStore.boards" :key="board.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" class="board-card cursor-pointer" @click="router.push(`/boards/${board.id}`)">
          <div class="board-icon">
            <el-icon :size="32" color="#667eea"><Grid /></el-icon>
          </div>
          <h3 class="board-name">{{ board.name }}</h3>
          <p class="board-desc">{{ board.description || $t('board.noDescription') }}</p>
          <div class="board-meta">
            <span>{{ board.columns?.length || 0 }} {{ $t('board.columns') }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else :description="$t('board.noBoards')" />

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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Plus, Grid } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useBoardStore } from '@/stores/board'
import { useWorkspaceStore } from '@/stores/workspace'

const { t } = useI18n()
const router = useRouter()
const boardStore = useBoardStore()
const workspaceStore = useWorkspaceStore()
const showCreate = ref(false)
const creating = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', description: '' })
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

watch(() => workspaceStore.currentWorkspace, loadBoards)
onMounted(loadBoards)
</script>

<style lang="scss" scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; }
.board-card { text-align: center; padding: 20px; transition: transform 0.2s; &:hover { transform: translateY(-2px); } }
.board-icon { margin-bottom: 12px; }
.board-name { font-size: 16px; margin-bottom: 8px; }
.board-desc { color: #909399; font-size: 13px; margin-bottom: 12px; min-height: 36px; }
.board-meta { color: #C0C4CC; font-size: 12px; }
</style>
