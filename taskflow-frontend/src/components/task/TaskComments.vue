<template>
  <div class="task-comments">
    <div v-for="comment in comments" :key="comment.id" class="comment-item">
      <el-avatar :size="28">{{ comment.user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.user?.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div v-if="editingId === comment.id" class="comment-edit">
          <el-input v-model="editContent" type="textarea" :rows="2" />
          <div class="comment-edit-actions">
            <el-button size="small" @click="cancelEdit">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" size="small" @click="saveEdit(comment.id)" :disabled="!editContent.trim()">{{ $t('common.save') }}</el-button>
          </div>
        </div>
        <div v-else class="comment-content">{{ comment.content }}</div>
      </div>
      <div v-if="isOwnComment(comment)" class="comment-actions">
        <el-button v-if="editingId !== comment.id" type="primary" link size="small" @click="startEdit(comment)">
          <el-icon><Edit /></el-icon>
        </el-button>
        <el-button type="danger" link size="small" @click="deleteComment(comment.id)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
    <div class="add-comment">
      <el-input v-model="newComment" :placeholder="$t('task.addComment')" :rows="2" type="textarea" />
      <el-button type="primary" size="small" @click="addComment" :disabled="!newComment.trim()" style="margin-top: 8px;">
        {{ $t('common.send') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { taskApi } from '@/api/task'
import { useAuthStore } from '@/stores/auth'
import type { Comment } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const { t } = useI18n()
const authStore = useAuthStore()

const props = defineProps<{ taskId: number }>()
const comments = ref<Comment[]>([])
const newComment = ref('')
const editingId = ref<number | null>(null)
const editContent = ref('')

function isOwnComment(comment: Comment): boolean {
  return comment.user?.id === authStore.user?.id
}

function startEdit(comment: Comment) {
  editingId.value = comment.id
  editContent.value = comment.content
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function saveEdit(commentId: number) {
  if (!editContent.value.trim()) return
  try {
    const res = await taskApi.updateComment(commentId, editContent.value)
    const idx = comments.value.findIndex(c => c.id === commentId)
    if (idx !== -1) {
      comments.value[idx] = { ...comments.value[idx], content: res.data.content }
    }
    editingId.value = null
    editContent.value = ''
    ElMessage.success(t('task.commentUpdated'))
  } catch {}
}

onMounted(async () => {
  try {
    const res = await taskApi.listComments(props.taskId)
    comments.value = res.data
  } catch {}
})

async function addComment() {
  if (!newComment.value.trim()) return
  try {
    const res = await taskApi.createComment(props.taskId, newComment.value)
    comments.value.push(res.data)
    newComment.value = ''
  } catch {}
}

async function deleteComment(id: number) {
  try {
    await taskApi.deleteComment(id)
    comments.value = comments.value.filter(c => c.id !== id)
  } catch {}
}

function formatTime(time: string) {
  return dayjs(time).fromNow()
}
</script>

<style lang="scss" scoped>
.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid var(--tf-border-color-lighter);
  align-items: flex-start;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}

.comment-author {
  font-weight: 500;
  font-size: 13px;
  color: var(--tf-text-primary);
}

.comment-time {
  color: var(--tf-text-placeholder);
  font-size: 12px;
}

.comment-content {
  font-size: 13px;
  color: var(--tf-text-regular);
  line-height: 1.5;
}

.comment-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.comment-edit {
  margin-top: 4px;
}

.comment-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}

.add-comment {
  margin-top: 12px;
}
</style>
