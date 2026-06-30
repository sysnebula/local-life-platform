import { reactive } from 'vue'

// 注意：Snowflake ID 超过 JS Number 精度，必须用字符串存储
export const shopStore = reactive({
  shopId: localStorage.getItem('shopId') || null,
  shopName: ''
})

export function saveShopId(id) {
  localStorage.setItem('shopId', String(id))
  shopStore.shopId = String(id)
}
