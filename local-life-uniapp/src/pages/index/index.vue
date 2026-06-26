<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const seckills = ref([])
const notes = ref([])

const loadSeckills = async () => {
  if (!uni.getStorageSync('token')) return
  try {
    const res = await api.getVoucherListAPI(1)
    const records = res.data?.records || []
    const seckillRecords = records.filter(v => v.type === 1)
    if (seckillRecords.length) {
      seckills.value = seckillRecords.map(v => ({
        id: v.id, shopId: v.shopId, shopName: v.title,
        title: v.title, price: (v.payValue / 100).toFixed(1),
        orig: (v.actualValue / 100).toFixed(0),
        stock: v.seckillStock != null ? v.seckillStock : '-'
      }))
    }
  } catch (e) {}
}

const loadNotes = async () => {
  if (!uni.getStorageSync('token')) return
  try {
    const res = await api.getShopNotesAPI(1, { page: 1, size: 5 })
    const records = res.data?.records || []
    notes.value = records.map(n => ({
      id: n.id,
      avatar: (n.userName || '用')[0],
      userName: n.userName || '用户',
      shopName: '',
      time: (n.createTime || '').substring(0, 10),
      title: n.title, content: n.content
    }))
  } catch (e) {}
}

onShow(() => { loadSeckills(); loadNotes() })

const goDelivery = () => uni.navigateTo({ url: '/pages/delivery/delivery' })
const goDinein = () => uni.navigateTo({ url: '/pages/dinein/dinein' })
const goAllSeckill = () => uni.navigateTo({ url: '/pages/dinein/dinein' })
const goSeckill = (shopId) => { if (shopId) uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + shopId }) }
const goExplore = () => uni.navigateTo({ url: '/pages/explore/explore' })
const goWriteNote = () => {
  if (!uni.getStorageSync('token')) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.switchTab({ url: '/pages/profile/profile' })
    return
  }
  uni.navigateTo({ url: '/pages/explore/explore' })
}
const goNote = (id) => uni.navigateTo({ url: '/pages/explore/explore?id=' + id })
const goShop = (id) => uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + id })
</script>

<template>
  <view class="page">
    <view class="top-bar">
      <text class="top-city">📍 北京市</text>
      <view class="top-search">
        <text class="icon">🔍</text><input placeholder="搜索店铺、菜品、团购套餐..." />
      </view>
    </view>

    <view class="scene-row">
      <view class="scene-card" @click="goDelivery">
        <view class="scene-icon">🛵</view>
        <text class="scene-title">外卖配送</text>
        <text class="scene-desc">点餐即送</text>
      </view>
      <view class="scene-card" @click="goDinein">
        <view class="scene-icon">🍽️</view>
        <text class="scene-title">到店团购</text>
        <text class="scene-desc">超值秒杀</text>
      </view>
    </view>

    <view class="sec">
      <view class="sec-title">⚡ 团购秒杀<text class="sec-sub">到店可享</text><text class="sec-more" @click="goAllSeckill">更多 ›</text></view>
      <scroll-view scroll-x class="sk-scroll" :show-scrollbar="false">
        <view class="sk-card" v-for="item in seckills" :key="item.id" @click="goSeckill(item.shopId)">
          <text class="sk-shop">{{ item.shopName }}</text>
          <view class="sk-price-row"><text class="sk-price">¥{{ item.price }}</text><text class="sk-orig">¥{{ item.orig }}</text></view>
          <text class="sk-title">{{ item.title }}</text>
          <view class="sk-bottom"><text class="sk-stock">库存 {{ item.stock }}</text></view>
        </view>
      </scroll-view>
    </view>

    <view class="sec">
      <view class="sec-title">📝 探店笔记<text class="sec-more" @click="goExplore">更多 ›</text></view>
      <view class="write-note-entry" @click="goWriteNote">
        <view class="wn-avatar">✍️</view>
        <text class="wn-text">分享你的探店体验...</text>
        <view class="wn-btn">发笔记</view>
      </view>
      <view class="note-card" v-for="item in notes" :key="item.id" @click="goNote(item.id)">
        <view class="note-hd">
          <view class="note-avatar">{{ item.avatar }}</view>
          <view class="note-user-info"><text class="note-user">{{ item.userName }}</text><text class="note-shop">📍 {{ item.shopName }}</text></view>
          <text class="note-time">{{ item.time }}</text>
        </view>
        <text class="note-title">{{ item.title }}</text>
        <text class="note-content">{{ item.content }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{background:#F8F9FA;min-height:100vh;padding-bottom:20px}
.top-bar{background:#fff;padding:16px 16px 12px;display:flex;align-items:center;gap:12px}
.top-city{font-size:15px;font-weight:600;color:#333;white-space:nowrap}
.top-search{flex:1;background:#F5F6F8;border-radius:20px;padding:8px 14px;display:flex;align-items:center;gap:6px}
.top-search input{flex:1;background:none;border:none;outline:none;font-size:13px;color:#333}
.top-search input::placeholder{color:#bbb}.top-search .icon{font-size:14px}
.scene-row{display:flex;padding:16px 16px 0;gap:12px}
.scene-card{flex:1;background:#fff;border-radius:12px;padding:20px 16px;text-align:center;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.scene-icon{width:48px;height:48px;border-radius:50%;background:#FFF3EB;display:flex;align-items:center;justify-content:center;font-size:22px;margin:0 auto 10px}
.scene-title{display:block;font-size:15px;font-weight:600;color:#222;margin-bottom:4px}
.scene-desc{display:block;font-size:12px;color:#999}
.sec{background:#fff;margin:12px 16px 0;padding:16px;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.sec-title{font-size:17px;font-weight:600;color:#222;margin-bottom:12px;display:flex;align-items:center}
.sec-sub{font-size:12px;color:#999;font-weight:400;margin-left:8px}
.sec-more{font-size:13px;color:#999;margin-left:auto}
.sk-scroll{white-space:nowrap}
.sk-card{display:inline-block;width:160px;background:#FFF9F5;border-radius:10px;padding:12px;margin-right:8px;vertical-align:top;border:1px solid #FFF0E5}
.sk-shop{display:block;font-size:11px;color:#999;margin-bottom:2px}
.sk-price-row{display:flex;align-items:baseline;gap:4px;margin-bottom:2px}
.sk-price{font-size:24px;font-weight:700;color:#E8702A}.sk-orig{font-size:11px;color:#ccc;text-decoration:line-through}
.sk-title{display:block;font-size:13px;font-weight:500;color:#333;margin-bottom:6px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}
.sk-bottom{display:flex;justify-content:space-between;font-size:10px}.sk-sold{color:#999}.sk-stock{color:#E8702A}
.write-note-entry{display:flex;align-items:center;gap:10px;background:#F5F6F8;border-radius:10px;padding:12px;margin-bottom:12px}
.wn-avatar{width:36px;height:36px;border-radius:50%;background:#FFF3EB;display:flex;align-items:center;justify-content:center;font-size:18px;flex-shrink:0}
.wn-text{flex:1;font-size:13px;color:#bbb}.wn-btn{background:#E8702A;color:#fff;font-size:12px;padding:5px 16px;border-radius:16px;flex-shrink:0}
.note-card{background:#F8F9FA;border-radius:10px;padding:12px;margin-bottom:10px}
.note-card:last-child{margin-bottom:0}
.note-hd{display:flex;align-items:center;gap:8px;margin-bottom:8px}
.note-avatar{width:36px;height:36px;border-radius:50%;background:#E8702A;color:#fff;display:flex;align-items:center;justify-content:center;font-size:14px;font-weight:600;flex-shrink:0}
.note-user-info{flex:1}.note-user{display:block;font-size:13px;font-weight:500}.note-shop{font-size:11px;color:#E8702A}.note-time{font-size:11px;color:#ccc}
.note-title{display:block;font-size:15px;font-weight:600;color:#222;margin-bottom:4px}
.note-content{font-size:13px;color:#666;line-height:1.6;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}
</style>
