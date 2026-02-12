import api from './index'
import type { ApiResponse, Board, BoardColumn, BoardCreateRequest, BoardColumnCreateRequest } from '@/types'

export const boardApi = {
  create(workspaceId: number, data: BoardCreateRequest) {
    return api.post<any, ApiResponse<Board>>(`/workspaces/${workspaceId}/boards`, data)
  },
  list(workspaceId: number) {
    return api.get<any, ApiResponse<Board[]>>(`/workspaces/${workspaceId}/boards`)
  },
  get(boardId: number) {
    return api.get<any, ApiResponse<Board>>(`/boards/${boardId}`)
  },
  update(boardId: number, data: Partial<Board>) {
    return api.put<any, ApiResponse<Board>>(`/boards/${boardId}`, data)
  },
  delete(boardId: number) {
    return api.delete<any, ApiResponse<void>>(`/boards/${boardId}`)
  },
  addColumn(boardId: number, data: BoardColumnCreateRequest) {
    return api.post<any, ApiResponse<BoardColumn>>(`/boards/${boardId}/columns`, data)
  },
  updateColumn(columnId: number, data: Partial<BoardColumn>) {
    return api.put<any, ApiResponse<BoardColumn>>(`/columns/${columnId}`, data)
  },
  deleteColumn(columnId: number) {
    return api.delete<any, ApiResponse<void>>(`/columns/${columnId}`)
  },
  reorderColumns(boardId: number, items: Array<{ id: number; position: number }>) {
    return api.put<any, ApiResponse<void>>(`/boards/${boardId}/columns/reorder`, { items })
  }
}
