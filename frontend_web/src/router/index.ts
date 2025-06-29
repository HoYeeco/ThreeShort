import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册', requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: '个人信息' }
        },
        // 普通居民功能
        {
          path: 'credit-reports',
          name: 'CreditReports',
          component: () => import('@/views/CreditReports.vue'),
          meta: { title: '信用行为上报', roles: ['RESIDENT'] }
        },
        {
          path: 'products',
          name: 'Products',
          component: () => import('@/views/Products.vue'),
          meta: { title: '积分商城', roles: ['RESIDENT'] }
        },
        {
          path: 'exchange-records',
          name: 'ExchangeRecords',
          component: () => import('@/views/ExchangeRecords.vue'),
          meta: { title: '兑换记录', roles: ['RESIDENT'] }
        },
        {
          path: 'agreements',
          name: 'Agreements',
          component: () => import('@/views/Agreements.vue'),
          meta: { title: '居民公约学习', roles: ['RESIDENT'] }
        },
        {
          path: 'feedback',
          name: 'Feedback',
          component: () => import('@/views/Feedback.vue'),
          meta: { title: '反馈建议', roles: ['RESIDENT'] }
        },
        // 社区管理员功能
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/Users.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] }
        },
        {
          path: 'credit-profiles',
          name: 'CreditProfiles',
          component: () => import('@/views/CreditProfiles.vue'),
          meta: { title: '信用档案管理', roles: ['ADMIN'] }
        },
        {
          path: 'admin/products',
          name: 'AdminProducts',
          component: () => import('@/views/Products.vue'),
          meta: { title: '商品配置管理', roles: ['ADMIN'] }
        },
        {
          path: 'admin/agreements',
          name: 'AdminAgreements',
          component: () => import('@/views/Agreements.vue'),
          meta: { title: '公约管理', roles: ['ADMIN'] }
        },
        {
          path: 'admin/feedback',
          name: 'AdminFeedback',
          component: () => import('@/views/Feedback.vue'),
          meta: { title: '反馈处理', roles: ['ADMIN'] }
        },
        {
          path: 'admin/reports',
          name: 'AdminReports',
          component: () => import('@/views/CreditReports.vue'),
          meta: { title: '上报审核', roles: ['ADMIN'] }
        },
        // 系统维护人员功能
        {
          path: 'maintenance/logs',
          name: 'MaintenanceLogs',
          component: () => import('@/views/MaintenanceLogs.vue'),
          meta: { title: '操作日志', roles: ['MAINTAINER'] }
        },
        {
          path: 'maintenance/tasks',
          name: 'MaintenanceTasks',
          component: () => import('@/views/MaintenanceTasks.vue'),
          meta: { title: '定时任务', roles: ['MAINTAINER'] }
        },
        {
          path: 'maintenance/system',
          name: 'MaintenanceSystem',
          component: () => import('@/views/MaintenanceSystem.vue'),
          meta: { title: '系统监控', roles: ['MAINTAINER'] }
        }
      ]
    },
    {
      path: '/404',
      name: '404',
      component: () => import('@/views/404.vue'),
      meta: { title: '页面不存在', requiresAuth: false }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 社区信用管理系统` : '社区信用管理系统'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查权限
    if (to.meta.roles && !userStore.hasAnyRole(to.meta.roles as string[])) {
      // 根据用户角色重定向到合适的页面
      if (userStore.isAdmin) {
        next('/users') // 管理员默认跳转到用户管理
      } else if (userStore.isResident) {
        next('/credit-reports') // 居民默认跳转到信用上报
      } else if (userStore.isMaintainer) {
        next('/maintenance/logs') // 维护员默认跳转到操作日志
      } else {
        next('/404')
      }
      return
    }
  }
  
  // 已登录用户访问登录页面或注册页面时重定向到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router