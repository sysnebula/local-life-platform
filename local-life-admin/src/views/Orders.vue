<template>
  <el-card>
    <div style="margin-bottom:16px">
      <el-radio-group v-model="filterStatus" @change="fetch">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="0">待接单</el-radio-button>
        <el-radio-button :value="1">已接单</el-radio-button>
        <el-radio-button :value="2">配送中</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="4">已取消</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="tableData" stripe v-loading="loading" size="default" highlight-current-row @row-click="showDetail(row.id)">
      <el-table-column prop="orderNumber" label="订单号" width="180"/>
      <el-table-column label="金额" width="100">
        <template #default="{row}">¥{{ (row.amount / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="['warning','','','success','info'][row.status]">
            {{ ['待接单', '已接单', '配送中', '已完成', '已取消'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注"/>
      <el-table-column prop="createTime" label="下单时间" width="180"/>
      <el-table-column label="操作" width="280">
        <template #default="{row}">
          <el-button v-if="row.status===0" size="small" type="primary" @click="act(row.id,'accept')">接单</el-button>
          <el-button v-if="row.status===1" size="small" type="primary" @click="act(row.id,'deliver')">开始配送
          </el-button>
          <el-button v-if="row.status===2" size="small" type="success" @click="act(row.id,'complete')">确认完成
          </el-button>
          <el-button v-if="row.status<=1" size="small" type="danger" @click="act(row.id,'cancel')">取消</el-button>
          <el-button size="small" @click="showDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next"
                   @current-change="fetch" style="margin-top:16px;justify-content:flex-end"/>

    <el-dialog v-model="detailVisible" title="订单详情" width="500px">
      <el-table :data="details" stripe>
        <el-table-column prop="name" label="菜品"/>
        <el-table-column prop="price" label="单价"/>
        <el-table-column prop="number" label="数量"/>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {
  acceptOrderAPI,
  cancelOrderAPI,
  completeOrderAPI,
  deliverOrderAPI,
  getOrderDetailAPI,
  getOrderPageAPI
} from '../api'
import {shopStore} from '../store'

const tableData = ref([]);
const loading = ref(false);
const page = ref(1);
const total = ref(0);
const filterStatus = ref(undefined)
const detailVisible = ref(false);
const details = ref([])

const fetch = async () => {
  loading.value = true
  try {
    const res = await getOrderPageAPI({shopId: shopStore.shopId, status: filterStatus.value, page: page.value, size: 10});
    tableData.value = res.data.records;
    total.value = res.data.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}
onMounted(fetch)

const act = async (id, action) => {
  try {
    if (action === 'accept') await acceptOrderAPI(id)
    else if (action === 'deliver') await deliverOrderAPI(id)
    else if (action === 'complete') await completeOrderAPI(id)
    else if (action === 'cancel') await cancelOrderAPI(id)
    ElMessage.success('操作成功');
    fetch()
  } catch (e) {
  }
}
const showDetail = async (id) => {
  try {
    const res = await getOrderDetailAPI(id);
    details.value = res.data;
    detailVisible.value = true
  } catch (e) {
  }
}
</script>
