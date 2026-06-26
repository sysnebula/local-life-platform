<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const tab = ref('all')
const orders = ref([])
const filteredOrders = ref([])

onShow(() => {
  if (!uni.getStorageSync('token')) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.switchTab({ url: '/pages/profile/profile' })
    return
  }
  loadOrders()
})

const loadOrders = async () => {
  try {
    const takeoutRes = await api.getTakeoutOrdersAPI({ page: 1, size: 50 })
    const takeoutOrders = (takeoutRes.data?.records || []).map(o => ({
      ...o, type: 'takeout',
      statusText: ['待接单', '已接单', '配送中', '已完成', '已取消'][o.status] || '未知',
      amount: ((o.amount || 0) / 100).toFixed(2)
    }))
    const voucherRes = await api.getVoucherOrdersAPI({ page: 1, size: 50 })
    const voucherOrders = (voucherRes.data?.records || []).map(o => ({
      ...o, type: 'voucher',
      shopName: o.voucherTitle || '团购券',
      statusText: ['待支付', '已支付', '已退款', '已核销'][o.status] || '未知',
      amount: ((o.payValue || 0) / 100).toFixed(2),
      faceValue: ((o.actualValue || 0) / 100).toFixed(2)
    }))
    const all = [...takeoutOrders, ...voucherOrders].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
    orders.value = all
    filter()
  } catch (e) {}
}

const filter = () => {
  const t = tab.value
  filteredOrders.value = t === 'all'
    ? orders.value
    : t === 'doing'
      ? orders.value.filter(o => o.status <= 2)
      : orders.value.filter(o => o.status >= 3)
}

const onTabChange = (t) => { tab.value = t; filter() }

const cancel = async (id) => {
  try { await api.cancelOrderAPI(id); uni.showToast({ title: '已取消', icon: 'none' }); loadOrders() } catch (e) {}
}

const remind = async (id) => {
  try { await api.remindAPI(id); uni.showToast({ title: '已催单！', icon: 'none' }) } catch (e) {}
}

const reorder = async (items) => {
  if (!items || !items.length) { uni.showToast({ title: '订单数据异常', icon: 'none' }); return }
  try {
    for (const item of items) {
      await api.addCartAPI({ dishId: item.dishId, setmealId: item.setmealId, name: item.name, price: Math.round(parseFloat(item.price) * 100), number: item.number || 1 })
    }
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (e) { uni.showToast({ title: '添加失败', icon: 'none' }) }
}
</script>

<template>
  <view class="page">
    <view class="tabs">
      <text class="tab" :class="{ active: tab === 'all' }" @click="onTabChange('all')">全部</text>
      <text class="tab" :class="{ active: tab === 'doing' }" @click="onTabChange('doing')">进行中</text>
      <text class="tab" :class="{ active: tab === 'done' }" @click="onTabChange('done')">已完成</text>
    </view>
    <view class="order-card" v-for="item in filteredOrders" :key="item.id + item.type">
      <view class="o-header"><text class="o-shop">{{ item.shopName }}</text><text class="o-type" :class="item.type">{{ item.type === 'voucher' ? '团购券' : '外卖' }}</text></view>
      <text class="o-status" :class="{ doing: item.status <= 1, done: item.status === 3, cancel: item.status === 4 }">{{ item.statusText }}</text>
      <view class="o-items" v-if="item.items && item.items.length">
        <view class="o-item" v-for="oi in item.items" :key="oi.name"><text>{{ oi.name }} ×{{ oi.number }}</text><text>¥{{ oi.price }}</text></view>
      </view>
      <view v-if="item.type==='voucher' && item.faceValue" class="o-total">
        <text>面值 ¥{{ item.faceValue }}</text>
        <text class="total-price">实付 ¥{{ item.amount }}</text>
      </view>
      <view v-else class="o-total"><text>实付：</text><text class="total-price">¥{{ item.amount }}</text></view>
      <view class="o-actions">
        <button v-if="item.status <= 1 && item.type === 'takeout'" class="btn danger" @click="cancel(item.id)">取消</button>
        <button v-if="item.status <= 1 && item.type === 'takeout'" class="btn primary" @click="remind(item.id)">催单</button>
        <button v-if="item.status === 3" class="btn" @click="reorder(item.items)">再来一单</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{background:#F8F9FA;min-height:100vh}
.tabs{display:flex;background:#fff;padding:0 16px;gap:20px;border-bottom:1px solid #f0f0f0}.tab{font-size:14px;color:#999;padding:12px 0}.tab.active{color:#E8702A;border-bottom:2px solid #E8702A;font-weight:500}
.order-card{background:#fff;margin:8px 16px 0;padding:14px;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}.o-header{display:flex;justify-content:space-between;margin-bottom:8px}.o-shop{font-size:15px;font-weight:600;color:#222}.o-type{padding:2px 8px;border-radius:4px;font-size:10px}.o-type.voucher{background:#FFF3EB;color:#E8702A}.o-type.takeout{background:#E8F5E9;color:#388E3C}
.o-status{font-size:12px}.doing{color:#FF9800}.done{color:#4CAF50}.cancel{color:#ccc}
.o-items{margin:8px 0}.o-item{display:flex;justify-content:space-between;font-size:13px;color:#555;padding:2px 0}
.o-total{display:flex;justify-content:space-between;padding-top:8px;border-top:1px solid #f0f0f0;font-size:15px;font-weight:600}.total-price{color:#E8702A}
.o-actions{display:flex;gap:8px;justify-content:flex-end;margin-top:10px}.btn{padding:5px 14px;border-radius:14px;font-size:12px;border:1px solid #ddd;background:#fff;color:#666}.btn.primary{background:#E8702A;color:#fff;border-color:#E8702A}.btn.danger{color:#F44336;border-color:#F44336}
</style>
