<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <div>
        <el-radio-group v-model="filterType" @change="fetch">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button :value="0">普通券</el-radio-button>
          <el-radio-button :value="1">秒杀券</el-radio-button>
        </el-radio-group>
      </div>
      <div>
        <el-button @click="fetchOrders">📋 领取记录</el-button>
        <el-button type="primary" @click="openDialog(null)">+ 新增优惠券</el-button>
      </div>
    </div>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="title" label="券名称"/>
      <el-table-column label="类型" width="100">
        <template #default="{row}">
          <el-tag :type="row.type===1?'danger':''">{{ row.type === 1 ? '秒杀券' : '普通券' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="售价" width="100">
        <template #default="{row}">¥{{ (row.payValue / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="面值" width="100">
        <template #default="{row}">¥{{ (row.actualValue / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="库存" width="100">
        <template #default="{row}">
          <span v-if="row.stock==null">不限量</span>
          <span v-else>{{ row.stock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="row.type===0" size="small" type="warning" @click="openSeckill(row)">⚡转秒杀</el-button>
          <el-button v-if="row.type===1" size="small" type="warning" @click="editSeckill(row)">⚡秒杀库存</el-button>
          <el-popconfirm title="确认删除？" @confirm="del(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next"
                   @current-change="fetch" style="margin-top:16px;justify-content:flex-end"/>

    <el-dialog v-model="dialogVisible" :title="editId?'编辑优惠券':'新增优惠券'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="券名称">
          <el-input v-model="form.title"/>
        </el-form-item>
        <el-form-item label="优惠券类型">
          <el-radio-group v-model="form.voucherType">
            <el-radio :value="0">普通券</el-radio>
            <el-radio :value="1">秒杀券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="售价(元)">
          <el-input-number v-model="form.payValue" :min="0" :precision="2"/>
        </el-form-item>
        <el-form-item label="面值(元)">
          <el-input-number v-model="form.actualValue" :min="0" :precision="2"/>
        </el-form-item>
        <el-form-item v-if="form.voucherType===0" label="库存">
          <el-input-number v-model="form.stock" :min="0" placeholder="留空=不限量" controls-position="right"/>
        </el-form-item>
        <template v-if="form.voucherType===1">
          <el-form-item label="秒杀库存">
            <el-input-number v-model="form.seckillStock" :min="1"/>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.seckillBeginTime" type="datetime" placeholder="选择开始时间"/>
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.seckillEndTime" type="datetime" placeholder="选择结束时间"/>
          </el-form-item>
        </template>
        <el-form-item label="副标题">
          <el-input v-model="form.subTitle"/>
        </el-form-item>
        <el-form-item label="使用规则">
          <el-input v-model="form.rules" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="seckillVisible" title="⚡ 秒杀库存设置" width="500px">
      <el-form :model="seckillForm" label-width="100px">
        <el-form-item label="库存">
          <el-input-number v-model="seckillForm.stock" :min="1"/>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="seckillForm.beginTime" type="datetime" placeholder="选择开始时间"/>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="seckillForm.endTime" type="datetime" placeholder="选择结束时间"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="seckillVisible=false">取消</el-button>
        <el-button type="primary" @click="saveSeckill" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="orderVisible" title="📋 优惠券领取记录" width="700px">
      <el-table :data="orderData" stripe v-loading="orderLoading" max-height="400">
        <el-table-column prop="voucherTitle" label="券名称" min-width="140"/>
        <el-table-column prop="userPhone" label="用户手机" width="140"/>
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="['warning','success','info',''][row.status]" size="small">
              {{ ['未支付','已支付','已退款','已核销'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="购买时间" width="170">
          <template #default="{row}">{{ row.createTime }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="orderPage" :total="orderTotal" :page-size="10" layout="prev,pager,next"
                     @current-change="fetchOrders" style="margin-top:12px;justify-content:flex-end"/>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {convertSeckillAPI, createVoucherAPI, deleteVoucherAPI, getVoucherOrdersAPI, getVoucherPageAPI, updateVoucherAPI} from '../api'
import {shopStore} from '../store'

const tableData = ref([]);
const loading = ref(false);
const saving = ref(false);
const page = ref(1);
const total = ref(0)
const filterType = ref('');
const dialogVisible = ref(false);
const seckillVisible = ref(false)
const editId = ref(null);
const seckillVoucherId = ref(null)
const form = reactive({title: '', subTitle: '', payValue: 0, actualValue: 0, stock: null, voucherType: 0, seckillStock: 200, seckillBeginTime: '', seckillEndTime: '', rules: '', shopId: shopStore.shopId})
const seckillForm = reactive({stock: 200, beginTime: '', endTime: ''})
const orderVisible = ref(false)
const orderData = ref([])
const orderLoading = ref(false)
const orderPage = ref(1)
const orderTotal = ref(0)

const fetch = async () => {
  loading.value = true
  try {
    const res = await getVoucherPageAPI({shopId: shopStore.shopId, type: filterType.value || undefined, page: page.value, size: 10});
    tableData.value = res.data.records;
    total.value = res.data.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}
onMounted(fetch)
const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) Object.assign(form, {
    title: row.title,
    subTitle: row.subTitle,
    payValue: row.payValue / 100,
    actualValue: row.actualValue / 100,
    stock: row.stock,
    voucherType: row.type || 0,
    seckillStock: row.seckillStock || 200,
    seckillBeginTime: row.seckillBeginTime || '',
    seckillEndTime: row.seckillEndTime || '',
    rules: row.rules || ''
  })
  else Object.assign(form, {title: '', subTitle: '', payValue: 0, actualValue: 0, stock: null, voucherType: 0, seckillStock: 200, seckillBeginTime: '', seckillEndTime: '', rules: ''})
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    const isSeckill = form.voucherType === 1
    const data = {
      title: form.title,
      subTitle: form.subTitle,
      payValue: Math.round(form.payValue * 100),
      actualValue: Math.round(form.actualValue * 100),
      stock: isSeckill ? null : (form.stock > 0 ? form.stock : null),
      rules: form.rules,
      shopId: shopStore.shopId
    }
    // 秒杀券字段
    if (isSeckill) {
      data.stock = form.seckillStock
      data.beginTime = form.seckillBeginTime
      data.endTime = form.seckillEndTime
    }
    if (editId.value) await updateVoucherAPI(editId.value, data)
    else await createVoucherAPI(data)
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetch()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
const openSeckill = (row) => {
  seckillVoucherId.value = row.id;
  seckillForm.stock = 200;
  seckillForm.beginTime = '';
  seckillForm.endTime = '';
  seckillVisible.value = true
}
const editSeckill = (row) => {
  seckillVoucherId.value = row.id;
  seckillForm.stock = row.seckillStock || 0;
  seckillForm.beginTime = row.seckillBeginTime || '';
  seckillForm.endTime = row.seckillEndTime || '';
  seckillVisible.value = true
}
const saveSeckill = async () => {
  saving.value = true
  try {
    await convertSeckillAPI(seckillVoucherId.value, {
      stock: seckillForm.stock,
      beginTime: seckillForm.beginTime, endTime: seckillForm.endTime
    })
    ElMessage.success('秒杀设置已保存');
    seckillVisible.value = false;
    fetch()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
const fetchOrders = async () => {
  orderVisible.value = true
  orderLoading.value = true
  try {
    // 先拉券列表获取券标题映射
    const vRes = await getVoucherPageAPI({ shopId: shopStore.shopId, page: 1, size: 100 })
    const voucherMap = {}
    ;(vRes.data?.records || []).forEach(v => { voucherMap[v.id] = v.title })
    // 再拉订单记录
    const res = await getVoucherOrdersAPI({ shopId: shopStore.shopId, page: orderPage.value, size: 10 })
    orderData.value = (res.data?.records || []).map(o => ({
      ...o,
      voucherTitle: voucherMap[o.voucherId] || '已删除的券'
    }))
    orderTotal.value = res.data?.total || 0
  } catch (e) {
  } finally {
    orderLoading.value = false
  }
}
const del = async (id) => {
  try {
    await deleteVoucherAPI(id);
    ElMessage.success('已删除');
    fetch()
  } catch (e) {
  }
}
</script>
