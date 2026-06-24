const BASE = 'http://localhost:8080/api'

const request = (url, method = 'GET', data = {}) => {
    return new Promise((resolve, reject) => {
        wx.request({
            url: BASE + url,
            method,
            data,
            header: {
                'Authorization': 'Bearer ' + (wx.getStorageSync('token') || ''),
                'Content-Type': 'application/json'
            },
            success: (res) => {
                if (res.data.code === 0) {
                    wx.showToast({title: res.data.msg, icon: 'none'});
                    reject(res.data.msg)
                } else resolve(res.data)
            },
            fail: (err) => {
                wx.showToast({title: '网络错误', icon: 'none'});
                reject(err)
            }
        })
    })
}

module.exports = {
    loginAPI: (data) => request('/customer/user/login', 'POST', data),
    getMeAPI: () => request('/customer/user/me'),
    updateMeAPI: (data) => request('/customer/user/me', 'PUT', data),
    getShopDetailAPI: (id) => request('/customer/shop/' + id),
    getShopListAPI: (params) => request('/customer/shop/list', 'GET', params),
    getVoucherListAPI: (shopId) => request('/customer/voucher/list/' + shopId),
    buyVoucherAPI: (id) => request('/customer/voucher/' + id, 'POST'),
    seckillAPI: (id) => request('/customer/seckill/' + id, 'POST'),
    getVoucherOrdersAPI: (params) => request('/customer/voucher/orders', 'GET', params),
    getCategoriesAPI: (shopId, type) => request('/customer/takeout/category/' + shopId + '?type=' + type),
    getDishesAPI: (categoryId) => request('/customer/takeout/dish/' + categoryId),
    getSetmealsAPI: (categoryId) => request('/customer/takeout/setmeal/' + categoryId),
    getCartAPI: () => request('/customer/takeout/cart'),
    addCartAPI: (data) => request('/customer/takeout/cart', 'POST', data),
    updateCartAPI: (field, data) => request('/customer/takeout/cart/' + field, 'PUT', data),
    clearCartAPI: () => request('/customer/takeout/cart', 'DELETE'),
    placeOrderAPI: (data) => request('/customer/takeout/order', 'POST', data),
    getTakeoutOrdersAPI: (params) => request('/customer/takeout/orders', 'GET', params),
    cancelOrderAPI: (id) => request('/customer/takeout/order/' + id + '/cancel', 'PUT', {reason: ''}),
    remindAPI: (id) => request('/customer/takeout/order/' + id + '/remind', 'POST'),
    getShopNotesAPI: (shopId, params) => request('/customer/explore/shop/' + shopId, 'GET', params),
    publishNoteAPI: (data) => request('/customer/explore', 'POST', data),
    getMyNotesAPI: () => request('/customer/explore/my')
}
