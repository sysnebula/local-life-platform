const api = require('../../utils/api')

Page({
    data: {user: {}},
    login() {
        api.loginAPI({phone: '13800001111', code: '123456'}).then(res => {
            wx.setStorageSync('token', res.data.token)
            this.setData({user: res.data})
            wx.showToast({title: '登录成功', icon: 'success'})
        })
    },
    goOrders() {
        wx.switchTab({url: '/pages/orders/orders'})
    },
    goAddress() {
        wx.navigateTo({url: '/pages/address/address'})
    }
})
