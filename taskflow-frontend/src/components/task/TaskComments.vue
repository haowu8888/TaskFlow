<template>
  <div class="task-comments">
    <div v-for="comment in comments" :key="comment.id" class="comment-item">
      <el-avatar :size="28">{{ comment.user?.username?.charAt(0)?.toUpperCase() }}</el-avatar>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.user?.username }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
      </div>
      <el-button type="danger" link size="small" @click="deleteComment(comment.id)">
        <el-icon><Delete /></el-icon>
      </el-button>
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
import { Delete } from '@element-plus/icons-vue'
import { taskApi } from '@/api/task'
import type { Comment } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const { t } = useI18n()

const props = defineProps<{ taskId: number }>()
const comments = ref<Comment[]>([])
const newComment = ref('')

onMounted(async () => {
  try {
    const res = await taskApi.listComments(props.taskId)
    comments.value = res.data
  } catch {
    // Error handled by API interceptor
  }
})

async function addComment() {
  if (!newComment.value.trim()) return
  try {
    const res = await taskApi.createComment(props.taskId, newComment.value)
    comments.value.push(res.data)
    newComment.value = ''
  } catch {
    // Error handled by API interceptor
  }
}

async function deleteComment(id: number) {
  try {
    await taskApi.deleteComment(id)
    comments.value = comments.value.filter(c => c.id !== id)
  } catch {
    // Error handled by API interceptor
  }
}

function formatTime(time: string) {
  return dayjs(time).fromNow()
}
</script>

<style lang="scss" scoped>
.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
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
}

.comment-time {
  color: #C0C4CC;
  font-size: 12px;
}

.comment-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.add-comment {
  margin-top: 12px;
}
</style>
