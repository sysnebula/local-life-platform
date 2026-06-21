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
      // 按店铺分组
      const map = {}
      items.forEach(i => {
        const key = i.shopId || 0
        if (!map[key]) map[key] = { shopId: key, shopName: '', items: [] }
        map[key].items.push(i)
      })
      this.setData({ shopItems: Object.values(map) })
      this.calcTotal()
    } catch(e) {}
  },

  calcTotal() {
    let t = 0
    this.data.shopItems.forEach(s => s.items.forEach(i => t += parseInt(i.price || 0) * (i.number || 1)))
    this.setData({ total: t })
  },

  async changeQty(e) {
    const { item, delta } = e.currentTarget.dataset
    const newNum = (item.number || 1) + delta
    if (newNum <= 0) {
      try {
        await api.updateCartAPI(item.dishId ? 'dish_' + item.dishId : 'setmeal_' + item.setmealId, { number: 0 })
      } catch(e) {}
      this.loadCart()
    } else {
      try {
        await api.updateCartAPI(item.dishId ? 'dish_' + item.dishId : 'setmeal_' + item.setmealId, { number: newNum })
      } catch(e) {}
      this.loadCart()
    }
  },

  async clearCart() {
    try {
      await api.clearCartAPI()
      this.setData({ shopItems: [], total: 0 })
      wx.showToast({ title: '已清空', icon: 'none' })
    } catch(e) {}
  },

  async placeOrder() {
    if (!this.data.shopItems.length) return
    const shopId = this.data.shopItems[0].shopId
    try {
      // 获取默认地址
      const addrRes = await api.getAddressesAPI()
      const addrs = addrRes.data || []
      const defaultAddr = addrs.find(a => a.isDefault) || addrs[0]
      if (!defaultAddr) {
        wx.showToast({ title: '请先添加收货地址', icon: 'none' })
        return
      }
      await api.placeOrderAPI({ shopId, addressBookId: defaultAddr.id, remark: '' })
      wx.showToast({ title: '下单成功！', icon: 'success' })
      this.loadCart()
    } catch(e) {}
  }
})
