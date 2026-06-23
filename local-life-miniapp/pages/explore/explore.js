const api = require('../../utils/api')

Page({
  data: {
    notes: [],
    showOrderPicker: false,
    showForm: false,
    orders: [],
    selectedOrder: {},
    form: { title: '', content: '' }
  },

  onShow() {
    this.loadNotes(1)
  },

  async loadNotes(shopId) {
    try {
      const res = await api.getShopNotesAPI(shopId, { page: 1, size: 20 })
      const records = (res.data?.records || []).map(n => ({
        ...n,
        color: '#' + Math.floor(Math.random()*16777215).toString(16),
        time: n.createTime ? n.createTime.substring(0, 10) : ''
      }))
      this.setData({ notes: records })
    } catch(e) {}
  },

  // 步骤1: 检查登录 → 拉已完成订单
  async startWrite() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    // 拉外卖已完成订单
    const orders = []
    try {
      const takeoutRes = await api.getTakeoutOrdersAPI({ page: 1, size: 50 })
      const takeoutDone = (takeoutRes.data?.records || [])
        .filter(o => o.status >= 3)
        .map(o => ({ id: o.id, shopId: o.shopId, shopName: '', amount: ((o.amount||0)/100).toFixed(2), type: 'takeout' }))
      orders.push(...takeoutDone)
    } catch(e) {}
    // 拉券已核销订单
    try {
      const voucherRes = await api.getVoucherOrdersAPI({ page: 1, size: 50 })
      const voucherDone = (voucherRes.data?.records || [])
        .filter(o => o.status >= 3)
        .map(o => ({ id: o.id, shopId: o.shopId || 1, shopName: '团购券订单', amount: '-', type: 'voucher' }))
      orders.push(...voucherDone)
    } catch(e) {}

    if (!orders.length) {
      wx.showToast({ title: '暂无已完成订单', icon: 'none' })
      return
    }
    this.setData({ orders, showOrderPicker: true })
  },

  hideOrderPicker() { this.setData({ showOrderPicker: false }) },

  // 步骤2: 选好订单 → 打开写笔记表单
  selectOrder(e) {
    const { id, type, shop, shopName } = e.currentTarget.dataset
    this.setData({
      showOrderPicker: false,
      showForm: true,
      selectedOrder: { id, type, shopId: shop, shopName },
      form: { title: '', content: '' }
    })
  },

  hideForm() { this.setData({ showForm: false }) },
  onTitle(e) { this.setData({ 'form.title': e.detail.value }) },
  onContent(e) { this.setData({ 'form.content': e.detail.value }) },

  async publish() {
    try {
      await api.publishNoteAPI({
        shopId: this.data.selectedOrder.shopId,
        orderId: this.data.selectedOrder.id,
        orderType: Number(this.data.selectedOrder.type),
        title: this.data.form.title || '探店笔记',
        content: this.data.form.content || '（无内容）',
        images: ''
      })
      wx.showToast({ title: '发布成功！', icon: 'success' })
      this.setData({ showForm: false })
      this.loadNotes(1)
    } catch(e) {}
  }
})
