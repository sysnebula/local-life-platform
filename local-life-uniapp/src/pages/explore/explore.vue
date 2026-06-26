<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const notes = ref([])
const showOrderPicker = ref(false)
const showForm = ref(false)
const orders = ref([])
const selectedOrder = ref({})
const form = ref({ title: '', content: '' })

onShow(() => { loadNotes(1) })

const loadNotes = async (shopId) => {
  try {
    const res = await api.getShopNotesAPI(shopId, { page: 1, size: 20 })
    const records = (res.data?.records || []).map(n => ({
      ...n,
      color: '#' + Math.floor(Math.random() * 16777215).toString(16),
      time: n.createTime ? n.createTime.substring(0, 10) : ''
    }))
    notes.value = records
  } catch (e) {}
}

const startWrite = async () => {
  if (!uni.getStorageSync('token')) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.switchTab({ url: '/pages/profile/profile' })
    return
  }
  const ordersList = []
  try {
    const takeoutRes = await api.getTakeoutOrdersAPI({ page: 1, size: 50 })
    const takeoutDone = (takeoutRes.data?.records || []).filter(o => o.status >= 3).map(o => ({ id: o.id, shopId: o.shopId, shopName: '', amount: ((o.amount || 0) / 100).toFixed(2), type: 'takeout' }))
    ordersList.push(...takeoutDone)
  } catch (e) {}
  try {
    const voucherRes = await api.getVoucherOrdersAPI({ page: 1, size: 50 })
    const voucherDone = (voucherRes.data?.records || []).filter(o => o.status >= 3).map(o => ({ id: o.id, shopId: o.shopId || 1, shopName: '团购券订单', amount: '-', type: 'voucher' }))
    ordersList.push(...voucherDone)
  } catch (e) {}

  if (!ordersList.length) { uni.showToast({ title: '暂无已完成订单', icon: 'none' }); return }
  orders.value = ordersList
  showOrderPicker.value = true
}

const hideOrderPicker = () => { showOrderPicker.value = false }

const selectOrder = (order) => {
  const orderType = order.type === 'takeout' ? 1 : 0
  showOrderPicker.value = false; showForm.value = true
  selectedOrder.value = { id: order.id, type: orderType, shopId: order.shopId, shopName: order.shopName }
  form.value = { title: '', content: '' }
}

const hideForm = () => { showForm.value = false }

const publish = async () => {
  try {
    await api.publishNoteAPI({
      shopId: selectedOrder.value.shopId,
      orderId: selectedOrder.value.id,
      orderType: selectedOrder.value.type,
      title: form.value.title || '探店笔记',
      content: form.value.content || '（无内容）',
      images: ''
    })
    uni.showToast({ title: '发布成功！', icon: 'success' })
    showForm.value = false
    loadNotes(1)
  } catch (e) {}
}
</script>

<template>
  <view class="page">
    <view class="header"><text class="title">📝 探店笔记</text><button class="write-btn" @click="startWrite">+ 写笔记</button></view>

    <view class="note-card" v-for="n in notes" :key="n.id">
      <view class="note-hd">
        <view class="avatar" :style="{ background: n.color || '#FF6B35' }">{{ (n.userName || '用')[0] }}</view>
        <view class="note-user"><text class="user-name">{{ n.userName }}</text><text class="shop-tag">📍 {{ n.shopName }}</text></view>
        <text class="time">{{ n.time }}</text>
      </view>
      <text class="note-title">{{ n.title }}</text>
      <text class="note-content">{{ n.content }}</text>
    </view>

    <view v-if="showOrderPicker" class="mask" @click="hideOrderPicker">
      <view class="panel" @click.stop>
        <text class="p-title">📋 选择一笔消费记录</text>
        <view v-if="!orders.length" class="empty"><text>暂无已完成订单</text></view>
        <view class="order-list" v-if="orders.length">
          <view class="order-row" v-for="o in orders" :key="o.id + o.type" @click="selectOrder(o)">
            <text class="or-icon">{{ o.type === 'takeout' ? '🛵' : '🎫' }}</text>
            <view class="or-info">
              <text class="or-shop">{{ o.shopName }}</text>
              <text class="or-detail">{{ o.type === 'takeout' ? '外卖订单' : '团购券' }} · ¥{{ o.amount }}</text>
            </view>
            <text class="or-arrow">›</text>
          </view>
        </view>
        <view class="p-actions"><button class="p-cancel" @click="hideOrderPicker">取消</button></view>
      </view>
    </view>

    <view v-if="showForm" class="mask" @click="hideForm">
      <view class="panel" @click.stop>
        <text class="p-title">✍️ 发布探店笔记</text>
        <view class="linked-order">关联: {{ selectedOrder.shopName }} · {{ selectedOrder.type === 1 ? '外卖' : '团购券' }}</view>
        <input class="p-input" placeholder="笔记标题" v-model="form.title" />
        <textarea class="p-textarea" placeholder="分享你的探店体验..." v-model="form.content" />
        <view class="p-actions"><button class="p-cancel" @click="hideForm">取消</button><button class="p-submit" @click="publish">发布</button></view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{background:#f5f5f5;min-height:100vh}
.header{display:flex;justify-content:space-between;align-items:center;padding:12px 14px;background:#fff;border-bottom:1px solid #eee}
.title{font-size:17px;font-weight:600}.write-btn{background:#FF6B35;color:#fff;border:none;border-radius:16px;padding:6px 16px;font-size:13px}
.note-card{background:#fff;margin:8px 0;padding:14px}.note-hd{display:flex;align-items:center;gap:8px;margin-bottom:8px}.avatar{width:34px;height:34px;border-radius:50%;color:#fff;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:600;flex-shrink:0}.note-user{flex:1}.user-name{display:block;font-size:13px;font-weight:500}.shop-tag{font-size:11px;color:#FF6B35}.time{font-size:11px;color:#ccc}.note-title{display:block;font-size:15px;font-weight:600;color:#222;margin-bottom:4px}.note-content{font-size:13px;color:#555;line-height:1.6}
.mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);z-index:200;display:flex;align-items:flex-end}.panel{background:#fff;width:100%;max-width:414px;margin:0 auto;border-radius:16px 16px 0 0;padding:16px}.p-title{display:block;font-size:16px;font-weight:600;margin-bottom:14px}.p-input{background:#f5f5f5;border:none;border-radius:8px;padding:10px 12px;font-size:13px;margin-bottom:10px;width:100%}.p-textarea{background:#f5f5f5;border:none;border-radius:8px;padding:10px 12px;font-size:13px;height:100px;width:100%;margin-bottom:12px}.p-actions{display:flex;gap:8px}.p-cancel{flex:1;padding:10px;border-radius:22px;border:1px solid #ddd;background:#fff;color:#666;font-size:14px}.p-submit{flex:1;padding:10px;border-radius:22px;background:#FF6B35;color:#fff;border:none;font-size:14px}
.order-list{max-height:300px;overflow-y:auto;margin-bottom:12px}.order-row{display:flex;align-items:center;gap:10px;padding:12px 8px;border-bottom:1px solid #f0f0f0}.or-icon{font-size:22px}.or-info{flex:1}.or-shop{display:block;font-size:14px;font-weight:500}.or-detail{font-size:11px;color:#999}.or-arrow{color:#ccc;font-size:18px}
.linked-order{background:#FFF3E0;border-radius:8px;padding:8px 10px;font-size:12px;color:#FF6B35;margin-bottom:10px}.empty{text-align:center;padding:30px;color:#999;font-size:14px}
</style>
