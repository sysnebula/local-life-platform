const api = require('../../utils/api')

Page({
  data: { seckills: [], shops: [] },

  onShow() {
    this.loadSeckills()
    this.loadShops()
  },

  async loadSeckills() {
    try {
      // 拉全部店铺的券，过滤出秒杀券
      const res = await api.getVoucherListAPI(1) // 以 shopId=1 为入口
      const all = res.data?.records || []
      this.setData({ seckills: all.filter(v => v.type === 1).map(v => ({
        id: v.id,
        shopId: v.shopId,
        name: v.title,
        price: (v.payValue/100).toFixed(1),
        orig: (v.actualValue/100).toFixed(0),
        sold: '...',
        stock: '...'
      })) })
    } catch(e) {}
  },

  async loadShops() {
    try {
      const res = await api.getShopListAPI({ page: 1, size: 50 })
      this.setData({ shops: (res.data?.records || []).map(s => ({
        id: s.id,
        icon: '🍲',
        name: s.name,
        area: s.area || '',
        score: s.score || '-',
        avgPrice: '¥' + ((s.avgPrice||0)/100).toFixed(0),
        distance: s.area || '-',
        meal: '',
        mealSold: ''
      })) })
    } catch(e) {}
  },

  goShop(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id })
  }
})
