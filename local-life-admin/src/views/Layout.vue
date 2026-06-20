<template>
  <el-container style="min-height:100vh">
    <el-aside width="220px">
      <div class="logo">🏪 本地生活·商家</div>
      <el-menu :default-active="route.path" router background-color="#1a1a2e" text-color="#ccc"
               active-text-color="#FF6B35">
        <el-menu-item index="/dashboard">
          <el-icon>
            <DataAnalysis/>
          </el-icon>
          工作台
        </el-menu-item>
        <el-menu-item index="/staff">
          <el-icon>
            <UserFilled/>
          </el-icon>
          店员管理
        </el-menu-item>
        <el-menu-item index="/shop">
          <el-icon>
            <Shop/>
          </el-icon>
          店铺信息
        </el-menu-item>
        <el-menu-item index="/category">
          <el-icon>
            <FolderOpened/>
          </el-icon>
          分类管理
        </el-menu-item>
        <el-menu-item index="/dish">
          <el-icon>
            <DishDot/>
          </el-icon>
          菜品管理
        </el-menu-item>
        <el-menu-item index="/setmeal">
          <el-icon>
            <TakeawayBox/>
          </el-icon>
          套餐管理
        </el-menu-item>
        <el-menu-item index="/voucher">
          <el-icon>
            <Ticket/>
          </el-icon>
          优惠券管理
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon>
            <Document/>
          </el-icon>
          订单处理
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header
          style="background:#fff;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between;padding:0 24px">
        <span style="font-weight:600;font-size:14px">{{ route.meta.title }}</span>
        <div style="display:flex;align-items:center;gap:12px">
          <span style="font-size:13px;color:#666">{{ user?.name || user?.nickName || '' }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main style="background:#f5f5f5">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {getMeAPI} from '../api'

const router = useRouter()
const route = useRoute()
const user = ref(null)

onMounted(async () => {
  try {
    const res = await getMeAPI();
    user.value = res.data
  } catch (e) {
  }
})

const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, .1);
}

.el-menu {
  border-right: none;
}

.el-main {
  padding: 20px;
}
</style>
