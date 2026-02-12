import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      { path: '', name: 'Login', component: () => import('@/views/auth/LoginView.vue') }
    ],
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      { path: '', name: 'Register', component: () => import('@/views/auth/RegisterView.vue') }
    ],
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue') },
      { path: 'tasks', name: 'Tasks', component: () => import('@/views/task/TaskListView.vue') },
      { path: 'boards', name: 'Boards', component: () => import('@/views/board/BoardListView.vue') },
      { path: 'boards/:id', name: 'BoardDetail', component: () => import('@/views/board/BoardDetailView.vue') },
      { path: 'calendar', name: 'Calendar', component: () => import('@/views/calendar/CalendarView.vue') },
      { path: 'gantt', name: 'Gantt', component: () => import('@/views/gantt/GanttView.vue') },
      { path: 'workspace/settings', name: 'WorkspaceSettings', component: () => import('@/views/workspace/WorkspaceSettingsView.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfileView.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const authData = localStorage.getItem('auth')
  let isAuthenticated = false
  try {
    isAuthenticated = !!authData && !!JSON.parse(authData).accessToken
  } catch { /* ignore */ }
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !isAuthenticated) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
