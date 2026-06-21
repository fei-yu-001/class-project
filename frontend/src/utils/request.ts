import axios, { type AxiosError, type AxiosRequestConfig } from 'axios'

// 创建 axios 实例
// 优先使用环境变量 VITE_API_BASE_URL（如 https://api.example.com/api）
// 未配置时使用相对路径 '/api'，生产环境通过 Nginx 反向代理到后端
// 开发环境通过 Vite 代理转发
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,  // 30秒超时，公网环境下可能更慢
  // 允许携带 cookie（如未来需要 session 认证）
  withCredentials: false
})

// 请求拦截器：注入 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码 + 网络错误
request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 业务成功
    if (data && data.code === 200) {
      return data
    }
    // 业务失败
    const message = data?.message || '请求失败'
    return Promise.reject(new Error(message))
  },
  (error: AxiosError<any>) => {
    const status = error.response?.status
    const message = error.response?.data?.message

    // 401 未认证：清除本地状态跳转登录
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 避免在登录页无限重定向
      if (!window.location.hash.includes('/login')) {
        window.location.hash = '/login'
      }
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }

    // 403 权限不足
    if (status === 403) {
      return Promise.reject(new Error(message || '权限不足'))
    }

    // 404/500 等其他错误
    if (status === 404) {
      return Promise.reject(new Error('请求的资源不存在'))
    }
    if (status && status >= 500) {
      return Promise.reject(new Error('服务器开小差了，请稍后重试'))
    }

    // 网络错误（无响应）
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请检查网络'))
    }
    if (!error.response) {
      return Promise.reject(new Error('网络连接失败，请检查网络'))
    }

    return Promise.reject(new Error(message || error.message || '请求失败'))
  }
)

export default request

// 导出请求方法以便在组件中使用时携带额外配置
export type { AxiosRequestConfig }
