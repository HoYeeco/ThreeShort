<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  
  .error-content {
    text-align: center;
    
    h1 {
      font-size: 72px;
      color: #409EFF;
      margin-bottom: 20px;
    }
    
    p {
      font-size: 18px;
      color: #666;
      margin-bottom: 30px;
    }
  }
}
</style> 