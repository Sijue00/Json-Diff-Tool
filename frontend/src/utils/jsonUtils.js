class JsonUtils {
  /**
   * 验证JSON格式
   * @param {string} jsonString - JSON字符串
   * @returns {Object} 验证结果
   */
  validateJson(jsonString) {
    try {
      if (!jsonString || jsonString.trim() === '') {
        return { valid: true, error: null }
      }
      
      JSON.parse(jsonString)
      return { valid: true, error: null }
    } catch (error) {
      // 提取错误位置信息
      const match = error.message.match(/position (\d+)/)
      const position = match ? parseInt(match[1]) : 0
      
      // 计算行号和列号
      const lines = jsonString.substring(0, position).split('\n')
      const lineNumber = lines.length
      const columnNumber = lines[lines.length - 1].length + 1
      
      return {
        valid: false,
        error: `第 ${lineNumber} 行，第 ${columnNumber} 列: ${error.message}`,
        lineNumber,
        columnNumber,
        position
      }
    }
  }

  /**
   * 格式化JSON字符串
   * @param {string} jsonString - JSON字符串
   * @param {number} indent - 缩进空格数
   * @returns {string} 格式化后的JSON字符串
   */
  formatJson(jsonString, indent = 2) {
    try {
      if (!jsonString || jsonString.trim() === '') {
        return ''
      }
      
      const obj = JSON.parse(jsonString)
      return JSON.stringify(obj, null, indent)
    } catch (error) {
      console.error('格式化JSON失败:', error)
      return jsonString
    }
  }

  /**
   * 压缩JSON字符串
   * @param {string} jsonString - JSON字符串
   * @returns {string} 压缩后的JSON字符串
   */
  compressJson(jsonString) {
    try {
      if (!jsonString || jsonString.trim() === '') {
        return ''
      }
      
      const obj = JSON.parse(jsonString)
      return JSON.stringify(obj)
    } catch (error) {
      console.error('压缩JSON失败:', error)
      return jsonString
    }
  }

  /**
   * 获取JSON对象的所有路径
   * @param {Object} obj - JSON对象
   * @param {string} parentPath - 父路径
   * @returns {Array} 路径数组
   */
  getAllPaths(obj, parentPath = '') {
    const paths = []
    
    if (obj === null || obj === undefined) {
      return paths
    }
    
    if (typeof obj !== 'object') {
      return paths
    }
    
    if (Array.isArray(obj)) {
      obj.forEach((item, index) => {
        const currentPath = parentPath ? `${parentPath}[${index}]` : `[${index}]`
        paths.push(currentPath)
        paths.push(...this.getAllPaths(item, currentPath))
      })
    } else {
      Object.keys(obj).forEach(key => {
        const currentPath = parentPath ? `${parentPath}.${key}` : key
        paths.push(currentPath)
        paths.push(...this.getAllPaths(obj[key], currentPath))
      })
    }
    
    return paths
  }

  /**
   * 根据路径获取JSON对象的值
   * @param {Object} obj - JSON对象
   * @param {string} path - 路径
   * @returns {*} 路径对应的值
   */
  getValueByPath(obj, path) {
    if (!path) return obj
    
    const parts = path.split('.')
    let current = obj
    
    for (const part of parts) {
      // 处理数组索引
      const arrayMatch = part.match(/^(\w+)\[(\d+)\]$/)
      if (arrayMatch) {
        const [, key, index] = arrayMatch
        if (current[key] && Array.isArray(current[key])) {
          current = current[key][parseInt(index)]
        } else {
          return undefined
        }
      } else {
        if (current && typeof current === 'object' && part in current) {
          current = current[part]
        } else {
          return undefined
        }
      }
      
      if (current === undefined) return undefined
    }
    
    return current
  }

  /**
   * 根据路径设置JSON对象的值
   * @param {Object} obj - JSON对象
   * @param {string} path - 路径
   * @param {*} value - 要设置的值
   */
  setValueByPath(obj, path, value) {
    if (!path) return
    
    const parts = path.split('.')
    let current = obj
    
    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i]
      
      if (!(part in current) || typeof current[part] !== 'object') {
        // 检查下一部分是否是数组索引
        const nextPart = parts[i + 1]
        const isArray = /^\d+$/.test(nextPart)
        current[part] = isArray ? [] : {}
      }
      
      current = current[part]
    }
    
    const lastPart = parts[parts.length - 1]
    current[lastPart] = value
  }

  /**
   * 删除JSON对象中指定路径的值
   * @param {Object} obj - JSON对象
   * @param {string} path - 路径
   */
  deleteValueByPath(obj, path) {
    if (!path) return
    
    const parts = path.split('.')
    let current = obj
    
    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i]
      
      if (!(part in current) || typeof current[part] !== 'object') {
        return // 路径不存在
      }
      
      current = current[part]
    }
    
    const lastPart = parts[parts.length - 1]
    delete current[lastPart]
  }

  /**
   * 比较两个JSON对象的类型
   * @param {*} a - 第一个值
   * @param {*} b - 第二个值
   * @returns {boolean} 类型是否相同
   */
  isSameType(a, b) {
    if (a === null && b === null) return true
    if (a === null || b === null) return false
    
    const typeA = Array.isArray(a) ? 'array' : typeof a
    const typeB = Array.isArray(b) ? 'array' : typeof b
    
    return typeA === typeB
  }

  /**
   * 深度克隆JSON对象
   * @param {*} obj - 要克隆的对象
   * @returns {*} 克隆后的对象
   */
  deepClone(obj) {
    if (obj === null || typeof obj !== 'object') {
      return obj
    }
    
    if (Array.isArray(obj)) {
      return obj.map(item => this.deepClone(item))
    }
    
    const cloned = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        cloned[key] = this.deepClone(obj[key])
      }
    }
    
    return cloned
  }

  /**
   * 计算JSON对象的深度
   * @param {*} obj - JSON对象
   * @returns {number} 深度
   */
  getDepth(obj) {
    if (obj === null || typeof obj !== 'object') {
      return 0
    }
    
    let maxDepth = 0
    
    if (Array.isArray(obj)) {
      obj.forEach(item => {
        maxDepth = Math.max(maxDepth, this.getDepth(item))
      })
    } else {
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          maxDepth = Math.max(maxDepth, this.getDepth(obj[key]))
        }
      }
    }
    
    return maxDepth + 1
  }

  /**
   * 计算JSON对象的大小（键的数量）
   * @param {*} obj - JSON对象
   * @returns {number} 大小
   */
  getSize(obj) {
    if (obj === null || typeof obj !== 'object') {
      return 0
    }
    
    let size = 0
    
    if (Array.isArray(obj)) {
      size = obj.length
      obj.forEach(item => {
        size += this.getSize(item)
      })
    } else {
      size = Object.keys(obj).length
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          size += this.getSize(obj[key])
        }
      }
    }
    
    return size
  }

  /**
   * 规范化JSON键名（排序）
   * @param {*} obj - JSON对象
   * @returns {*} 规范化后的对象
   */
  normalizeKeys(obj) {
    if (obj === null || typeof obj !== 'object') {
      return obj
    }
    
    if (Array.isArray(obj)) {
      return obj.map(item => this.normalizeKeys(item))
    }
    
    const normalized = {}
    const sortedKeys = Object.keys(obj).sort()
    
    sortedKeys.forEach(key => {
      normalized[key] = this.normalizeKeys(obj[key])
    })
    
    return normalized
  }

  /**
   * 将JSON转换为XML
   * @param {*} obj - JSON对象
   * @param {string} rootName - 根元素名称
   * @returns {string} XML字符串
   */
  jsonToXml(obj, rootName = 'root') {
    let xml = ''
    
    if (Array.isArray(obj)) {
      obj.forEach((item, index) => {
        xml += `<item index="${index}">${this.jsonToXml(item, 'item')}</item>`
      })
    } else if (typeof obj === 'object' && obj !== null) {
      xml += `<${rootName}>`
      Object.keys(obj).forEach(key => {
        const value = obj[key]
        if (typeof value === 'object') {
          xml += this.jsonToXml(value, key)
        } else {
          xml += `<${key}>${value}</${key}>`
        }
      })
      xml += `</${rootName}>`
    } else {
      xml += String(obj)
    }
    
    return xml
  }

  /**
   * 安全的JSON字符串化
   * @param {*} obj - 要字符串化的对象
   * @param {*} replacer - 替换函数
   * @param {number} space - 缩进空格数
   * @returns {string} JSON字符串
   */
  safeStringify(obj, replacer = null, space = 0) {
    try {
      return JSON.stringify(obj, replacer, space)
    } catch (error) {
      console.error('JSON字符串化失败:', error)
      return String(obj)
    }
  }

  /**
   * 安全的JSON解析
   * @param {string} jsonString - JSON字符串
   * @param {*} defaultValue - 解析失败时的默认值
   * @returns {*} 解析后的对象或默认值
   */
  safeParse(jsonString, defaultValue = null) {
    try {
      if (!jsonString || typeof jsonString !== 'string') {
        return defaultValue
      }
      return JSON.parse(jsonString)
    } catch (error) {
      console.error('JSON解析失败:', error)
      return defaultValue
    }
  }


  
  /**
 * HTML特殊字符转义
 * @param {string} text - 原始文本
 * @returns {string} 转义后的HTML
 */
escapeHtml(text) {
  if (typeof text !== 'string') {
    text = String(text ?? '')
  }
  
  const htmlEscapes = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;'
  }
  
  return text.replace(/[&<>"'/]/g, (match) => htmlEscapes[match])
}

/**
 * CSV值转义处理
 * @param {any} value - 任意值
 * @returns {string} 安全的CSV字段
 */
escapeCsvValue(value) {
  if (value === null || value === undefined) {
    return '""'
  }
  
  const stringValue = String(value)
  
  // 如果包含特殊字符，用双引号包裹并转义内部双引号
  if (/[",\n\r]/.test(stringValue)) {
    return `"${stringValue.replace(/"/g, '""')}"`
  }
  
  return stringValue
}

}

export const jsonUtils = new JsonUtils()
export default JsonUtils

