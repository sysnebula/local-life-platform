<template>
  <div class="login-bg">
    <div class="login-card">
      <h1>🏪 本地生活 · 商家后台</h1>
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
      <p class="hint">默认账号 admin / 123456</p>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Lock, User} from '@element-plus/icons-vue'
import {loginAPI} from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({username: 'admin', password: '123456'})
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
