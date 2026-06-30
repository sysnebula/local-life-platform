<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const seckills = ref([])
const shops = ref([])

onShow(() => { loadSeckills(); loadShops() })

const loadSeckills = async () => {
  try {
    const res = await api.getVoucherListAPI(1)
    const all = res.data?.records || []
    seckills.value = all.filter(v => v.type === 1).map(v => ({
      id: v.id, shopId: v.shopId, name: v.title,
      price: (v.payValue / 100).toFixed(1), orig: (v.actualValue / 100).toFixed(0),
      stock: v.seckillStock != null ? v.seckillStock : '-'
    }))
  } catch (e) {}
}

const loadShops = async () => {
  try {
    const res = await api.getShopListAPI({ page: 1, size: 50 })
    shops.value = (res.data?.records || []).map(s => ({
      id: s.id, icon: '🍲', name: s.name, area: s.area || '',
      score: s.score || '-', avgPrice: '¥' + ((s.avgPrice || 0) / 100).toFixed(0),
      distance: s.area || '-', meal: '', mealSold: ''
    }))
  } catch (e) {}
}

const goShop = (id) => uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + id })
const goSeckill = (shopId) => { if (shopId) uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + shopId }) }
</script>

<template>
  <view class="page">
    <view class="sk-sec">
      <text class="sk-title">⚡ 限时秒杀</text>
      <scroll-view scroll-x class="sk-scroll" :show-scrollbar="false">
        <view class="sk-card" v-for="sk in seckills" :key="sk.id" @click="goSeckill(sk.shopId || 1)">
          <text class="sk-name">{{ sk.name }}</text>
          <text class="sk-price">¥{{ sk.price }}<text class="sk-orig">¥{{ sk.orig }}</text></text>
          <view class="sk-info"><text>库存 {{ sk.stock }}</text></view>
        </view>
      </scroll-view>
    </view>

    <view class="shop-g" v-for="s in shops" :key="s.id" @click="goShop(s.id)">
      <view class="sg-img"><text style="font-size:36px">{{ s.icon }}</text></view>
      <view class="sg-info">
        <view class="sg-nr"><text class="sg-name">{{ s.name }}</text></view>
        <view class="sg-meta"><text class="sg-score">⭐{{ s.score }}分</text><text class="sg-avg">{{ s.avgPrice }}/人</text></view>
        <view class="sg-loc">{{ s.distance }} · {{ s.area }}</view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{background:#F8F9FA;min-height:100vh}
.sk-sec{background:#fff;padding:16px;margin:8px 16px 0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}.sk-title{font-size:16px;font-weight:600;color:#222;margin-bottom:10px}.sk-scroll{white-space:nowrap}
.sk-card{display:inline-block;width:170px;background:#FFF9F5;border:1px solid #FFF0E5;border-radius:10px;padding:12px;margin-right:8px;vertical-align:top}.sk-name{display:block;font-size:12px;color:#999}.sk-price{display:block;font-size:22px;font-weight:700;color:#E8702A;margin:4px 0}.sk-orig{font-size:11px;color:#ccc;text-decoration:line-through;margin-left:4px}.sk-info{font-size:10px;color:#bbb;margin-top:4px}
.shop-g{display:flex;gap:12px;padding:16px;background:#fff;margin:8px 16px 0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.sg-img{width:64px;height:64px;border-radius:10px;background:#FFF3EB;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.sg-info{flex:1;min-width:0}.sg-nr{display:flex;align-items:center;gap:6px;margin-bottom:4px}
.sg-name{font-size:15px;font-weight:600;color:#222}
.sg-meta{margin-bottom:4px}.sg-score{font-size:12px;color:#E8702A}.sg-avg{font-size:11px;color:#999;margin-left:6px}
.sg-loc{font-size:12px;color:#666;margin-bottom:6px}
</style>
