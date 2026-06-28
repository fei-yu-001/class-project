import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue')
    },
    {
      path: '/employees',
      name: 'employees',
      component: () => import('@/views/EmployeeView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/salaries',
      name: 'salaries',
      component: () => import('@/views/SalaryView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/departments',
      name: 'departments',
      component: () => import('@/views/DepartmentView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/positions',
      name: 'positions',
      component: () => import('@/views/PositionView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/payment-methods',
      name: 'payment-methods',
      component: () => import('@/views/PaymentView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue')
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsView.vue')
    },
    {
      path: '/user-permissions',
      name: 'user-permissions',
      component: () => import('@/views/UserPermissionView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/employee-comprehensive',
      name: 'employee-comprehensive',
      component: () => import('@/views/EmployeeComprehensiveView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('@/views/ProjectView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/performance',
      name: 'performance',
      component: () => import('@/views/PerformanceView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/attendance',
      name: 'attendance',
      component: () => import('@/views/AttendanceView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/leaves',
      name: 'leaves',
      component: () => import('@/views/LeaveView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/overtime',
      name: 'overtime',
      component: () => import('@/views/OvertimeView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/position-changes',
      name: 'position-changes',
      component: () => import('@/views/PositionChangeView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/schema',
      name: 'schema',
      component: () => import('@/views/SchemaView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/sys-config',
      name: 'sys-config',
      component: () => import('@/views/SysConfigView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/audit-logs',
      name: 'audit-logs',
      component: () => import('@/views/AuditLogView.vue'),
      meta: { requireAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  auth.init()

  if (!to.meta.public && !auth.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && auth.isLoggedIn) {
    next('/dashboard')
  } else if (to.meta.requireAdmin && auth.user?.role === 'USER') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router