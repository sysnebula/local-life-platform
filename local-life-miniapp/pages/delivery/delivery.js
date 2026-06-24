const api = require('../../utils/api')

Page({
  data: { shops: [] },

  onShow() { this.loadShops() },

  async loadShops() {
    try {
      const res = await api.getShopListAPI({ page: 1, size: 50 })
      this.setData({ shops: (res.data?.records || []).map(s => ({
        id: s.id,
        icon: '🍲',
        name: s.name,
        area: s.area || '',
        score: s.score || '-',
        monthSold: (s.sold || 0) > 1000 ? Math.floor(s.sold/1000)+'k' : (s.sold || '-'),
        minOrder: '¥' + ((s.minOrder||0)/100).toFixed(0),
        deliveryFee: '¥' + ((s.deliveryFee||0)/100).toFixed(0),
        time: '30分钟'
      })) })
    } catch(e) {}
  },

  goShop(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id + '&tab=1' })
  }
})
