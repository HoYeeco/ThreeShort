<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(async () => {
  // 应用初始化时检查用户登录状态
  try {
    await userStore.checkAuth()
  } catch (error) {
    // 忽略未登录错误
    console.log('用户未登录')
  }
})
</script>

<style lang="scss">
#app {
  height: 100vh;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style> 