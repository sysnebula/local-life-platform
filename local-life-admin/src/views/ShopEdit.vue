<template>
  <el-card v-loading="loading">
    <template #header>🏪 编辑店铺信息</template>
    <el-form :model="form" label-width="100px" style="max-width:800px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="店铺名称">
            <el-input v-model="form.name"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="联系电话">
            <el-input v-model="form.phone"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="人均消费(元)">
            <el-input-number v-model="form.avgPrice" :min="0"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="营业时间">
            <el-input v-model="form.openHours" placeholder="10:00-22:00"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="配送费(元)">
            <el-input-number v-model="form.deliveryFee" :min="0"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="起送价(元)">
            <el-input-number v-model="form.minOrder" :min="0"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="经度(GEO)">
            <el-input v-model="form.longitude"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="纬度(GEO)">
            <el-input v-model="form.latitude"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="营业"
                       inactive-text="休息"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="地址">
        <el-input v-model="form.address"/>
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.description" type="textarea" :rows="3"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveShop" :loading="saving">💾 保存修改</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {getMyShopAPI, updateShopAPI} from '../api'
import {shopStore} from '../store'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  name: '',
  phone: '',
  avgPrice: 0,
  openHours: '',
  deliveryFee: 0,
  minOrder: 0,
  longitude: 0,
  latitude: 0,
  status: 1,
  address: '',
  description: ''
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyShopAPI(shopStore.shopId)
    if (res.data) {
      Object.assign(form, res.data)
      // 分 → 元
      form.avgPrice = (form.avgPrice / 100).toFixed(2)
      form.deliveryFee = (form.deliveryFee / 100).toFixed(2)
      form.minOrder = (form.minOrder / 100).toFixed(2)
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
})

const saveShop = async () => {
  saving.value = true
  try {
    await updateShopAPI({
      ...form,
      avgPrice: Math.round(form.avgPrice * 100),
      deliveryFee: Math.round(form.deliveryFee * 100),
      minOrder: Math.round(form.minOrder * 100)
    });
    ElMessage.success('保存成功')
  } catch (e) {
  } finally {
    saving.value = false
  }
}
</script>
