<template>
  <el-card>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <div>
        <el-radio-group v-model="catType" @change="fetch">
          <el-radio-button :value="1">菜品分类</el-radio-button>
          <el-radio-button :value="2">套餐分类</el-radio-button>
        </el-radio-group>
      </div>
      <el-button type="primary" @click="openDialog(null)">+ 新增分类</el-button>
    </div>
    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="分类名称"/>
      <el-table-column prop="sort" label="排序" width="100"/>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="del(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑分类':'新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0"/>
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
import {addCategoryAPI, deleteCategoryAPI, getCategoriesAPI, updateCategoryAPI} from '../api'

const catType = ref(1)
const tableData = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editId = ref(null)
const form = reactive({name: '', sort: 0, type: 1, shopId: 1})

const fetch = async () => {
  loading.value = true
  try {
    const res = await getCategoriesAPI({shopId: 1, type: catType.value});
    tableData.value = res.data
  } catch (e) {
  } finally {
    loading.value = false
  }
}
const openDialog = (row) => {
  editId.value = row?.id || null
  if (row) {
    form.name = row.name;
    form.sort = row.sort
  } else {
    form.name = '';
    form.sort = 0
  }
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  form.type = catType.value;
  form.shopId = 1
  try {
    if (editId.value) {
      form.id = editId.value;
      await updateCategoryAPI(form)
    } else await addCategoryAPI(form)
    ElMessage.success('保存成功');
    dialogVisible.value = false;
    fetch()
  } catch (e) {
  } finally {
    saving.value = false
  }
}
const del = async (id) => {
  try {
    await deleteCategoryAPI(id);
    ElMessage.success('已删除');
    fetch()
  } catch (e) {
  }
}
onMounted(fetch)
</script>
