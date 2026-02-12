import { defineStore } from 'pinia'
import { ref } from 'vue'
import { boardApi } from '@/api/board'
import type { Board, BoardCreateRequest, BoardColumnCreateRequest } from '@/types'

export const useBoardStore = defineStore('board', () => {
  const boards = ref<Board[]>([])
  const currentBoard = ref<Board | null>(null)
  const loading = ref(false)

  async function fetchBoards(workspaceId: number) {
    const res = await boardApi.list(workspaceId)
    boards.value = res.data
  }

  async function fetchBoard(boardId: number) {
    loading.value = true
    try {
      const res = await boardApi.get(boardId)
      currentBoard.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function createBoard(workspaceId: number, data: BoardCreateRequest) {
    const res = await boardApi.create(workspaceId, data)
    boards.value.push(res.data)
    return res.data
  }

  async function addColumn(boardId: number, data: BoardColumnCreateRequest) {
    const res = await boardApi.addColumn(boardId, data)
    if (currentBoard.value && currentBoard.value.id === boardId) {
      currentBoard.value.columns = currentBoard.value.columns || []
      currentBoard.value.columns.push(res.data)
    }
    return res.data
  }

  return { boards, currentBoard, loading, fetchBoards, fetchBoard, createBoard, addColumn }
})
