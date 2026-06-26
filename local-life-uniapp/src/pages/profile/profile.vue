<script setup>
import { ref } from 'vue'
import { onShow, onUnload } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const isLogin = ref(false)
const user = ref({})
const phone = ref('')
const code = ref('')
const codeSending = ref(false)
const codeBtnText = ref('获取验证码')
const logging = ref(false)
const showEditForm = ref(false)
const editNick = ref('')
const editIcon = ref('')
let codeTimer = null

onShow(() => {
  const token = uni.getStorageSync('token')
  if (token) fetchUser()
})

const sendCode = () => {
  if (!phone.value || phone.value.length < 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  uni.showToast({ title: '验证码已发送（开发环境固定123456）', icon: 'none' })
  codeSending.value = true
  let t = 60
  codeBtnText.value = t + 's'
  codeTimer = setInterval(() => {
    t--
    if (t <= 0) { clearInterval(codeTimer); codeSending.value = false; codeBtnText.value = '获取验证码' }
    else codeBtnText.value = t + 's'
  }, 1000)
}

const doLogin = async () => {
  if (!phone.value || phone.value.length < 11) { uni.showToast({ title: '请输入手机号', icon: 'none' }); return }
  if (!code.value) { uni.showToast({ title: '请输入验证码', icon: 'none' }); return }
  logging.value = true
  try {
    const res = await api.loginAPI({ phone: phone.value, code: code.value })
    uni.setStorageSync('token', res.data.token)
    isLogin.value = true; user.value = res.data; logging.value = false
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e) { logging.value = false }
}

const fetchUser = async () => {
  try {
    const res = await api.getMeAPI()
    isLogin.value = true; user.value = res.data
  } catch (e) { uni.removeStorageSync('token') }
}

const showEdit = () => { showEditForm.value = true; editNick.value = user.value.nickName || ''; editIcon.value = user.value.icon || '' }
const hideEdit = () => { showEditForm.value = false }

const doEdit = async () => {
  try {
    await api.updateMeAPI({ nickName: editNick.value, icon: editIcon.value })
    user.value.nickName = editNick.value; user.value.icon = editIcon.value
    showEditForm.value = false
    uni.showToast({ title: '已保存', icon: 'success' })
  } catch (e) {}
}

const logout = () => {
  uni.removeStorageSync('token')
  isLogin.value = false; user.value = {}
  uni.showToast({ title: '已退出', icon: 'none' })
}

const goOrders = () => uni.switchTab({ url: '/pages/orders/orders' })
const goExplore = () => uni.navigateTo({ url: '/pages/explore/explore' })

onUnload(() => { if (codeTimer) clearInterval(codeTimer) })
</script>

<template>
  <view class="page">
    <block v-if="isLogin">
      <view class="profile-hd">
        <image v-if="user.icon" class="avatar" :src="user.icon" mode="aspectFill"></image>
        <view v-else class="avatar">😊</view>
        <text class="nickname">{{ user.nickName || user.name || '用户' }}<text class="edit-link" @click="showEdit"> ✏️</text></text>
        <text class="uid" v-if="user.phone">{{ user.phone }}</text>
        <view class="stats">
          <view class="stat"><text class="num">-</text><text class="label">笔记</text></view>
          <view class="stat"><text class="num">-</text><text class="label">团购券</text></view>
          <view class="stat"><text class="num">-</text><text class="label">外卖</text></view>
        </view>
      </view>
      <view class="menu">
        <view class="menu-item" @click="goOrders"><text class="icon">📋</text><text>我的订单</text><text class="arrow">›</text></view>
        <view class="menu-item" @click="goOrders"><text class="icon">🎫</text><text>我的团购券</text><text class="arrow">›</text></view>
        <view class="menu-item" @click="goExplore"><text class="icon">📝</text><text>我的探店笔记</text><text class="arrow">›</text></view>
      </view>
      <view class="logout-wrap"><button class="logout-btn" @click="logout">退出登录</button></view>
    </block>

    <block v-else>
      <view class="login-card">
        <view class="login-logo">🏪</view>
        <text class="login-title">本地生活</text>
        <text class="login-sub">登录后享受更多优惠</text>
        <view class="login-form">
          <input class="login-input" type="number" maxlength="11" placeholder="请输入手机号" v-model="phone" />
          <view class="code-row">
            <input class="login-input code" type="number" maxlength="6" placeholder="验证码" v-model="code" />
            <button class="send-btn" @click="sendCode" :disabled="codeSending">{{ codeBtnText }}</button>
          </view>
          <button class="login-submit" @click="doLogin" :loading="logging">登录 / 注册</button>
        </view>
        <text class="login-hint">开发环境验证码固定为 123456</text>
      </view>
    </block>

    <view v-if="showEditForm" class="mask" @click="hideEdit">
      <view class="edit-panel" @click.stop>
        <text class="ep-title">✏️ 编辑资料</text>
        <text class="ep-label">昵称</text>
        <input class="ep-input" v-model="editNick" placeholder="输入昵称" />
        <text class="ep-label">头像链接</text>
        <input class="ep-input" v-model="editIcon" placeholder="粘贴头像图片URL" />
        <view class="ep-actions">
          <button class="ep-cancel" @click="hideEdit">取消</button>
          <button class="ep-save" @click="doEdit">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{min-height:100vh;background:#F8F9FA}
.profile-hd{background:#fff;padding:32px 16px 24px;text-align:center;border-bottom:1px solid #f0f0f0}
.avatar{width:72px;height:72px;border-radius:50%;background:#FFF3EB;margin:0 auto 10px;display:flex;align-items:center;justify-content:center;font-size:32px}
.nickname{display:block;font-size:17px;font-weight:600;color:#222}.uid{display:block;font-size:12px;color:#bbb;margin-top:4px}
.stats{display:flex;justify-content:center;gap:36px;margin-top:16px}.stat{text-align:center}.num{display:block;font-size:20px;font-weight:700;color:#333}.label{display:block;font-size:11px;color:#999;margin-top:3px}
.menu{background:#fff;margin:12px 16px 0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.menu-item{display:flex;align-items:center;gap:12px;padding:16px;border-bottom:1px solid #f5f5f5;font-size:15px;color:#333}.menu-item:last-child{border-bottom:none}.icon{font-size:18px}.arrow{color:#ccc;font-size:16px;margin-left:auto}
.logout-wrap{padding:20px 16px;text-align:center}
.logout-btn{width:100%;padding:12px;border-radius:24px;background:#F5F5F5;border:none;color:#999;font-size:14px}
.login-card{background:#fff;margin:48px 20px 0;padding:32px 24px;border-radius:12px;text-align:center;box-shadow:0 1px 6px rgba(0,0,0,.04)}
.login-logo{font-size:48px;margin-bottom:8px}
.login-title{display:block;font-size:20px;font-weight:700;color:#222}.login-sub{display:block;font-size:13px;color:#bbb;margin-top:4px;margin-bottom:24px}
.login-form{text-align:left}
.login-input{width:100%;background:#F5F6F8;border:none;border-radius:8px;padding:12px;font-size:14px;margin-bottom:12px;outline:none}
.code-row{display:flex;gap:8px}.code{flex:1}
.send-btn{width:110px;background:#fff;border:1px solid #E8702A;color:#E8702A;border-radius:8px;font-size:12px;padding:10px 0;white-space:nowrap;flex-shrink:0}
.login-submit{width:100%;background:#E8702A;color:#fff;border:none;border-radius:24px;padding:12px;font-size:15px;font-weight:600;margin-top:16px}
.edit-link{font-size:14px;color:#E8702A}
.mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);z-index:200;display:flex;align-items:center;justify-content:center}
.edit-panel{background:#fff;border-radius:12px;padding:20px;width:300px;margin:0 20px}
.ep-title{display:block;font-size:17px;font-weight:600;color:#222;margin-bottom:16px}
.ep-label{display:block;font-size:13px;color:#666;margin-bottom:4px}
.ep-input{background:#F5F6F8;border-radius:8px;padding:10px 12px;font-size:14px;margin-bottom:12px;width:100%}
.ep-actions{display:flex;gap:8px;margin-top:8px}.ep-cancel,.ep-save{flex:1;padding:10px;border-radius:20px;font-size:14px}.ep-cancel{border:1px solid #ddd;background:#fff;color:#666}.ep-save{background:#E8702A;color:#fff;border:none}
.login-hint{display:block;font-size:11px;color:#ccc;margin-top:12px}
</style>
