const api = require('../../utils/api')

Page({
  data: {
    tab: 'all',
    orders: [],
    filteredOrders: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    this.loadOrders()
  },

  async loadOrders() {
    try {
      // 加载外卖订单
      const takeoutRes = await api.getTakeoutOrdersAPI({ page: 1, size: 50 })
      const takeoutOrders = (takeoutRes.data?.records || []).map(o => ({
        ...o, type: 'takeout',
        statusText: ['待接单','已接单','配送中','已完成','已取消'][o.status] || '未知',
        amount: ((o.amount || 0) / 100).toFixed(2)
      }))
      // 加载券订单
      const voucherRes = await api.getVoucherOrdersAPI({ page: 1, size: 50 })
      const voucherOrders = (voucherRes.data?.records || []).map(o => ({
        ...o, type: 'voucher',
        statusText: ['待支付','已支付','已退款','已核销'][o.status] || '未知'
      }))
      const all = [...takeoutOrders, ...voucherOrders].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
      this.setData({ orders: all })
      this.filter()
    } catch(e) {}
  },

  filter() {
    const t = this.data.tab
    const filtered = t === 'all'
      ? this.data.orders
      : t === 'doing'
        ? this.data.orders.filter(o => o.status <= 2)
        : this.data.orders.filter(o => o.status >= 3)
    this.setData({ filteredOrders: filtered })
  },

  onTabChange(e) {
    this.setData({ tab: e.currentTarget.dataset.tab })
    this.filter()
  },

  async cancel(e) {
    const id = e.currentTarget.dataset.id
    try {
      await api.cancelOrderAPI(id)
      wx.showToast({ title: '已取消', icon: 'none' })
      this.loadOrders()
    } catch(e) {}
  },

  async remind(e) {
    const id = e.currentTarget.dataset.id
    try {
      await api.remindAPI(id)
      wx.showToast({ title: '已催单！', icon: 'none' })
    } catch(e) {}
  },

  reorder() {
    wx.showToast({ title: '已加入购物车', icon: 'none' })
  }
})
