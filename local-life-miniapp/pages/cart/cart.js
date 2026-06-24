const api = require('../../utils/api')

Page({
  data: {
    total: 0,
    shopItems: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    this.loadCart()
  },

  async loadCart() {
    try {
      const res = await api.getCartAPI()
      const items = res.data || []
      const map = {}
      for (const i of items) {
        const key = i.shopId || 0
        if (!map[key]) {
          let shopName = ''
          if (i.shopId) {
            try { const shopRes = await api.getShopDetailAPI(i.shopId); shopName = shopRes.data?.name || '' } catch(e) {}
          }
          map[key] = { shopId: key, shopName: shopName || '未知店铺', items: [] }
        }
        map[key].items.push(i)
      }
      // 价格 分→元 转换
      Object.values(map).forEach(s => s.items.forEach(i => { i.price = ((i.price||0)/100).toFixed(1) }))
      this.setData({ shopItems: Object.values(map) })
      this.calcTotal()
    } catch(e) {}
  },

  calcTotal() {
    let t = 0
    this.data.shopItems.forEach(s => s.items.forEach(i => t += parseFloat(i.price || 0) * (i.number || 1)))
    this.setData({ total: t.toFixed(1) })
  },

  async changeQty(e) {
    const { item, delta } = e.currentTarget.dataset
    const newNum = (item.number || 1) + delta
    const key = item.dishId ? 'dish_' + item.dishId : 'setmeal_' + item.setmealId
    try { await api.updateCartAPI(key, { number: newNum <= 0 ? 0 : newNum }); this.loadCart() } catch(e) {}
  },

  async clearCart() {
    try { await api.clearCartAPI(); this.setData({ shopItems: [], total: 0 }); wx.showToast({ title: '已清空', icon: 'none' }) } catch(e) {}
  },

  async placeOrder() {
    if (!this.data.shopItems.length) return
    const shopId = this.data.shopItems[0].shopId
    try {
      await api.placeOrderAPI({ shopId, remark: '' })
      this.setData({ shopItems: [], total: 0 })
      wx.showToast({ title: '下单成功！', icon: 'success' })
      this.loadCart()
    } catch(e) {}
  }
})
