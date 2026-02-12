// User types
export interface User {
  id: number
  username: string
  email: string
  avatarUrl?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

// Task types
export type TaskPriority = 'URGENT' | 'HIGH' | 'MEDIUM' | 'LOW'
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE' | 'CANCELLED'
export type MemberRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
export type NotificationType = 'TASK_ASSIGNED' | 'TASK_UPDATED' | 'COMMENT_ADDED' | 'DUE_DATE_REMINDER' | 'MEMBER_INVITED'

export interface Task {
  id: number
  workspaceId: number
  boardColumnId?: number
  title: string
  description?: string
  priority: TaskPriority
  status: TaskStatus
  startDate?: string
  dueDate?: string
  progress: number
  assigneeId?: number
  creatorId: number
  parentTaskId?: number
  position: number
  assignee?: User
  creator?: User
  subtasks?: Subtask[]
  commentCount?: number
  labels?: Label[]
  createdAt: string
  updatedAt: string
}

export interface Subtask {
  id: number
  taskId: number
  title: string
  isCompleted: boolean
  position: number
}

export interface Comment {
  id: number
  taskId: number
  content: string
  user: User
  createdAt: string
}

export interface Board {
  id: number
  workspaceId: number
  name: string
  description?: string
  columns?: BoardColumn[]
  createdAt: string
}

export interface BoardColumn {
  id: number
  boardId: number
  name: string
  color: string
  position: number
  wipLimit: number
  tasks?: Task[]
}

export interface Workspace {
  id: number
  name: string
  description?: string
  owner?: User
  memberCount: number
  createdAt: string
}

export interface WorkspaceMember {
  id: number
  userId: number
  username: string
  email: string
  avatarUrl?: string
  role: MemberRole
  joinedAt: string
}

export interface Notification {
  id: number
  type: NotificationType
  title: string
  content?: string
  referenceId?: number
  isRead: boolean
  createdAt: string
}

export interface TaskDependency {
  id: number
  predecessorTaskId: number
  successorTaskId: number
  dependencyType: string
}

export interface Label {
  id: number
  workspaceId: number
  name: string
  color: string
  createdAt: string
}

export interface TaskActivity {
  id: number
  taskId: number
  action: string
  fieldName?: string
  oldValue?: string
  newValue?: string
  createdAt: string
  user: User
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface TaskCreateRequest {
  title: string
  description?: string
  priority?: TaskPriority
  status?: TaskStatus
  startDate?: string
  dueDate?: string
  assigneeId?: number
  parentTaskId?: number
  boardColumnId?: number
}

export interface TaskUpdateRequest {
  title?: string
  description?: string
  priority?: TaskPriority
  status?: TaskStatus
  startDate?: string
  dueDate?: string
  assigneeId?: number | null
  boardColumnId?: number
  progress?: number
}

export interface TaskMoveRequest {
  boardColumnId: number
  position: number
}

export interface SubtaskCreateRequest {
  title: string
}

export interface BoardCreateRequest {
  name: string
  description?: string
}

export interface BoardColumnCreateRequest {
  name: string
  color?: string
  wipLimit?: number
}

export interface WorkspaceCreateRequest {
  name: string
  description?: string
}

export interface MemberInviteRequest {
  userId: number
  role: MemberRole
}

export interface TaskFilterParams {
  status?: TaskStatus
  priority?: TaskPriority
  assigneeId?: number
  keyword?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}
