import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue') },
        { path: 'monitor', name: 'monitor', component: () => import('@/views/MonitorView.vue') },
        { path: 'stations', name: 'stations', component: () => import('@/views/StationListView.vue') },
        { path: 'stations/:id', name: 'station-detail', component: () => import('@/views/StationDetailView.vue') },
        { path: 'violations', name: 'violations', component: () => import('@/views/ViolationListView.vue') },
        { path: 'phm', name: 'phm', component: () => import('@/views/PhmMonitorView.vue') },
        { path: 'traffic', name: 'traffic', component: () => import('@/views/TrafficRiskView.vue') },
        { path: 'alerts', name: 'alerts', component: () => import('@/views/AlertCenterView.vue') },
        { path: 'members', name: 'members', component: () => import('@/views/MemberManageView.vue'), meta: { role: 'ADMIN' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return next('/login')
  if (to.meta.role && !auth.hasRole(to.meta.role as string)) return next('/dashboard')
  next()
})

export default router
