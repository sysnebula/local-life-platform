const api = require('../../utils/api')

Page({
  data: {
    isLogin: false,
    user: {},
    phone: '',
    code: '',
    codeSending: false,
    codeBtnText: '获取验证码',
    logging: false
  },

  onShow() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.fetchUser()
    }
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }) },
  onCodeInput(e) { this.setData({ code: e.detail.value }) },

  // 获取验证码（开发环境模拟）
  sendCode() {
    if (!this.data.phone || this.data.phone.length < 11) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' })
      return
    }
    wx.showToast({ title: '验证码已发送（开发环境固定123456）', icon: 'none' })
    this.setData({ codeSending: true })
    // 60秒倒计时
    let t = 60
    this.setData({ codeBtnText: t + 's' })
    const timer = setInterval(() => {
      t--
      if (t <= 0) {
        clearInterval(timer)
        this.setData({ codeSending: false, codeBtnText: '获取验证码' })
      } else {
        this.setData({ codeBtnText: t + 's' })
      }
    }, 1000)
    this._codeTimer = timer
  },

  // 登录
  async doLogin() {
    if (!this.data.phone || this.data.phone.length < 11) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    if (!this.data.code) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }
    this.setData({ logging: true })
    try {
      const res = await api.loginAPI({ phone: this.data.phone, code: this.data.code })
      wx.setStorageSync('token', res.data.token)
      this.setData({ isLogin: true, user: res.data, logging: false })
      wx.showToast({ title: '登录成功', icon: 'success' })
    } catch (e) {
      this.setData({ logging: false })
    }
  },

  // 拉取用户信息
  async fetchUser() {
    try {
      const res = await api.getMeAPI()
      this.setData({ isLogin: true, user: res.data })
    } catch (e) {
      wx.removeStorageSync('token')
    }
  },

  // 编辑资料
  showEdit() {
    this.setData({
      showEditForm: true,
      editNick: this.data.user.nickName || '',
      editIcon: this.data.user.icon || ''
    })
  },
  hideEdit() { this.setData({ showEditForm: false }) },
  onNickInput(e) { this.setData({ editNick: e.detail.value }) },
  onIconInput(e) { this.setData({ editIcon: e.detail.value }) },
  async doEdit() {
    try {
      await api.updateMeAPI({ nickName: this.data.editNick, icon: this.data.editIcon })
      const user = this.data.user
      user.nickName = this.data.editNick
      user.icon = this.data.editIcon
      this.setData({ user, showEditForm: false })
      wx.showToast({ title: '已保存', icon: 'success' })
    } catch(e) {}
  },

  // 退出
  logout() {
    wx.removeStorageSync('token')
    this.setData({ isLogin: false, user: {} })
    wx.showToast({ title: '已退出', icon: 'none' })
  },

  goOrders() { wx.switchTab({ url: '/pages/orders/orders' }) },
  goAddress() { wx.navigateTo({ url: '/pages/address/address' }) },
  goExplore() { wx.navigateTo({ url: '/pages/explore/explore' }) },

  onUnload() {
    if (this._codeTimer) { clearInterval(this._codeTimer) }
  }
})
