import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {noAuth: true}
    },
    {
        path: '/',
        component: () => import('../views/Layout.vue'),
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue'),
                meta: {title: '工作台'}
            },
            {
                path: 'shop',
                name: 'ShopEdit',
                component: () => import('../views/ShopEdit.vue'),
                meta: {title: '店铺信息'}
            },
            {
                path: 'category',
                name: 'Category',
                component: () => import('../views/Category.vue'),
                meta: {title: '分类管理'}
            },
            {path: 'dish', name: 'Dish', component: () => import('../views/Dish.vue'), meta: {title: '菜品管理'}},
            {
                path: 'setmeal',
                name: 'Setmeal',
                component: () => import('../views/Setmeal.vue'),
                meta: {title: '套餐管理'}
            },
            {
                path: 'voucher',
                name: 'Voucher',
                component: () => import('../views/Voucher.vue'),
                meta: {title: '优惠券管理'}
            },
            {path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: {title: '订单处理'}}
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (!to.meta.noAuth && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
