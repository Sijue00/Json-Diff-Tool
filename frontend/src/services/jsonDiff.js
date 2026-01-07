import api from './api'

class JsonDiffService {
  async compare(data) { //JSON对比接口 
    return await api.post('/compare', data) 
  }
  async validate(data) {  //JSON验证接口 ---前端校验
    return await api.post('/validate', data)
  }
  async format(data) { //JSON格式化接口
    return await api.post('/format', data)
  }
  async compress(data) { //JSON压缩接口
    return await api.post('/compress', data)
  }
  async export(data) { //导出---前端实现
    return await api.post('/export', data, { responseType: 'blob' })
  }
  async convert(data) {
    return await api.post('/convert', data)
  }
  async getSample(type = 'basic') {
    return await api.get(`/samples/${type}`)
  }
  async saveHistory(data) {
    return await api.post('/history', data)
  }
  async clearHistory() {
    return await api.delete('/history')
  }
}

export const jsonDiffService = new JsonDiffService()