const api = require('../../utils/api')

Page({
  data: {
    showForm: false, editId: null,
    form: { consignee:'', phone:'', detail:'', label:'' },
    addresses: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      wx.switchTab({ url: '/pages/profile/profile' })
      return
    }
    this.loadAddresses()
  },

  async loadAddresses() {
    try {
      const res = await api.getAddressesAPI()
      this.setData({ addresses: res.data || [] })
    } catch(e) {}
  },

  editAddr(e) {
    const id = e.currentTarget.dataset.id
    if (id) {
      const addr = this.data.addresses.find(a => a.id === id)
      this.setData({ editId: id, form: { consignee: addr.consignee, phone: addr.phone, detail: addr.detail, label: addr.label || '' }, showForm: true })
    } else {
      this.setData({ editId: null, form: { consignee:'', phone:'', detail:'', label:'' }, showForm: true })
    }
  },

  hideForm() { this.setData({ showForm: false }) },

  onField(e) {
    this.setData({ ['form.' + e.currentTarget.dataset.field]: e.detail.value })
  },

  async saveAddr() {
    try {
      if (this.data.editId) {
        await api.updateAddressAPI(this.data.editId, this.data.form)
      } else {
        await api.addAddressAPI(this.data.form)
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.setData({ showForm: false })
      this.loadAddresses()
    } catch(e) {}
  },

  async setDefault(e) {
    try {
      await api.setDefaultAddressAPI(e.currentTarget.dataset.id)
      this.loadAddresses()
    } catch(e) {}
  },

  async delAddr(e) {
    try {
      await api.deleteAddressAPI(e.currentTarget.dataset.id)
      wx.showToast({ title: '已删除', icon: 'none' })
      this.loadAddresses()
    } catch(e) {}
  },

  goBack() { wx.navigateBack() }
})
