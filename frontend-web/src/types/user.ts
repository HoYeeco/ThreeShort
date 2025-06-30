export interface User {
  id: number
  username: string
  realName: string
  phone: string
  idCard: string
  address: string
  avatar?: string
  role: 'RESIDENT' | 'ADMIN' | 'MAINTAINER'
  status: number
  communityId: number
  createdTime: string
  updatedTime: string
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  realName: string
  phone: string
  idCard: string
  address: string
  communityId: number
}

export interface UserUpdateForm {
  realName?: string
  phone?: string
  idCard?: string
  address?: string
  avatar?: string
}

export interface UserQueryForm {
  page: number
  size: number
  keyword?: string
  role?: 'RESIDENT' | 'ADMIN' | 'MAINTAINER'
  status?: number
  communityId?: number
}

export interface UserListResponse {
  records: User[]
  total: number
  size: number
  current: number
  pages: number
} 