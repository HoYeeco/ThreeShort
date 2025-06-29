<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-box">
        <div class="register-header">
          <h2>用户注册</h2>
          <p>欢迎加入社区诚信管理系统</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          label-width="80px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名（3-20位字符）"
              clearable
            />
          </el-form-item>

          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              clearable
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              clearable
            />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard">
            <el-input
              v-model="registerForm.idCard"
              placeholder="请输入身份证号"
              clearable
            />
          </el-form-item>

          <el-form-item label="住址" prop="address">
            <el-input
              v-model="registerForm.address"
              placeholder="请输入住址（可选）"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（6位以上）"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <el-link type="primary" @click="goToLogin">已有账号？立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// 注册表单
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  realName: '',
  phone: '',
  idCard: '',
  address: '',
  communityId: 1, // 默认设置为示例社区
  password: '',
  confirmPassword: ''
})

// 验证确认密码
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '真实姓名长度在2到10个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    
    loading.value = true
    
    
    await request.post('/auth/register', registerForm)
    
    ElMessage.success('注册成功！请登录')
    
    // 跳转到登录页
    router.push('/login')
    
      } catch (error: any) {
      ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
}

.register-box {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.register-header p {
  color: #666;
  font-size: 0.9rem;
}

.register-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-input {
    height: 45px;
    
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }
  
  .el-select {
    width: 100%;
    
    :deep(.el-input__wrapper) {
      border-radius: 8px;
    }
  }
}

.register-btn {
  width: 100%;
  height: 50px;
  font-size: 1rem;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  
  &:hover {
    opacity: 0.9;
  }
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  
  .el-link {
    color: #667eea;
    font-size: 0.9rem;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style> 