import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import type { User, LoginForm, RegisterForm, UserUpdateForm, UserQueryForm, UserListResponse } from '@/types/user'



export const useUserStore = defineStore('user', () => {
  // 状态 - 简化版，去掉token
  const userInfo = ref<User | null>(null)
  const loading = ref<boolean>(false)
  const isLoggedIn = computed(() => !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isResident = computed(() => userInfo.value?.role === 'RESIDENT')
  const isMaintainer = computed(() => userInfo.value?.role === 'MAINTAINER')

  // 从localStorage恢复状态
  const initStore = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('userInfo')
      }
    }
  }

  // 保存状态到localStorage
  const saveToStorage = () => {
    if (userInfo.value) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  // 清除状态
  const clearStorage = () => {
    localStorage.removeItem('userInfo')
  }

  // 登录 - 简化版，去掉token
  const login = async (loginForm: LoginForm) => {
    loading.value = true
    try {
      const response = await request.post('/auth/login', loginForm)
      
      // request拦截器已经处理了Result结构，直接使用response.data
      const loginData = response.data
      
      // 设置用户信息
      userInfo.value = {
        id: loginData.userId,
        username: loginData.username,
        realName: loginData.realName,
        role: loginData.role, // 后端返回的是枚举，前端会自动转为字符串
        avatar: loginData.avatar,
        communityId: loginData.communityId,
        phone: '',
        idCard: '',
        address: '',
        status: 1,
        createdTime: '',
        updatedTime: ''
      }
      saveToStorage()
      
      return loginData
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '登录失败')
    } finally {
      loading.value = false
    }
  }

  // 注册
  const register = async (registerForm: RegisterForm) => {
    try {
      const response = await request.post('/auth/register', registerForm)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '注册失败')
    }
  }

  // 登出 - 简化版
  const logout = async () => {
    try {
      await request.post('/auth/logout')
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      userInfo.value = null
      clearStorage()
      // 跳转到登录页
      window.location.href = '/login'
    }
  }

  // 获取用户信息
  const fetchUserInfo = async (userId?: number) => {
    try {
      const url = userId ? `/user/${userId}` : `/user/${userInfo.value?.id}`
      const response = await request.get(url)
      
      if (!userId) {
        userInfo.value = response.data
        saveToStorage()
      }
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '获取用户信息失败')
    }
  }

  // 更新用户信息
  const updateUserInfo = async (updateForm: UserUpdateForm) => {
    try {
      const response = await request.put(`/user/${userInfo.value?.id}`, updateForm)
      
      // 更新本地用户信息
      if (userInfo.value) {
        Object.assign(userInfo.value, updateForm)
        saveToStorage()
      }
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '更新用户信息失败')
    }
  }

  // 获取用户列表
  const fetchUserList = async (queryForm: UserQueryForm): Promise<UserListResponse> => {
    try {
      const response = await request.get('/user/list', { params: queryForm })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '获取用户列表失败')
    }
  }

  // 管理员更新用户信息
  const updateUserInfoByAdmin = async (userId: number, updateForm: UserUpdateForm) => {
    try {
      const response = await request.put(`/user/${userId}`, updateForm)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '更新用户信息失败')
    }
  }

  // 管理员更新用户状态
  const updateUserStatus = async (userId: number, status: 0 | 1) => {
    try {
      const response = await request.put(`/user/${userId}/status?status=${status}`)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '更新用户状态失败')
    }
  }

  // 管理员重置用户密码
  const resetUserPassword = async (userId: number, newPassword: string) => {
    try {
      const response = await request.put(`/user/${userId}/password?newPassword=${encodeURIComponent(newPassword)}`)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '重置密码失败')
    }
  }

  // 用户修改自己的密码
  const changePassword = async (userId: number, oldPassword: string, newPassword: string) => {
    try {
      const response = await request.put(`/user/${userId}/change-password?oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}&currentUserId=${userId}`)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '密码修改失败')
    }
  }

  // 管理员更新用户角色
  const updateUserRole = async (userId: number, role: string) => {
    try {
      const response = await request.put(`/user/${userId}/role?role=${role}`)
      return response
    } catch (error: any) {
      throw new Error(error.response?.data?.message || error.message || '更新用户角色失败')
    }
  }

  // 权限检查方法
  const hasPermission = (_permission: string): boolean => {
    if (!userInfo.value) return false
    // 简化版权限检查
    return userInfo.value.role === 'ADMIN'
  }

  const hasRole = (role: string): boolean => {
    return userInfo.value?.role === role
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.includes(userInfo.value?.role || '')
  }

  // 检查是否可以访问用户管理
  const canAccessUserManagement = computed(() => {
    return isAdmin.value
  })

  // 检查是否可以访问系统管理功能
  const canAccessSystemManagement = computed(() => {
    return isAdmin.value
  })

  // 检查是否可以访问维护功能
  const canAccessMaintenance = computed(() => {
    return isMaintainer.value
  })

  // 检查是否可以访问自己的资源
  const canAccessOwnResource = (resourceUserId: number): boolean => {
    return userInfo.value?.id === resourceUserId || isAdmin.value
  }

  // 检查是否可以管理用户
  const canManageUsers = computed(() => {
    return isAdmin.value
  })

  // 检查是否可以审核内容
  const canReviewContent = computed(() => {
    return isAdmin.value
  })

  // 检查是否可以配置系统
  const canConfigureSystem = computed(() => {
    return isAdmin.value
  })

  const checkAuth = async () => {
    // 简化版认证检查，基于本地存储
    return !!userInfo.value
  }

  // 初始化store
  initStore()

  return {
    userInfo,
    loading,
    isLoggedIn,
    isAdmin,
    isResident,
    isMaintainer,
    canAccessUserManagement,
    canAccessSystemManagement,
    canAccessMaintenance,
    canManageUsers,
    canReviewContent,
    canConfigureSystem,
    login,
    register,
    logout,
    fetchUserInfo,
    updateUserInfo,
    fetchUserList,
    updateUserInfoByAdmin,
    updateUserStatus,
    resetUserPassword,
    changePassword,
    updateUserRole,
    hasPermission,
    hasRole,
    hasAnyRole,
    canAccessOwnResource,
    checkAuth,
    initStore
  }
}) 