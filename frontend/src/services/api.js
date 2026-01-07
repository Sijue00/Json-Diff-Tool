import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.response.use(
  (response) => {
    const res = response.data
    // 解包后端 ApiResponse
    if (res && typeof res === 'object' && 'success' in res) {
      if (!res.success) throw new Error(res.message || '请求失败')
      return res.data
    }
    return res
  },
  (error) => {
    if (error?.response) {
      const message = error.response.data?.message || error.response.statusText
      throw new Error(message || '请求失败')
    }
    if (error?.request) throw new Error('网络连接失败，请检查后端是否启动')
    throw new Error(error?.message || '未知错误')
  }
)

export default api
