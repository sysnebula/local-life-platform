const api = require('../../utils/api')

Page({
  data: {
    tab: 0,
    shopId: null,
    shopName: '',
    shop: {},
    vouchers: [],
    menu: [],
    notes: []
  },

  onLoad(options) {
    const shopId = options.id
    this.setData({ shopId })
    if (!shopId) return
    // 加载店铺数据
    this.loadShop(shopId)
    this.loadVouchers(shopId)
    this.loadMenu(shopId)
  },

  async loadShop(id) {
    try {
      const res = await api.getShopDetailAPI(id)
      if (res.data) this.setData({ shop: res.data, shopName: res.data.name })
    } catch(e) {}
  },

  async loadVouchers(shopId) {
    try {
      const res = await api.getVoucherListAPI(shopId)
      this.setData({ vouchers: res.data?.records || [] })
    } catch(e) {}
  },

  async loadMenu(shopId) {
    try {
      // 加载菜品分类及菜品
      const catRes = await api.getCategoriesAPI(shopId, 1)
      const cats = catRes.data || []
      const menu = []
      for (const c of cats) {
        const dishRes = await api.getDishesAPI(c.id)
        menu.push({ name: c.name, dishes: dishRes.data || [] })
      }
      this.setData({ menu })
    } catch(e) {}
  },

  switchTab(e) {
    this.setData({ tab: parseInt(e.currentTarget.dataset.idx) })
  },

  buyVoucher(e) {
    if (!wx.getStorageSync('token')) { wx.switchTab({ url: '/pages/profile/profile' }); return }
    const item = e.currentTarget.dataset.item
    if (!item) return
    const apiCall = item.type === 1 ? api.seckillAPI(item.id) : api.buyVoucherAPI(item.id)
    apiCall.then(() => wx.showToast({ title: '购买成功！', icon: 'success' }))
      .catch(() => wx.showToast({ title: '购买失败', icon: 'none' }))
  },

  addCart(e) {
    if (!wx.getStorageSync('token')) { wx.switchTab({ url: '/pages/profile/profile' }); return }
    const item = e.currentTarget.dataset.item
    if (!item) return
    api.addCartAPI({ dishId: item.id, name: item.name, price: item.price, number: 1 })
      .then(() => wx.showToast({ title: '已加入购物车', icon: 'success' }))
      .catch(() => wx.showToast({ title: '添加失败', icon: 'none' }))
  }
})
