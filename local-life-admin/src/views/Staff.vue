<template>
  <div>
    <el-card>
      <div style="display:flex;justify-content:space-between;margin-bottom:16px">
        <el-input v-model="searchName" placeholder="搜索姓名" style="width:200px" clearable @change="fetch"/>
        <el-button type="primary" @click="openDialog(null)">+ 新增店员</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="name" label="姓名"/>
        <el-table-column prop="phone" label="手机号"/>
        <el-table-column prop="username" label="账号"/>
        <el-table-column prop="status" label="状态">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"/>
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next"
                     @current-change="fetch" style="margin-top:16px;justify-content:flex-end"/>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId?'编辑店员':'新增店员'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone"/>
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="form.username"/>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" :placeholder="editId?'留空不修改':''"/>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.sex">
            <el-option :value="1" label="男"/>
            <el-option :value="2" label="女"/>
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idNumber"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {addEmployeeAPI, getEmployeesAPI, toggleEmployeeAPI, updateEmployeeAPI} from '../api'

const tableData = ref([])
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const total = ref(0)
const searchName = ref('')
const dialogVisible = ref(false)
const editId = ref(null)
const form = reactive({name: '', phone: '', username: '', password: '', sex: 1, idNumber: ''})

const fetch = async () => {
  loading.value = true
  try {
    const res = await getEmployeesAPI({page: page.value, size: 10, name: searchName.value});
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
    phone: row.phone,
    username: row.username,
    password: '',
    sex: row.sex,
    idNumber: row.idNumber
  })
  else Object.assign(form, {name: '', phone: '', username: '', password: '', sex: 1, idNumber: ''})
  dialogVisible.value = true
}
const save = async () => {
  saving.value = true
  try {
    if (editId.value) await updateEmployeeAPI(editId.value, form)
    else await addEmployeeAPI(form)
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
    await toggleEmployeeAPI(row.id);
    ElMessage.success('操作成功');
    fetch()
  } catch (e) {
  }
}
onMounted(fetch)
</script>
