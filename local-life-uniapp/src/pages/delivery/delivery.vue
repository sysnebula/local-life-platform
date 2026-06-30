<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import api from '../../utils/api.js'

const shops = ref([])
const keyword = ref('')

onLoad((options) => {
  keyword.value = options?.keyword || ''
  loadShops()
})

const loadShops = async () => {
  try {
    let res
    if (keyword.value) {
      res = await api.searchShopsAPI({ keyword: keyword.value, page: 1, size: 50 })
    } else {
      res = await api.getShopListAPI({ page: 1, size: 50 })
    }
    shops.value = (res.data?.records || []).map(s => ({
      id: s.id, icon: s.image || '🍲', name: s.name, area: s.area || '',
      score: s.score || '-',
      monthSold: (s.sold || 0) > 1000 ? Math.floor(s.sold / 1000) + 'k' : (s.sold || '-'),
      minOrder: '¥' + ((s.minOrder || 0) / 100).toFixed(0),
      deliveryFee: '¥' + ((s.deliveryFee || 0) / 100).toFixed(0),
      time: '约' + (s.deliveryTime || 30) + '分钟',
      image: s.image || ''
    }))
  } catch (e) {}
}

const goShop = (id) => uni.navigateTo({ url: '/pages/shop-detail/shop-detail?id=' + id + '&tab=1' })
</script>

<template>
  <view class="page">
    <view class="shop-d" v-for="s in shops" :key="s.id" @click="goShop(s.id)">
      <view class="sd-img">
        <image v-if="s.icon && s.icon.startsWith('http')" :src="s.icon" mode="aspectFill" style="width:64px;height:64px;border-radius:10px" />
        <text v-else style="font-size:36px">{{ s.icon }}</text>
      </view>
      <view class="sd-info">
        <view class="sd-nr"><text class="sd-name">{{ s.name }}</text></view>
        <view class="sd-meta"><text class="sd-score">⭐{{ s.score }}分</text><text class="sd-sold">月售{{ s.monthSold }}+</text></view>
        <view class="sd-deliver">起送{{ s.minOrder }} | 配送费{{ s.deliveryFee }} | {{ s.time }}</view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page{background:#F8F9FA;min-height:100vh}
.shop-d{display:flex;gap:12px;padding:16px;background:#fff;margin:8px 16px 0;border-radius:12px;box-shadow:0 1px 4px rgba(0,0,0,.04)}
.sd-img{width:64px;height:64px;border-radius:10px;background:#FFF3EB;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.sd-info{flex:1;min-width:0}.sd-nr{display:flex;align-items:center;gap:6px;margin-bottom:4px}
.sd-name{font-size:15px;font-weight:600;color:#222}
.sd-meta{margin-bottom:4px}.sd-score{font-size:12px;color:#E8702A}.sd-sold{font-size:11px;color:#999;margin-left:6px}
.sd-deliver{font-size:12px;color:#666;margin-bottom:4px}
</style>
