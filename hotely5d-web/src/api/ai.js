import axios from 'axios'
import { getToken } from '@/utils/auth'
import { Message } from 'element-ui'

const aiRequest = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 60000
})

aiRequest.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

aiRequest.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 20000) {
      Message({ message: res.message || '请求失败', type: 'error', duration: 5000 })
      return Promise.reject(res.message || 'Error')
    }
    return res
  },
  error => {
    Message({ message: error.message || '网络错误', type: 'error', duration: 5000 })
    return Promise.reject(error)
  }
)

export const chatAPI = (data) => aiRequest.post('/ai/chat', data)
