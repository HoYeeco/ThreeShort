import axios, { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({ showSpinner: false })

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true, // 启用cookies，用于Session管理
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 简化版，去掉token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    NProgress.start()
    
    // 如果是 FormData，删除 Content-Type，让浏览器自动设置
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    
    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse): any => {
    NProgress.done()
    
    const { data } = response
    
    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    if (data.code === 200) {
      return data
    }
    
    // 如果返回的状态码不是200，则抛出错误，让调用方处理
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  async (error) => {
    NProgress.done()
    
    // 处理HTTP错误状态码，但不显示错误信息，让调用方处理
    if (error.response) {
      const { status, data } = error.response
      
      // 只对特殊状态码进行统一处理
      switch (status) {
        case 401:
          // 认证失败，跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          // 权限不足，显示错误信息
          ElMessage.error('权限不足')
          break
      }
      
      // 将错误信息传递给调用方
      error.message = data?.message || `HTTP ${status} 错误`
    } else {
      error.message = '网络连接失败'
    }
    
    return Promise.reject(error)
  }
)

export default request 