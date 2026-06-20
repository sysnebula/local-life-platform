App({
    globalData: {
        baseUrl: 'http://localhost:8080/api',
        token: ''
    },
    onLaunch() {
        const token = wx.getStorageSync('token')
        if (token) this.globalData.token = token
        // 小程序启动时不校验合法域名
        console.log('本地生活小程序启动')
    }
})
