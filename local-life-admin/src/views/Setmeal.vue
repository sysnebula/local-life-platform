<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <el-input v-model="searchName" placeholder="搜索套餐" style="width:200px" clearable @change="fetch"/>
      <el-button type="primary" @click="openDialog(null)">+ 新增套餐</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="套餐名称"/>
      <el-table-column label="价格" width="100">
        <template #default="{row}">¥{{ (row.price / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="包含菜品" min-width="200">
        <template #default="{row}">{{ row.dishes?.map(d => d.name + '×' + d.copies).join('、') }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggle(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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

    <el-dialog v-model="dialogVisible" :title="editId?'编辑套餐':'新增套餐'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)">
          <el-input-number v-model="form.price" :min="0" :precision="2"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description"/>
        </el-form-item>
        <el-form-item label="包含菜品">
          <div v-for="(d,i) in form.dishes" :key="i" style="display:flex;gap:8px;margin-bottom:4px;align-items:center">
            <el-input v-model="d.name" placeholder="菜品名" style="width:140px"/>
            <el-input-number v-model="d.price" :min="0" placeholder="单价" style="width:100px"/>
            <el-input-number v-model="d.copies" :min="1" placeholder="份数" style="width:80px"/>
            <el-button @click="form.dishes.splice(i,1)">✕</el-button>
          </div>
          <el-button size="small" @click="form.dishes.push({name:'',price:0,copies:1})">+添加菜品</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {
  addSetmealAPI,
  deleteSetmealAPI,
  getCategoriesAPI,
  getSetmealPageAPI,
  toggleSetmealAPI,
  updateSetmealAPI
} from '../api'

const tableData = ref([]);
const loading = ref(false);
const saving = ref(false);
const page = ref(1);
const total = ref(0);
const searchName = ref('')
const dialogVisible = ref(false);
const editId = ref(null);
const categories = ref([])
const form = reactive({name: '', categoryId: null, price: 0, description: '', dishes: [], shopId: 1})

onMounted(async () => {
  fetch()
  try {
    const res = await getCategoriesAPI({shopId: 1, type: 2});
    categories.value = res.data
  } catch (e) {
  }
})
const fetch = async () => {
  loading.value = true
  try {
    const res = await getSetmealPageAPI({shopId: 1, page: page.value, size: 10});
    tableData.value = res.data.records;
    total.value = res.data.total
  } catch (e) {
  } finally {
    loading.value = false
  }
}
const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) Object.assign(form, {
    name: row.name,
    categoryId: row.categoryId,
    price: row.price / 100,
    description: row.description || '',
    dishes: row.dishes?.map(d => ({name: d.name, price: d.price / 100, copies: d.copies})) || []
  })
  else Object.assign(form, {name: '', categoryId: null, price: 0, description: '', dishes: []})
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    const data = {
      ...form,
      price: Math.round(form.price * 100),
      shopId: 1,
      dishes: form.dishes.map(d => ({...d, price: Math.round(d.price * 100)}))
    }
    if (editId.value) {
      data.id = editId.value;
      await updateSetmealAPI(data)
    } else await addSetmealAPI(data)
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetch()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
const toggle = async (row) => {
  try {
    await toggleSetmealAPI(row.id);
    ElMessage.success('操作成功');
    fetch()
  } catch (e) {
  }
}
const del = async (id) => {
  try {
    await deleteSetmealAPI(id);
    ElMessage.success('已删除');
    fetch()
  } catch (e) {
  }
}
</script>
