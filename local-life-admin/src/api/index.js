import axios from 'axios'
import {ElMessage} from 'element-plus'

const http = axios.create({baseURL: '/api', timeout: 10000})

http.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

http.interceptors.response.use(
    res => {
        if (res.data.code === 0) {
            ElMessage.error(res.data.msg);
            return Promise.reject(res.data.msg)
        }
        return res.data
    },
    err => {
        if (err.response?.status === 401) { localStorage.clear(); window.location.href = '/login'; return Promise.reject(err) }
        ElMessage.error(err.response?.data?.msg || '请求失败');
        return Promise.reject(err)
    }
)

export default http

// ===== 用户 =====
export const loginAPI = (data) => http.post('/merchant/login', data)
export const registerAPI = (data) => http.post('/merchant/register', data)
export const getMeAPI = () => http.get('/merchant/me')

// ===== 店铺 =====
export const getMyShopAPI = () => http.get('/merchant/shop/my')
export const updateShopAPI = (data) => http.put('/merchant/shop', data)

// ===== 分类 =====
export const getCategoriesAPI = (params) => http.get('/merchant/takeout/category', {params})
export const addCategoryAPI = (data) => http.post('/merchant/takeout/category', data)
export const updateCategoryAPI = (data) => http.put('/merchant/takeout/category', data)
export const deleteCategoryAPI = (id) => http.delete(`/merchant/takeout/category/${id}`)

// ===== 菜品 =====
export const getDishPageAPI = (params) => http.get('/merchant/takeout/dish/page', {params})
export const addDishAPI = (data) => http.post('/merchant/takeout/dish', data)
export const updateDishAPI = (data) => http.put('/merchant/takeout/dish', data)
export const deleteDishAPI = (id) => http.delete(`/merchant/takeout/dish/${id}`)
export const toggleDishAPI = (id) => http.put(`/merchant/takeout/dish/${id}/status`)

// ===== 套餐 =====
export const getSetmealPageAPI = (params) => http.get('/merchant/takeout/setmeal/page', {params})
export const addSetmealAPI = (data) => http.post('/merchant/takeout/setmeal', data)
export const updateSetmealAPI = (data) => http.put('/merchant/takeout/setmeal', data)
export const deleteSetmealAPI = (id) => http.delete(`/merchant/takeout/setmeal/${id}`)
export const toggleSetmealAPI = (id) => http.put(`/merchant/takeout/setmeal/${id}/status`)

// ===== 优惠券 =====
export const getVoucherPageAPI = (params) => http.get('/merchant/voucher/page', {params})
export const createVoucherAPI = (data) => http.post('/merchant/voucher', data)
export const updateVoucherAPI = (id, data) => http.put(`/merchant/voucher/${id}`, data)
export const deleteVoucherAPI = (id) => http.delete(`/merchant/voucher/${id}`)
export const convertSeckillAPI = (id, data) => http.post(`/merchant/voucher/${id}/seckill`, data)

// ===== 订单 =====
export const getOrderPageAPI = (params) => http.get('/merchant/takeout/order/page', {params})
export const getOrderDetailAPI = (id) => http.get(`/merchant/takeout/order/${id}`)
export const acceptOrderAPI = (id) => http.put(`/merchant/takeout/order/${id}/accept`)
export const deliverOrderAPI = (id) => http.put(`/merchant/takeout/order/${id}/deliver`)
export const completeOrderAPI = (id) => http.put(`/merchant/takeout/order/${id}/complete`)
export const cancelOrderAPI = (id) => http.put(`/merchant/takeout/order/${id}/cancel`)
