<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const tab = ref(0)
const shopId = ref(null)
const shopName = ref('')
const shop = ref({})
const vouchers = ref([])
const menu = ref([])
const notes = ref([])
const cartCount = ref(0)
const cartTotal = ref(0)
const cartItems = ref([])
const showCart = ref(false)

onShow(() => { cartCount.value = 0; cartTotal.value = 0; cartItems.value = []; showCart.value = false; loadCartSummary() })

onLoad((options) => {
  const id = options.id
  const t = options.tab ? parseInt(options.tab) : 0
  shopId.value = id; tab.value = t
  if (!id) return
  loadShop(id); loadVouchers(id); loadMenu(id)
  if (t === 1) loadCartSummary()
})

const loadShop = async (id) => {
  try { const res = await api.getShopDetailAPI(id); if (res.data) { shop.value = res.data; shopName.value = res.data.name } } catch (e) {}
}
const loadVouchers = async (sid) => {
  try { const res = await api.getVoucherListAPI(sid); vouchers.value = (res.data?.records || []).map(v => ({ ...v, payValueYuan: ((v.payValue || 0) / 100).toFixed(1), actualValueYuan: ((v.actualValue || 0) / 100).toFixed(0) })) } catch (e) {}
}
const loadMenu = async (sid) => {
  try {
    const catRes = await api.getCategoriesAPI(sid, 1)
    const cats = catRes.data || []
    const m = []
    for (const c of cats) {
      const dishRes = await api.getDishesAPI(c.id)
      const dishes = (dishRes.data || []).map(d => ({ ...d, price: ((d.price || 0) / 100).toFixed(1) }))
      m.push({ name: c.name, dishes })
    }
    menu.value = m
  } catch (e) {}
}
const switchTab = (idx) => { tab.value = idx; if (idx === 1) loadCartSummary() }
const showCartPopup = () => { loadCartSummary(); loadCartDetail() }
const hideCartPopup = () => { showCart.value = false }
const loadCartSummary = async () => {
  try {
    const res = await api.getCartAPI()
    const items = res.data || []
    let count = 0, total = 0
    items.forEach(i => { count += i.number || 1; total += (i.price || 0) * (i.number || 1) })
    cartCount.value = count; cartTotal.value = (total / 100).toFixed(0)
  } catch (e) {}
}
const loadCartDetail = async () => {
  try {
    const res = await api.getCartAPI()
    const items = (res.data || []).map(i => ({ ...i, showPrice: ((i.price || 0) / 100).toFixed(1) }))
    let total = 0
    items.forEach(i => { total += (i.price || 0) * (i.number || 1) })
    cartItems.value = items; cartCount.value = items.length; cartTotal.value = (total / 100).toFixed(0); showCart.value = true
  } catch (e) {}
}
const cartPlus = async (dishId) => { try { await api.updateCartAPI('dish_' + dishId, { number: 1 }); loadCartDetail() } catch (e) {} }
const cartMinus = async (dishId) => { try { await api.updateCartAPI('dish_' + dishId, { number: 0 }); loadCartDetail() } catch (e) {} }
const clearCart = async () => { try { await api.clearCartAPI(); showCart.value = false; cartCount.value = 0; cartTotal.value = 0 } catch (e) {} }
const goCart = () => uni.navigateTo({ url: '/pages/cart/cart' })

// 券购买确认弹窗（用wx原生API避开uni编译问题）
const buyVoucher = (item) => {
  if (!uni.getStorageSync('token')) { uni.switchTab({ url: '/pages/profile/profile' }); return }
  wx.showModal({
    title: item.title,
    content: `售价 ¥${item.payValueYuan || '--'}，面值 ¥${item.actualValueYuan || '--'}，确认购买？`,
    success(res) {
      if (!res.confirm) return
      const call = item.type === 1 ? api.seckillAPI(item.id) : api.buyVoucherAPI(item.id)
      call.then(() => {
        uni.showToast({ title: '购买成功！', icon: 'success' })
        loadVouchers(shopId.value)
      }).catch(e => uni.showToast({ title: typeof e === 'string' ? e : '购买失败', icon: 'none' }))
    }
  })
}

const addCart = (item) => {
  if (!uni.getStorageSync('token')) { uni.switchTab({ url: '/pages/profile/profile' }); return }
  api.addCartAPI({ shopId: shopId.value, dishId: item.id, name: item.name, price: Math.round(parseFloat(item.price) * 100), number: 1 })
    .then(() => { uni.showToast({ title: '已加入购物车', icon: 'success' }); loadCartSummary() })
    .catch(() => uni.showToast({ title: '添加失败', icon: 'none' }))
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="h-name">{{ shopName || '加载中...' }}</text>
      <text class="h-meta">⭐{{ shop.score || '-' }} · 人均¥{{ (shop.avgPrice / 100) || '-' }} · 🕐{{ shop.openHours || '-' }}</text>
      <text class="h-meta">📍 {{ shop.address || '-' }}</text>
    </view>

    <view class="tabs">
      <text class="tab" :class="{ active: tab === 0 }" @click="switchTab(0)">团购券</text>
      <text class="tab" :class="{ active: tab === 1 }" @click="switchTab(1)">外卖菜单</text>
      <text class="tab" :class="{ active: tab === 2 }" @click="switchTab(2)">探店笔记</text>
    </view>

    <view v-if="tab === 0" class="sec">
      <view class="v-card" v-for="v in vouchers" :key="v.id" @tap="buyVoucher(v)">
        <view class="v-price">¥{{ v.payValueYuan }}</view>
        <view class="v-info">
          <text class="v-title">{{ v.title }}</text>
          <text class="v-desc">
            <template v-if="v.type === 1">秒杀库存: {{ v.seckillStock ?? '-' }}</template>
            <template v-else>库存: {{ v.stock != null ? v.stock : '不限量' }}</template>
          </text>
        </view>
        <view class="v-btn" :class="{ seckill: v.type === 1 }">{{ v.type === 1 ? '⚡秒杀' : '购买' }}</view>
      </view>
    </view>

    <view v-if="tab === 1" class="sec">
      <block v-for="cat in menu" :key="cat.name">
        <text class="cat-title">{{ cat.name }}</text>
        <view class="dish-item" v-for="d in cat.dishes" :key="d.id">
          <view class="d-img"><text>🍲</text></view>
          <view class="d-info"><text class="d-name">{{ d.name }}</text><text class="d-price">¥{{ d.price }}</text></view>
          <button class="add-btn" @click="addCart(d)">+</button>
        </view>
      </block>
    </view>

    <view v-if="tab === 2" class="sec">
      <view class="note" v-for="n in notes" :key="n.id">
        <view class="note-hd"><text class="note-user">{{ n.userName }}</text><text class="note-time">{{ n.time }}</text></view>
        <text class="note-content">{{ n.content }}</text>
      </view>
    </view>

    <view v-if="tab === 1 && cartCount > 0" style="height:50px" />
    <view class="cart-bar" v-if="tab === 1 && cartCount > 0" @click="showCartPopup">
      <view class="cb-left">
        <view class="cb-icon">🛒<text class="cb-badge">{{ cartCount }}</text></view>
        <text class="cb-total">¥{{ cartTotal }}</text>
      </view>
      <view class="cb-btn">去结算</view>
    </view>

    <view v-if="showCart" class="mask" @click="hideCartPopup">
      <view class="cart-popup" @click.stop>
        <view class="cp-title">🛒 已选商品<text class="cp-clear" @click="clearCart">清空</text></view>
        <view class="cp-list">
          <view class="cp-item" v-for="ci in cartItems" :key="ci.dishId || ci.setmealId">
            <text class="cp-name">{{ ci.name }}</text>
            <view class="cp-qty"><text class="cp-minus" @click="cartMinus(ci.dishId)">−</text><text class="cp-num">{{ ci.number }}</text><text class="cp-plus" @click="cartPlus(ci.dishId)">+</text></view>
            <text class="cp-price">¥{{ ci.showPrice }}</text>
          </view>
        </view>
        <view v-if="!cartItems.length" class="cp-empty">购物车为空</view>
        <view class="cp-bottom">
          <text class="cp-total">合计 ¥{{ cartTotal }}</text>
          <view class="cp-actions">
            <button class="cp-back" @click="hideCartPopup">继续点餐</button>
            <button class="cp-checkout" @click="goCart">去结算</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.hero{background:#fff;padding:16px;border-bottom:1px solid #f0f0f0}
.h-name{display:block;font-size:20px;font-weight:700;color:#222}
.h-meta{display:block;font-size:12px;color:#999;margin-top:2px}
.tabs{display:flex;background:#fff;border-bottom:1px solid #f0f0f0}
.tab{flex:1;text-align:center;padding:12px;font-size:14px;color:#999}
.tab.active{color:#E8702A;border-bottom:2px solid #E8702A;font-weight:600}
.sec{padding:12px 16px;background:#fff;margin:8px 0;border-radius:0}
.v-card{display:flex;align-items:center;background:#FFF9F5;border-radius:10px;padding:14px;margin-bottom:10px;gap:12px;border:1px solid #FFF0E5}
.v-price{font-size:28px;font-weight:700;color:#E8702A;flex-shrink:0}.v-info{flex:1}.v-title{display:block;font-size:14px;font-weight:600;color:#333}
.v-btn{background:#E8702A;color:#fff;border:none;border-radius:18px;padding:5px 16px;font-size:12px;flex-shrink:0}
.v-btn.seckill{background:#E91E63}
.cat-title{display:inline-block;font-size:15px;font-weight:600;color:#333;margin:10px 0 6px;padding-bottom:4px;border-bottom:2px solid #E8702A}
.dish-item{display:flex;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid #f5f5f5}.dish-item:last-child{border-bottom:none}
.d-img{width:54px;height:54px;border-radius:8px;background:#FFF3EB;display:flex;align-items:center;justify-content:center;font-size:24px;flex-shrink:0}
.d-info{flex:1}.d-name{display:block;font-size:14px;color:#333}.d-price{display:block;font-size:16px;font-weight:600;color:#E8702A}
.add-btn{width:26px;height:26px;border-radius:50%;background:#E8702A;color:#fff;border:none;font-size:16px;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.note{border-bottom:1px solid #f0f0f0;padding:12px 0}.note:last-child{border-bottom:none}
.note-hd{display:flex;gap:8px;margin-bottom:4px}.note-user{font-size:13px;font-weight:500;color:#333}.note-time{font-size:11px;color:#ccc}.note-content{font-size:13px;color:#666;line-height:1.6}
.cart-bar{position:fixed;bottom:0;left:0;right:0;background:#333;color:#fff;display:flex;align-items:center;justify-content:space-between;padding:12px 16px;z-index:100}.cb-left{display:flex;align-items:center;gap:10px}.cb-icon{position:relative;font-size:28px}.cb-badge{position:absolute;top:-8px;right:-10px;background:#F44336;color:#fff;font-size:10px;min-width:18px;height:18px;border-radius:9px;display:flex;align-items:center;justify-content:center;padding:0 4px}.cb-total{font-size:20px;font-weight:700}.cb-btn{background:#E8702A;color:#fff;padding:10px 28px;border-radius:20px;font-size:15px;font-weight:600}
.mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.4);z-index:200;display:flex;align-items:flex-end}
.cart-popup{background:#fff;width:100%;border-radius:16px 16px 0 0;padding:16px;max-height:50vh;display:flex;flex-direction:column}
.cp-title{font-size:16px;font-weight:600;color:#222;display:flex;align-items:center;margin-bottom:12px}.cp-clear{font-size:12px;color:#bbb;margin-left:auto}
.cp-list{flex:1;overflow-y:auto;max-height:200px}.cp-item{display:flex;align-items:center;gap:8px;padding:10px 0;border-bottom:1px solid #f5f5f5}.cp-name{flex:1;font-size:14px;color:#333}.cp-qty{display:flex;align-items:center;gap:6px}.cp-minus,.cp-plus{width:22px;height:22px;border-radius:50%;border:1px solid #ddd;background:#fff;display:flex;align-items:center;justify-content:center;font-size:14px;color:#666}.cp-plus{background:#E8702A;color:#fff;border-color:#E8702A}.cp-num{font-size:14px;min-width:16px;text-align:center}.cp-price{font-size:14px;font-weight:600;color:#E8702A;width:50px;text-align:right}.cp-empty{text-align:center;padding:20px;color:#bbb;font-size:14px}
.cp-bottom{border-top:1px solid #f0f0f0;padding-top:12px;display:flex;align-items:center;justify-content:space-between}.cp-total{font-size:18px;font-weight:700;color:#E8702A}.cp-actions{display:flex;gap:8px}.cp-back{padding:8px 16px;border-radius:20px;font-size:13px;border:1px solid #ddd;background:#fff;color:#666}.cp-checkout{padding:8px 20px;border-radius:20px;font-size:13px;background:#E8702A;color:#fff;border:none}
</style>
