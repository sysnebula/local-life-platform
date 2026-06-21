import { reactive } from 'vue'

// 商家店铺信息（登录后由 Layout 初始化）
export const shopStore = reactive({
  shopId: null,
  shopName: ''
})

export function setShopId(id) { shopStore.shopId = id }
export function setShopName(name) { shopStore.shopName = name }

// 默认 shopId（从 localStorage 恢复，否则默认 1；后续从 /merchant/me 接口获取后覆盖）
if (!shopStore.shopId) {
  shopStore.shopId = Number(localStorage.getItem('shopId')) || 1
}
