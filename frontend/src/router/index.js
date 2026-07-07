import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘', group: '概览' } },
      { path: 'users', name: 'Users', component: () => import('@/views/UserList.vue'), meta: { title: '客户管理', group: '业务管理' } },
      { path: 'admins', name: 'Admins', component: () => import('@/views/AdminList.vue'), meta: { title: '管理员管理', group: '系统管理' } },
      { path: 'pets', name: 'Pets', component: () => import('@/views/PetList.vue'), meta: { title: '宠物管理', group: '业务管理' } },
      { path: 'feeders', name: 'Feeders', component: () => import('@/views/FeederList.vue'), meta: { title: '喂养员管理', group: '业务管理' } },
      { path: 'feeders/:id/reviews', name: 'FeederReviews', component: () => import('@/views/ReviewList.vue'), meta: { title: '喂养员评价', group: '业务管理' } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/OrderList.vue'), meta: { title: '订单管理', group: '业务管理' } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/ReviewManage.vue'), meta: { title: '评价管理', group: '业务管理' } },
      { path: 'payments', name: 'Payments', component: () => import('@/views/PaymentList.vue'), meta: { title: '支付记录', group: '业务管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token) {
      next('/')
      return
    }
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
