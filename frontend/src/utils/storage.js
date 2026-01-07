class Storage {
  constructor() {
    this.prefix = 'json-diff-'
  }

  /**
   * 设置本地存储
   * @param {string} key - 键名
   * @param {*} value - 值
   * @param {number} expire - 过期时间（小时），0表示不过期
   */
  set(key, value, expire = 0) {
    try {
      const data = {
        value: value,
        timestamp: Date.now(),
        expire: expire > 0 ? Date.now() + expire * 60 * 60 * 1000 : 0
      }
      localStorage.setItem(this.prefix + key, JSON.stringify(data))
    } catch (error) {
      console.error('设置本地存储失败:', error)
    }
  }

  /**
   * 获取本地存储
   * @param {string} key - 键名
   * @param {*} defaultValue - 默认值
   * @returns {*} 存储的值或默认值
   */
  get(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(this.prefix + key)
      if (!item) {
        return defaultValue
      }

      const data = JSON.parse(item)
      
      // 检查是否过期
      if (data.expire > 0 && Date.now() > data.expire) {
        this.remove(key)
        return defaultValue
      }

      return data.value
    } catch (error) {
      console.error('获取本地存储失败:', error)
      return defaultValue
    }
  }

  /**
   * 删除本地存储
   * @param {string} key - 键名
   */
  remove(key) {
    try {
      localStorage.removeItem(this.prefix + key)
    } catch (error) {
      console.error('删除本地存储失败:', error)
    }
  }

  /**
   * 清空所有本地存储（带前缀的）
   */
  clear() {
    try {
      const keys = Object.keys(localStorage)
      keys.forEach(key => {
        if (key.startsWith(this.prefix)) {
          localStorage.removeItem(key)
        }
      })
    } catch (error) {
      console.error('清空本地存储失败:', error)
    }
  }

  /**
   * 获取所有键名
   * @returns {Array} 键名数组
   */
  keys() {
    try {
      const keys = []
      const allKeys = Object.keys(localStorage)
      allKeys.forEach(key => {
        if (key.startsWith(this.prefix)) {
          keys.push(key.substring(this.prefix.length))
        }
      })
      return keys
    } catch (error) {
      console.error('获取键名失败:', error)
      return []
    }
  }

  /**
   * 获取存储大小
   * @returns {number} 存储大小（字节）
   */
  size() {
    try {
      let total = 0
      const keys = this.keys()
      keys.forEach(key => {
        total += localStorage.getItem(this.prefix + key).length
      })
      return total
    } catch (error) {
      console.error('获取存储大小失败:', error)
      return 0
    }
  }

  /**
   * 保存历史记录
   * @param {Object} data - 历史数据
   */
  saveHistory(data) {
    try {
      const histories = this.get('histories', [])
      
      // 添加新记录
      histories.unshift({
        ...data,
        id: Date.now(),
        timestamp: Date.now()
      })
      
      // 限制历史记录数量
      const maxHistory = 10
      if (histories.length > maxHistory) {
        histories.splice(maxHistory)
      }
      
      this.set('histories', histories)
    } catch (error) {
      console.error('保存历史记录失败:', error)
    }
  }

  /**
   * 获取历史记录
   * @returns {Array} 历史记录数组
   */
  getHistory() {
    return this.get('histories', [])
  }

  /**
   * 删除历史记录
   * @param {string} id - 记录ID
   */
  removeHistory(id) {
    try {
      const histories = this.get('histories', [])
      const filtered = histories.filter(item => item.id !== id)
      this.set('histories', filtered)
    } catch (error) {
      console.error('删除历史记录失败:', error)
    }
  }

  /**
   * 清空历史记录
   */
  clearHistory() {
    this.remove('histories')
  }

  /**
   * 保存设置
   * @param {Object} settings - 设置对象
   */
  saveSettings(settings) {
    this.set('settings', settings)
  }

  /**
   * 获取设置
   * @returns {Object} 设置对象
   */
  getSettings() {
    return this.get('settings', {})
  }

  /**
   * 保存临时数据
   * @param {string} key - 键名
   * @param {*} value - 值
   * @param {number} minutes - 过期时间（分钟）
   */
  setTemp(key, value, minutes = 60) {
    this.set('temp-' + key, value, minutes / 60)
  }

  /**
   * 获取临时数据
   * @param {string} key - 键名
   * @param {*} defaultValue - 默认值
   * @returns {*} 临时数据
   */
  getTemp(key, defaultValue = null) {
    return this.get('temp-' + key, defaultValue)
  }

  /**
   * 导出数据
   * @returns {Object} 所有存储的数据
   */
  export() {
    try {
      const data = {}
      const keys = this.keys()
      keys.forEach(key => {
        data[key] = this.get(key)
      })
      return data
    } catch (error) {
      console.error('导出数据失败:', error)
      return {}
    }
  }

  /**
   * 导入数据
   * @param {Object} data - 要导入的数据
   */
  import(data) {
    try {
      Object.keys(data).forEach(key => {
        this.set(key, data[key])
      })
    } catch (error) {
      console.error('导入数据失败:', error)
    }
  }
}

export const storage = new Storage()