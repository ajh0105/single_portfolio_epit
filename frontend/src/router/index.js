import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard',
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/stations',
    name: 'Stations',
    component: () => import('@/views/StationListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/stations/:id',
    name: 'StationDetail',
    component: () => import('@/views/StationDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/violations',
    name: 'Violations',
    component: () => import('@/views/ViolationsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/equipment',
    name: 'Equipment',
    component: () => import('@/views/EquipmentView.vue'),
    meta: { requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'Login' && auth.isAuthenticated) {
    return { name: 'Dashboard' }
  }
})

export default router
