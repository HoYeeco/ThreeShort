<template>
  <div class="users-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <!-- 查询表单 -->
      <el-form :model="queryForm" :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="用户名/真实姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="用户角色">
          <el-select v-model="queryForm.role" placeholder="请选择" clearable style="width: 150px">
            <el-option label="居民" value="RESIDENT" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="维护员" value="MAINTAINER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 用户表格 -->
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="住址" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="info" size="small" @click="handleResetPassword(row)">
              重置密码
            </el-button>
            <el-button type="primary" size="small" @click="handleChangeRole(row)">
              分配角色
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="queryForm.page"
          :page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        
        <el-form-item label="住址" prop="address">
          <el-input
            v-model="editForm.address"
            placeholder="请输入住址"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="400px"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="用户">
          <el-input :value="currentUser?.realName" disabled />
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
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSubmit">
            确定重置
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="400px"
      @close="handleRoleDialogClose"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="100px"
      >
        <el-form-item label="用户">
          <el-input :value="currentUser?.realName" disabled />
        </el-form-item>
        
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(currentUser?.role || '')">
            {{ getRoleText(currentUser?.role || '') }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="新角色" prop="role">
          <el-select v-model="roleForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通居民" value="RESIDENT" />
            <el-option label="社区管理员" value="ADMIN" />
            <el-option label="系统维护人员" value="MAINTAINER" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="roleLoading" @click="handleRoleSubmit">
            确定分配
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules, TagProps } from 'element-plus'
import type { User, UserQueryForm, UserUpdateForm } from '@/types/user'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const passwordLoading = ref(false)
const roleLoading = ref(false)
const editDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const currentUser = ref<User | null>(null)

const queryForm = reactive<UserQueryForm>({
  page: 1,
  size: 10,
  keyword: '',
  role: undefined,
  status: undefined
})

const editForm = reactive({
  id: 0,
  username: '',
  realName: '',
  phone: '',
  idCard: '',
  address: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const roleForm = reactive({
  role: '' as 'RESIDENT' | 'ADMIN' | 'MAINTAINER' | ''
})

const editFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const roleFormRef = ref<FormInstance>()

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const editRules: FormRules = {
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
  ]
}

const passwordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const roleRules: FormRules = {
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const dialogTitle = ref('编辑用户')

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    RESIDENT: '居民',
    ADMIN: '管理员',
    MAINTAINER: '维护员'
  }
  return roleMap[role] || role
}

const getRoleTagType = (role: string): TagProps['type'] => {
  const typeMap: Record<string, TagProps['type']> = {
    RESIDENT: 'info',
    ADMIN: 'warning',
    MAINTAINER: 'success'
  }
  return typeMap[role] || 'info'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userStore.fetchUserList(queryForm)
    userList.value = response.records
    total.value = response.total
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryForm.page = 1
  loadUsers()
}

const handleSizeChange = (newSize: number) => {
  queryForm.size = newSize
  queryForm.page = 1
  loadUsers()
}

const handleCurrentChange = (newPage: number) => {
  queryForm.page = newPage
  loadUsers()
}

const resetQuery = () => {
  Object.assign(queryForm, {
    page: 1,
    size: 10,
    keyword: '',
    role: undefined,
    status: undefined
  })
  loadUsers()
}

const handleEdit = (user: User) => {
  currentUser.value = user
  dialogTitle.value = '编辑用户'
  
  Object.assign(editForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    phone: user.phone,
    idCard: user.idCard,
    address: user.address
  })
  
  editDialogVisible.value = true
}

const handleToggleStatus = async (user: User) => {
  const action = user.status === 1 ? '禁用' : '启用'
  const newStatus = user.status === 1 ? 0 : 1
  
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${user.realName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.updateUserStatus(user.id, newStatus as 0 | 1)
    
    user.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (error) {
    // 用户取消或操作失败
  }
}

const handleResetPassword = (user: User) => {
  currentUser.value = user
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    submitLoading.value = true
    
    const updateForm: UserUpdateForm = {
      realName: editForm.realName,
      phone: editForm.phone,
      idCard: editForm.idCard,
      address: editForm.address
    }
    
    await userStore.updateUserInfoByAdmin(editForm.id, updateForm)
    
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadUsers()
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '更新失败')
  } finally {
    submitLoading.value = false
  }
}

const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value || !currentUser.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    passwordLoading.value = true
    
    await userStore.resetUserPassword(currentUser.value.id, passwordForm.newPassword)
    
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch (error: any) {
    if (error.fields) {
      return
    }
    ElMessage.error(error.message || '密码重置失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleDialogClose = () => {
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
  currentUser.value = null
}

const handlePasswordDialogClose = () => {
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const handleRoleDialogClose = () => {
  roleForm.role = ''
  roleFormRef.value?.clearValidate()
}

const handleChangeRole = (user: User) => {
  currentUser.value = user
  roleForm.role = user.role
  roleDialogVisible.value = true
}

const handleRoleSubmit = async () => {
  if (!roleFormRef.value || !currentUser.value) return
  
  try {
    await roleFormRef.value.validate()
    
    if (roleForm.role === currentUser.value.role) {
      ElMessage.warning('新角色与当前角色相同')
      return
    }
    
    roleLoading.value = true
    
    await userStore.updateUserRole(currentUser.value.id, roleForm.role)
    
    // 更新本地数据
    currentUser.value.role = roleForm.role as 'RESIDENT' | 'ADMIN' | 'MAINTAINER'
    const userIndex = userList.value.findIndex(u => u.id === currentUser.value!.id)
    if (userIndex !== -1) {
      userList.value[userIndex].role = roleForm.role as 'RESIDENT' | 'ADMIN' | 'MAINTAINER'
    }
    
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '角色分配失败')
  } finally {
    roleLoading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.users-container {
  .card-header {
    font-size: 18px;
    font-weight: 600;
  }
  
  .query-form {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 4px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style> 