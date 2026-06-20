<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:12px">
            <span style="font-size:32px">{{ s.icon }}</span>
            <div>
              <div style="font-size:22px;font-weight:700">{{ s.value }}</div>
              <div style="font-size:12px;color:#999">{{ s.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card>
      <template #header>🔔 最新订单（WebSocket 实时推送）</template>
      <el-table :data="orders" stripe>
        <el-table-column prop="orderNumber" label="订单号" width="160"/>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{row}">¥{{ (row.amount / 100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">{{ ['待接单', '已接单', '配送中', '已完成', '已取消'][row.status] }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180"/>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {getOrderPageAPI} from '../api'

const stats = reactive([
  {icon: '📋', value: '-', label: '今日订单'}, {icon: '💰', value: '-', label: '今日营业额'},
  {icon: '⏳', value: '-', label: '待处理'}, {icon: '🍽️', value: '-', label: '在售菜品'}
])
const orders = ref([])

onMounted(async () => {
  try {
    const res = await getOrderPageAPI({page: 1, size: 5})
    orders.value = res.data?.records || []
    if (res.data) {
      stats[0].value = res.data.total || '-'
      const pending = res.data.records?.filter(o => o.status === 0).length || 0
      stats[2].value = pending
    }
  } catch (e) {
  }
})
</script>
