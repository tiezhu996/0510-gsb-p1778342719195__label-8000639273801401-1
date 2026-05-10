import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/query',
        name: 'PublicQuery',
        component: () => import('@/views/PublicQuery.vue')
    },
    {
        path: '/',
        component: () => import('@/views/layout/Layout.vue'),
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '首页', requiresAuth: true }
            },
            {
                path: 'card-manage',
                name: 'CardManage',
                component: () => import('@/views/CardManage.vue'),
                meta: { title: '发卡管理', requiresAuth: true }
            },
            {
                path: 'verify-manage',
                name: 'VerifyManage',
                component: () => import('@/views/VerifyManage.vue'),
                meta: { title: '核销管理', requiresAuth: true }
            },
            {
                path: 'user-manage',
                name: 'UserManage',
                component: () => import('@/views/UserManage.vue'),
                meta: { title: '用户管理', requiresAuth: true, roles: ['admin'] }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    if (to.meta.requiresAuth && !userStore.userInfo) {
        next('/login')
    } else if (to.meta.roles && userStore.userInfo && !to.meta.roles.includes(userStore.userInfo.role)) {
        next('/dashboard')
    } else {
        next()
    }
})

export default router
