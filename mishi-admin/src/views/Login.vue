<template>
  <div class="login-bg">
    <div class="login-card">
      <h1>🏪 觅食 · 商家后台</h1>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock"
                    @keyup.enter="login"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="login" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
      <p class="hint">默认账号 admin / 123456 · <el-button type="primary" link @click="showRegister=true">注册商家</el-button></p>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="🏪 商家注册（同步创建店铺）" width="520px" :close-on-click-modal="false">
      <el-form :model="regForm" label-width="90px" size="default">
        <el-divider content-position="left">👤 账号信息</el-divider>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="用户名"><el-input v-model="regForm.username" placeholder="登录用户名" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="密码"><el-input v-model="regForm.password" type="password" placeholder="至少6位" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="手机号"><el-input v-model="regForm.phone" placeholder="11位手机号，也作为店铺联系电话" /></el-form-item>
        <el-divider content-position="left">🏪 店铺信息</el-divider>
        <el-row :gutter="12">
          <el-col :span="16"><el-form-item label="店铺名称"><el-input v-model="regForm.shopName" placeholder="如：老王火锅" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型"><el-select v-model="regForm.typeId" placeholder="选择"><el-option v-for="t in shopTypes" :key="t.id" :label="t.name" :value="t.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="regForm.address" placeholder="详细地址" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="regForm.area" placeholder="如：朝阳区" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="regForm.openHours" placeholder="如：10:00-22:00" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="regForm.description" type="textarea" :rows="2" placeholder="店铺简介..." /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister=false">取消</el-button>
        <el-button type="primary" @click="doRegister" :loading="regLoading">注册并创建店铺</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Lock, User} from '@element-plus/icons-vue'
import {loginAPI, registerAPI, getShopTypesAPI} from '../api'
import {saveShopId} from '../store'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({username: '', password: ''})

// 加载店铺类型
const shopTypes = ref([])
onMounted(async () => {
  try { const res = await getShopTypesAPI(); shopTypes.value = res.data || [] } catch (e) {}
})

// 注册
const showRegister = ref(false)
const regLoading = ref(false)
const regForm = reactive({ username: '', password: '', phone: '', shopName: '', typeId: null, area: '', address: '', openHours: '', description: '' })
const doRegister = async () => {
  if (!regForm.username || !regForm.password || !regForm.shopName) {
    ElMessage.warning('请填写用户名、密码和店铺名称')
    return
  }
  regLoading.value = true
  try {
    const res = await registerAPI({ ...regForm, shopPhone: regForm.phone })
    localStorage.setItem('token', res.data.token)
    if (res.data.shopId) saveShopId(res.data.shopId)
    ElMessage.success('注册成功，店铺已创建！')
    router.push('/dashboard')
  } catch(e) {} finally { regLoading.value = false }
}
const rules = {
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

const login = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await loginAPI(form)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))
    if (res.data.shopId) saveShopId(res.data.shopId)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .3);
}

.login-card h1 {
  text-align: center;
  font-size: 22px;
  margin-bottom: 24px;
  color: #333;
}

.hint {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
