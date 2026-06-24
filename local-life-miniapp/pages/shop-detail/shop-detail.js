const api = require('../../utils/api')

Page({
  data: {
    tab: 0,
    shopId: null,
    shopName: '',
    shop: {},
    vouchers: [],
    menu: [],
    notes: [],
    cartCount: 0,
    cartTotal: 0,
    cartItems: [],
    showCart: false
  },

  onShow() {
    this.setData({ cartCount: 0, cartTotal: 0, cartItems: [], showCart: false })
    this.loadCartSummary()
  },

  onLoad(options) {
    const shopId = options.id
    const tab = options.tab ? parseInt(options.tab) : 0
    this.setData({ shopId, tab })
    if (!shopId) return
    this.loadShop(shopId)
    this.loadVouchers(shopId)
    this.loadMenu(shopId)
    if (tab === 1) this.loadCartSummary()
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
      const vouchers = (res.data?.records || []).map(v => ({ ...v, payValue: ((v.payValue||0)/100).toFixed(1) }))
      this.setData({ vouchers })
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
        const dishes = (dishRes.data || []).map(d => ({ ...d, price: ((d.price||0)/100).toFixed(1) }))
        menu.push({ name: c.name, dishes })
      }
      this.setData({ menu })
    } catch(e) {}
  },

  switchTab(e) {
    const tab = parseInt(e.currentTarget.dataset.idx)
    this.setData({ tab })
    if (tab === 1) this.loadCartSummary()
  },

  async showCartPopup() {
    await this.loadCartSummary()
    this.loadCartDetail()
  },

  hideCartPopup() {
    this.setData({ showCart: false })
  },

  async loadCartSummary() {
    try {
      const res = await api.getCartAPI()
      const items = res.data || []
      let count = 0, total = 0
      items.forEach(i => { count += i.number || 1; total += (i.price || 0) * (i.number || 1) })
      this.setData({ cartCount: count, cartTotal: (total/100).toFixed(0) })
    } catch(e) {}
  },

  async loadCartDetail() {
    try {
      const res = await api.getCartAPI()
      const items = (res.data || []).map(i => ({ ...i, showPrice: ((i.price||0)/100).toFixed(1) }))
      let total = 0
      items.forEach(i => { total += (i.price || 0) * (i.number || 1) })
      this.setData({ cartItems: items, cartCount: items.length, cartTotal: (total/100).toFixed(0), showCart: true })
    } catch(e) {}
  },

  async cartPlus(e) {
    const id = e.currentTarget.dataset.id
    try { await api.updateCartAPI('dish_' + id, { number: 1 }); this.loadCartDetail() } catch(e) {}
  },

  async cartMinus(e) {
    const id = e.currentTarget.dataset.id
    try { await api.updateCartAPI('dish_' + id, { number: 0 }); this.loadCartDetail() } catch(e) {}
  },

  async clearCart() {
    try { await api.clearCartAPI(); this.setData({ showCart: false, cartCount: 0, cartTotal: 0 }) } catch(e) {}
  },

  goCart() {
    wx.navigateTo({ url: '/pages/cart/cart' })
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
    api.addCartAPI({ shopId: this.data.shopId, dishId: item.id, name: item.name, price: Math.round(parseFloat(item.price)*100), number: 1 })
      .then(() => { wx.showToast({ title: '已加入购物车', icon: 'success' }); this.loadCartSummary() })
      .catch(() => wx.showToast({ title: '添加失败', icon: 'none' }))
  }
})
