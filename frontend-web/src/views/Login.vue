<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    
    <div class="login-content">
      <!-- 左侧信息区域 -->
      <div class="login-info">
        <div class="info-content">
          <h1 class="system-title">
            <el-icon class="title-icon"><House /></el-icon>
            社区诚信管理系统
          </h1>
          <p class="system-desc">构建诚信社区，共享美好生活</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>信用行为管理</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>积分兑换服务</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>社区公约学习</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>居民反馈建议</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录区域 -->
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p class="subtitle">Web管理端</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          label-width="0"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              class="login-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="login-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <el-link type="primary" @click="showResetDialog = true">忘记密码？</el-link>
          <el-link type="primary" @click="goToRegister">注册账号</el-link>
        </div>
      </div>
    </div>

    <!-- 找回密码对话框 -->
    <el-dialog v-model="showResetDialog" title="找回密码" width="400px">
      <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="resetForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="resetForm.newPassword" 
            type="password" 
            placeholder="请输入新密码（6位以上）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="resetForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showResetDialog = false">取消</el-button>
          <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">
            确定重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, House, Check } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { LoginForm } from '@/types/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loginForm = reactive<LoginForm>({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const loading = ref(false)

const showResetDialog = ref(false)
const resetFormRef = ref<FormInstance>()
const resetLoading = ref(false)

const resetForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value || loading.value) return
  
  try {
    loading.value = true
    await loginFormRef.value.validate()
    
    await userStore.login(loginForm)
    
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error: any) {
    if (error.fields) {
      // 表单验证错误
      return
    }
    
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  
  try {
    await resetFormRef.value.validate()
    
    resetLoading.value = true
    
    // 重置密码
    await request.post('/auth/reset-password', null, {
      params: {
        username: resetForm.username,
        newPassword: resetForm.newPassword
      }
    })
    
    ElMessage.success('密码重置成功，请使用新密码登录')
    showResetDialog.value = false
    
    // 清空重置表单
    resetForm.username = ''
    resetForm.newPassword = ''
    resetForm.confirmPassword = ''
    
      } catch (error: any) {
      ElMessage.error(error.message || '密码重置失败')
  } finally {
    resetLoading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    
    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      animation: float 6s ease-in-out infinite;
      
      &.circle-1 {
        width: 120px;
        height: 120px;
        top: 10%;
        left: 10%;
        animation-delay: 0s;
      }
      
      &.circle-2 {
        width: 80px;
        height: 80px;
        top: 20%;
        right: 20%;
        animation-delay: 2s;
      }
      
      &.circle-3 {
        width: 150px;
        height: 150px;
        bottom: 10%;
        left: 15%;
        animation-delay: 4s;
      }
    }
  }
  
  .login-content {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 1;
    
    .login-info {
      flex: 1;
      max-width: 500px;
      padding: 0 60px;
      color: white;
      
      .info-content {
        .system-title {
          font-size: 3rem;
          font-weight: 700;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          gap: 15px;
          
          .title-icon {
            font-size: 3.5rem;
            color: #ffd700;
          }
        }
        
        .system-desc {
          font-size: 1.2rem;
          margin-bottom: 40px;
          opacity: 0.9;
          line-height: 1.6;
        }
        
        .feature-list {
          .feature-item {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;
            font-size: 1.1rem;
            
            .el-icon {
              color: #4ade80;
              font-size: 1.2rem;
            }
          }
        }
      }
    }
    
    .login-box {
      width: 450px;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 20px;
      padding: 50px 40px;
      box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(20px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      
      .login-header {
        text-align: center;
        margin-bottom: 40px;
        
        h2 {
          font-size: 2rem;
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
        }
        
        .subtitle {
          color: #666;
          font-size: 0.9rem;
          opacity: 0.8;
        }
      }
      
      .login-form {
        .el-form-item {
          margin-bottom: 24px;
        }
      
        .login-input {
          :deep(.el-input__wrapper) {
            border-radius: 12px;
            border: 2px solid #e1e5e9;
            transition: all 0.3s;
            
            &:hover {
              border-color: #667eea;
            }
            
            &.is-focus {
              border-color: #667eea;
              box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
          }
        }
        
        .login-btn {
          width: 100%;
          height: 50px;
          border-radius: 12px;
          font-size: 1.1rem;
          font-weight: 600;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;
          transition: all 0.3s;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
          }
          
          &:active {
            transform: translateY(0);
          }
        }
      }
      
      .login-footer {
        text-align: center;
        margin-top: 30px;
        
        .el-link {
          color: #667eea;
          font-size: 0.9rem;
          margin: 0 10px;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .login-container .login-content {
    flex-direction: column;
    padding: 20px;
    
    .login-info {
      max-width: none;
      text-align: center;
      padding: 20px;
      margin-bottom: 20px;
      
      .system-title {
        font-size: 2rem;
        justify-content: center;
        
        .title-icon {
          font-size: 2.5rem;
        }
      }
      
      .system-desc {
        font-size: 1rem;
        margin-bottom: 20px;
      }
      
      .feature-list {
        display: none;
      }
    }
    
    .login-box {
      width: 100%;
      max-width: 400px;
      padding: 30px 25px;
    }
  }
}

@media (max-width: 768px) {
  .login-container .login-content .login-info {
    .system-title {
      font-size: 1.5rem;
      
      .title-icon {
        font-size: 2rem;
      }
    }
  }
}
</style> 