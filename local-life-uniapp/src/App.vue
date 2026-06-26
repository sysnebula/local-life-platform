<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  const token = uni.getStorageSync('token')
  console.log('本地生活 UniApp 启动')
})
</script>

<style>
page {
  background: #f5f5f5;
  font-family: -apple-system, 'PingFang SC', 'Helvetica Neue', sans-serif;
}
</style>
