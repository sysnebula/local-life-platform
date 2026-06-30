<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-row">
            <span class="stat-icon">{{ s.icon }}</span>
            <div class="stat-body">
              <div class="stat-value">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新订单 -->
    <el-card shadow="never">
      <template #header><span class="card-title">📋 最新订单</span><span style="font-size:12px;color:#999;margin-left:8px">每30秒自动刷新</span></template>
      <el-table :data="orders" stripe size="default" highlight-current-row @row-click="goOrder" style="cursor:pointer">
        <el-table-column prop="orderNumber" label="订单号" width="180" />
        <el-table-column label="金额" width="100"><template #default="{row}">¥{{ (row.amount/100).toFixed(2) }}</template></el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag :type="['warning','','primary','success','info'][row.status]" size="small">{{ ['待接单','已接单','配送中','已完成','已取消'][row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderPageAPI, getDishPageAPI, getVoucherPageAPI } from '../api'
import { shopStore } from '../store'

const router = useRouter()
const stats = reactive([
  { icon:'📋', value:'-', label:'总订单' },
  { icon:'⏳', value:'-', label:'待处理' },
  { icon:'💰', value:'-', label:'总营收' },
  { icon:'🍽️', value:'-', label:'在售菜品' }
])
const orders = ref([])

const fetch = async () => {
  try {
    // 订单统计
    const orderRes = await getOrderPageAPI({ shopId: shopStore.shopId, page: 1, size: 100 })
    const records = orderRes.data?.records || []
    orders.value = records.slice(0, 8)
    if (orderRes.data) {
      stats[0].value = orderRes.data.total || 0
      // 待处理：统计所有待接单订单
      const pending = records.filter(o => o.status === 0).length
      stats[1].value = pending
      // 总营收：统计所有已完成订单（status >= 1 排除已取消）
      const revenue = records.filter(o => o.status >= 1 && o.status <= 3).reduce((s, o) => s + (o.amount||0), 0)
      stats[2].value = '¥' + (revenue/100).toFixed(0)
    }
    // 在售菜品数
    const dishRes = await getDishPageAPI({ shopId: shopStore.shopId, page: 1, size: 1 })
    stats[3].value = dishRes.data?.total || 0
  } catch(e) {}
}

onMounted(() => { fetch(); setInterval(fetch, 30000) })

const goOrder = (row) => { router.push('/orders') }
</script>

<style scoped>
.stat-card{border-radius:8px}.stat-row{display:flex;align-items:center;gap:14px}.stat-icon{font-size:28px}.stat-body{flex:1}.stat-value{font-size:22px;font-weight:700;color:#222}.stat-label{font-size:12px;color:#999;margin-top:2px}
.card-title{font-size:15px;font-weight:600}
</style>
