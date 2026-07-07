import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const RETRYABLE_METHODS = new Set(['get', 'head', 'options'])

function shouldRetry(error) {
  const config = error.config || {}
  const method = String(config.method || 'get').toLowerCase()
  const status = error.response?.status

  if (!RETRYABLE_METHODS.has(method)) return false
  if (config.__retried) return false

  if (!error.response) return true
  return status >= 500 && status < 600
}

function retryRequest(error) {
  const config = error.config || {}
  config.__retried = true
  return request(config)
}

// 请求拦截器 - 附加 JWT Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  async error => {
    if (shouldRetry(error)) {
      try {
        return await retryRequest(error)
      } catch (retryError) {
        error = retryError
      }
    }

    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
