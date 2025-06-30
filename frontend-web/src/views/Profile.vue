<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="rules"
        label-width="100px"
        class="profile-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="profileForm.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="住址" prop="address">
          <el-input
            v-model="profileForm.address"
            placeholder="请输入住址"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUpdate">
            保存修改
          </el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="warning" @click="showPasswordDialog = true">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6位以上）"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSubmit">
            确定修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { UserUpdateForm } from '@/types/user'

const userStore = useUserStore()
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const loading = ref(false)
const passwordLoading = ref(false)
const showPasswordDialog = ref(false)

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  idCard: '',
  address: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { 
      pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, 
      message: '身份证号格式不正确', 
      trigger: 'blur' 
    }
  ],
  address: [
    { max: 200, message: '住址长度不能超过200个字符', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
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

const loadUserInfo = async () => {
  if (userStore.userInfo) {
    try {
      // 从服务器获取完整的用户信息
      const fullUserInfo = await userStore.fetchUserInfo(userStore.userInfo.id)
      
      Object.assign(profileForm, {
        username: fullUserInfo.username,
        realName: fullUserInfo.realName,
        phone: fullUserInfo.phone || '',
        idCard: fullUserInfo.idCard || '',
        address: fullUserInfo.address || ''
      })
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，使用本地存储的信息
      Object.assign(profileForm, {
        username: userStore.userInfo.username,
        realName: userStore.userInfo.realName,
        phone: userStore.userInfo.phone || '',
        idCard: userStore.userInfo.idCard || '',
        address: userStore.userInfo.address || ''
      })
    }
  }
}

const handleUpdate = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    
    loading.value = true
    
    const updateForm: UserUpdateForm = {
      realName: profileForm.realName,
      phone: profileForm.phone,
      idCard: profileForm.idCard,
      address: profileForm.address
    }
    
    await userStore.updateUserInfo(updateForm)
    
    ElMessage.success('信息更新成功')
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '更新失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
  }
  loadUserInfo()
}

const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value || !userStore.userInfo) return
  
  try {
    await passwordFormRef.value.validate()
    
    passwordLoading.value = true
    
    await userStore.changePassword(
      userStore.userInfo.id, 
      passwordForm.oldPassword, 
      passwordForm.newPassword
    )
    
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    handlePasswordDialogClose()
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handlePasswordDialogClose = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

onMounted(async () => {
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  
  .card-header {
    font-size: 18px;
    font-weight: 600;
  }
  
  .profile-form {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
</style> 