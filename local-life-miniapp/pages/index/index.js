const api = require('../../utils/api')

Page({
  data: {
    seckills: [],
    notes: []
  },

  onShow() {
    this.loadSeckills()
    this.loadNotes()
  },

  async loadSeckills() {
    try {
      const res = await api.getVoucherListAPI(1)
      const records = res.data?.records || []
      const seckillRecords = records.filter(v => v.type === 1)
      if (seckillRecords.length) {
        this.setData({ seckills: seckillRecords.map(v => ({
          id: v.id, shopId: v.shopId, shopName: v.title,
          title: v.title, price: (v.payValue/100).toFixed(1),
          orig: (v.actualValue/100).toFixed(0),
          sold: '...', stock: '...'
        })) })
      }
    } catch(e) {}
  },

  async loadNotes() {
    try {
      const res = await api.getShopNotesAPI(1, { page: 1, size: 5 })
      const records = res.data?.records || []
      this.setData({ notes: records.map(n => ({
        id: n.id,
        avatar: (n.userName || '用')[0],
        userName: n.userName || '用户',
        shopName: '',
        time: (n.createTime || '').substring(0, 10),
        title: n.title,
        content: n.content
      })) })
    } catch(e) {}
  },

  goDelivery() { wx.navigateTo({ url: '/pages/delivery/delivery' }) },
  goDinein() { wx.navigateTo({ url: '/pages/dinein/dinein' }) },
  goAllSeckill() { wx.navigateTo({ url: '/pages/dinein/dinein' }) },

  goSeckill(e) {
    const shopId = e.currentTarget.dataset.shopid
    if (shopId) wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + shopId })
  },

  goExplore() { wx.navigateTo({ url: '/pages/explore/explore' }) },

  goWriteNote() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    wx.navigateTo({ url: '/pages/explore/explore' })
  },

  goNote(e) {
    wx.navigateTo({ url: '/pages/explore/explore?id=' + e.currentTarget.dataset.id })
  },

  goShop(e) {
    wx.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + e.currentTarget.dataset.id })
  }
})
