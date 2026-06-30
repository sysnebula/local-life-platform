<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const total = ref(0)
const shopItems = ref([])
const shopInfo = ref({ deliveryFee: 0, minOrder: 0, name: '' })
const addresses = ref([])
const selectedAddress = ref(null)
const showPay = ref(false)
const currentOrder = ref(null)

onShow(() => {
  if (!uni.getStorageSync('token')) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.switchTab({ url: '/pages/profile/profile' })
    return
  }
  loadCart()
})

const loadCart = async () => {
  try {
    const res = await api.getCartAPI()
    const items = res.data || []
    const map = {}
    for (const i of items) {
      const key = i.shopId || 0
      if (!map[key]) {
        let shopName = '', deliveryFee = 0, minOrder = 0
        if (i.shopId) {
          try {
            const shopRes = await api.getShopDetailAPI(i.shopId)
            shopName = shopRes.data?.name || ''
            deliveryFee = shopRes.data?.deliveryFee || 0
            minOrder = shopRes.data?.minOrder || 0
          } catch (e) {}
        }
        map[key] = { shopId: key, shopName: shopName || '未知店铺', deliveryFee, minOrder, items: [] }
      }
      map[key].items.push(i)
    }
    Object.values(map).forEach(s => s.items.forEach(i => { i.price = ((i.price || 0) / 100).toFixed(1) }))
    shopItems.value = Object.values(map)
    if (shopItems.value.length > 0) {
      shopInfo.value = shopItems.value[0]
    }
  } catch (e) {}
  calcTotal()
}

const calcTotal = () => {
  let t = 0
  shopItems.value.forEach(s => s.items.forEach(i => t += parseFloat(i.price || 0) * (i.number || 1)))
  if (shopItems.value.length > 0) {
    t += (shopInfo.value.deliveryFee || 0) / 100
  }
  total.value = t.toFixed(1)
}

const changeQty = async (item, delta) => {
  const newNum = (item.number || 1) + delta
  const key = item.dishId ? 'dish_' + item.dishId : 'setmeal_' + item.setmealId
  try { await api.updateCartAPI(key, { number: newNum <= 0 ? 0 : newNum }); loadCart() } catch (e) {}
}

const clearCart = async () => {
  try { await api.clearCartAPI(); shopItems.value = []; total.value = 0; uni.showToast({ title: '已清空', icon: 'none' }) } catch (e) {}
}

const placeOrder = async () => {
  if (!shopItems.value.length) return
  const shopId = shopItems.value[0].shopId
  if (!shopId) {
    uni.showToast({ title: '店铺信息异常', icon: 'none' })
    return
  }
  // 先加载地址列表让用户选择
  try {
    const addrRes = await api.getAddressListAPI()
    addresses.value = addrRes.data || []
  } catch (e) { addresses.value = [] }
  if (!addresses.value.length) {
    uni.showModal({
      title: '需要收货地址', content: '请先添加收货地址', success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/address/address' })
      }
    })
    return
  }
  // 选默认地址或第一个
  selectedAddress.value = addresses.value.find(a => a.isDefault === 1) || addresses.value[0]
  // 下单
  try {
    const amount = parseFloat(total.value) * 100
    const res = await api.placeOrderAPI({ shopId, addressId: selectedAddress.value.id, remark: '' })
    currentOrder.value = { ...res.data, amount }
    showPay.value = true
  } catch (e) {
    uni.showToast({ title: typeof e === 'string' ? e : '下单失败', icon: 'none' })
  }
}

const confirmPay = async () => {
  try {
    await api.payTakeoutOrderAPI(currentOrder.value.id)
    showPay.value = false; shopItems.value = []; total.value = 0
    uni.showToast({ title: '支付成功！', icon: 'success' })
    loadCart()
  } catch (e) { uni.showToast({ title: '支付失败', icon: 'none' }) }
}
</script>

<template>
  <view class="page">
    <view class="header"><text class="title">🛒 购物车</text><text class="clear" @click="clearCart">清空</text></view>
    <block v-for="shop in shopItems" :key="shop.shopId">
      <view class="shop-block"><text class="shop-name">{{ shop.shopName }}</text>
        <view class="cart-item" v-for="ci in shop.items" :key="ci.dishId || ci.setmealId">
          <view class="item-img"><text>{{ ci.image || '🍲' }}</text></view>
          <view class="item-info"><text class="item-name">{{ ci.name }}</text><text class="item-price">¥{{ ci.price }}</text></view>
          <view class="qty">
            <button class="qty-btn" @click="changeQty(ci, -1)">−</button>
            <text class="qty-num">{{ ci.number }}</text>
            <button class="qty-btn plus" @click="changeQty(ci, 1)">+</button>
          </view>
        </view>
      </view>
    </block>
    <view v-if="!shopItems.length" class="empty"><text class="empty-icon">🛒</text><text class="empty-text">购物车空空如也</text></view>
    <view class="bottom-bar" v-if="shopItems.length">
      <view class="total">
        <view style="font-size:12px;color:#999" v-if="shopInfo.deliveryFee > 0">配送费 ¥{{ (shopInfo.deliveryFee / 100).toFixed(1) }}</view>
        <view style="font-size:12px;color:#999" v-if="shopInfo.minOrder > 0">起送 ¥{{ (shopInfo.minOrder / 100).toFixed(1) }}</view>
        <text>合计 ¥{{ total }}</text>
        <text style="font-size:10px;color:#999;display:block">已含配送费</text>
      </view>
      <button class="checkout-btn" @click="placeOrder">去结算</button>
    </view>

    <!-- 支付确认弹窗 -->
    <view v-if="showPay" class="mask" @click="showPay=false">
      <view class="pay-panel" @click.stop>
        <text class="pay-title">确认支付</text>
        <view class="pay-info">
          <text class="pay-label">配送至</text>
          <text class="pay-val" v-if="selectedAddress">{{ selectedAddress.contactName }} {{ selectedAddress.phone }}\n{{ (selectedAddress.province||'')+(selectedAddress.city||'')+(selectedAddress.district||'')+' '+selectedAddress.detail }}</text>
        </view>
        <view class="pay-amount">
          <text class="amt-label">支付金额</text>
          <text class="amt-num">¥{{ total }}</text>
        </view>
        <text class="pay-hint">模拟支付 — 点击确认即完成支付</text>
        <view class="pay-actions">
          <button class="pay-cancel" @click="showPay=false">取消</button>
          <button class="pay-confirm" @click="confirmPay">确认支付</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{padding-bottom:70px;background:#F8F9FA}
.header{display:flex;justify-content:space-between;padding:14px 16px;background:#fff;border-bottom:1px solid #f0f0f0}.title{font-size:17px;font-weight:600}.clear{font-size:13px;color:#bbb}
.shop-block{background:#fff;margin:8px 16px 0;padding:14px;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}.shop-name{font-size:14px;font-weight:600;display:block;margin-bottom:10px;color:#333}
.cart-item{display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid #f5f5f5}.cart-item:last-child{border-bottom:none}
.item-img{width:48px;height:48px;border-radius:8px;background:#FFF3EB;display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0}
.item-info{flex:1}.item-name{display:block;font-size:14px;color:#333}.item-price{display:block;font-size:15px;font-weight:600;color:#E8702A}
.qty{display:flex;align-items:center;gap:8px}.qty-btn{width:26px;height:26px;border-radius:50%;border:1px solid #ddd;background:#fff;font-size:16px;display:flex;align-items:center;justify-content:center;padding:0;line-height:26px;color:#666}.qty-btn.plus{background:#E8702A;color:#fff;border-color:#E8702A}.qty-num{font-size:14px;min-width:20px;text-align:center}
.empty{text-align:center;padding:80px 0}.empty-icon{font-size:50px}.empty-text{display:block;font-size:14px;color:#bbb;margin-top:8px}
.bottom-bar{position:fixed;bottom:0;left:0;right:0;background:#fff;padding:12px 16px;display:flex;align-items:center;justify-content:space-between;border-top:1px solid #f0f0f0;z-index:50}.total{font-size:20px;font-weight:700;color:#E8702A}.checkout-btn{background:#E8702A;color:#fff;border:none;border-radius:22px;padding:10px 28px;font-size:15px;font-weight:600}
.mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);z-index:200;display:flex;align-items:center;justify-content:center}
.pay-panel{background:#fff;border-radius:12px;padding:20px;width:300px;margin:0 20px}
.pay-title{display:block;font-size:17px;font-weight:600;text-align:center;margin-bottom:16px}
.pay-info{margin-bottom:12px}.pay-label{display:block;font-size:12px;color:#999;margin-bottom:4px}.pay-val{display:block;font-size:14px;color:#333;white-space:pre-line;line-height:1.4}
.pay-amount{text-align:center;border-top:1px solid #f0f0f0;padding-top:12px;margin-bottom:4px}.amt-label{display:block;font-size:12px;color:#999}.amt-num{display:block;font-size:28px;font-weight:700;color:#E8702A;margin-top:4px}
.pay-hint{display:block;font-size:11px;color:#bbb;text-align:center;margin-bottom:16px}
.pay-actions{display:flex;gap:8px}.pay-cancel,.pay-confirm{flex:1;padding:10px;border-radius:20px;font-size:14px}.pay-cancel{border:1px solid #ddd;background:#fff;color:#666}.pay-confirm{background:#E8702A;color:#fff;border:none}
</style>
