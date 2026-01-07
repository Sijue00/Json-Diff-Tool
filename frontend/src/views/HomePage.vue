<template>
  <div class="page">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button @click="showSettings = true">
          <el-icon>
            <Setting />
          </el-icon>
          设置
        </el-button>
        <el-button @click="loadSample">
          <el-icon>
            <DocumentAdd />
          </el-icon>
          简单示例数据
        </el-button>
        <el-button @click="loadMultilevel">
          <el-icon>
            <DocumentAdd />
          </el-icon>
          多级示例数据
        </el-button>

        <el-button @click="formatAll" :disabled="!leftJson.trim() && !rightJson.trim()">
          <el-icon>
            <Document />
          </el-icon>
          格式化全部
        </el-button>

        <el-dropdown @command="compressJson" :disabled="!leftJson.trim() && !rightJson.trim()">
          <el-button>
            <el-icon>
              <Minus/>
            </el-icon>压缩 <el-icon>
              <ArrowDown />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-item command="left" :disabled="!leftJson.trim()">
              <el-icon>
                <Back />
              </el-icon> 压缩左侧
            </el-dropdown-item>
            <el-dropdown-item command="right" :disabled="!rightJson.trim()">
              <el-icon>
                <Right />
              </el-icon> 压缩右侧
            </el-dropdown-item>
          </template>
        </el-dropdown>

        <el-button @click="clearAll">
          <el-icon>
            <Delete />
          </el-icon>
          清空
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-switch v-model="realtimeCompare" active-text="实时对比" @change="toggleRealtime" />
        <el-button type="primary" @click="showExport = true" :disabled="!hasDiffResult">
          <el-icon>
            <Download />
          </el-icon>
          导出结果
        </el-button>
      </div>
    </div>
    <!-- 主内容 -->
    <div class="main-area">
      <!-- 左侧 -->
      <div class="editor-panel">
        <div class="panel-header">
          <span class="panel-title">原始 JSON</span>
          <FileUpload side="left" @file-loaded="handleFileLoad" />
        </div>
        <JsonEditor ref="leftEditor" v-model="leftJson" :error="leftError" @change="handleLeftChange" />
      </div>
      <!-- 中间操作 -->
      <div class="action-panel">
        <el-button type="primary" size="large" circle :loading="loading" :disabled="!canCompare" @click="compareJson">
          <el-icon>
            <Right />
          </el-icon>
        </el-button>
        <div class="stats-preview" v-if="hasDiffResult">
          <div class="stat-item added">
            <el-icon>
              <Plus />
            </el-icon>
            <span>{{ diffStats.added }}</span>
          </div>
          <div class="stat-item removed">
            <el-icon>
              <Minus />
            </el-icon>
            <span>{{ diffStats.removed }}</span>
          </div>
          <div class="stat-item modified">
            <el-icon>
              <Edit />
            </el-icon>
            <span>{{ diffStats.modified }}</span>
          </div>
        </div>
        <div class="hint" v-else>
          填入两份 JSON 后点击对比
        </div>
      </div>
      <!-- 右侧 -->
      <div class="editor-panel">
        <div class="panel-header">
          <span class="panel-title">对比 JSON</span>
          <FileUpload side="right" @file-loaded="handleFileLoad" />
        </div>
        <JsonEditor ref="rightEditor" v-model="rightJson" :error="rightError" @change="handleRightChange" />
      </div>
    </div>
    <!-- 底部差异 -->
    <div class="diff-viewer" v-if="hasDiffResult">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="差异导航" name="navigator">
          <DiffNavigator :diff-result="diffResult" @navigate="navigateToDiff" />
        </el-tab-pane>
        <el-tab-pane label="统计信息" name="statistics">
          <StatisticsPanel :diff-result="diffResult" :stats="diffStats" />
        </el-tab-pane>
      </el-tabs>
    </div>
    <SettingsPanel v-model="showSettings" :settings="settings" @update:settings="updateSettings" />

    <ExportDialog v-model="showExport" :diff-result="diffResult" :left-json="leftJson" :right-json="rightJson" />
  </div>
</template>
<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { jsonDiffService } from '../services/jsonDiff'
import { jsonUtils } from '../utils/jsonUtils'
import { ArrowDown, Back, Right as ArrowRight } from '@element-plus/icons-vue'

import JsonEditor from '../components/JsonEditor.vue'
import FileUpload from '../components/FileUpload.vue'
import DiffNavigator from '../components/DiffNavigator.vue'
import StatisticsPanel from '../components/StatisticsPanel.vue'
import SettingsPanel from '../components/SettingsPanel.vue'
import ExportDialog from '../components/ExportDialog.vue'

// 状态
const leftJson = ref('')
const rightJson = ref('')
const leftError = ref('')
const rightError = ref('')
const loading = ref(false)

const diffResult = ref(null)
const diffStats = reactive({ added: 0, removed: 0, modified: 0 })

const activeTab = ref('navigator')
const showSettings = ref(false)
const showExport = ref(false)
const realtimeCompare = ref(false)

//编辑器申明
const leftEditor = ref(null)
const rightEditor = ref(null)

// 对比设置
const settings = reactive({
  ignoreOrder: false,
  ignoreWhitespace: false,
  caseSensitive: true,
  maxDepth: 200,
  theme: 'auto',
  fontSize: 14,
  showLineNumbers: true,
  wordWrap: false,
  minimap: false,
  realtimeCompare: false,
  autoSave: true,
  maxHistory: 10,
  maxFileSize: 10
})

// 计算
const hasDiffResult = computed(() => !!diffResult.value && Array.isArray(diffResult.value.differences))
const canCompare = computed(() => !!leftJson.value.trim() && !!rightJson.value.trim() && !leftError.value && !rightError.value)


// 发送请求
async function compareJson() {
  const L = safeParseJson(leftJson.value)
  const R = safeParseJson(rightJson.value)
  leftError.value = L.ok ? '' : L.error
  rightError.value = R.ok ? '' : R.error
  if (!L.ok || !R.ok) return

  loading.value = true
  try {
    // 走后端结构化对比
    const res = await jsonDiffService.compare({
      left: L.value,
      right: R.value,
      settings: { ...settings }
    })
    diffResult.value = res
    updateStats(res)
    ElMessage.success('对比完成')
  } catch (e) {
    ElMessage.error(e?.message || '对比失败')
    diffResult.value = null
    updateStats({ differences: [] })
  } finally {
    loading.value = false
  }
}

function safeParseJson(text) {
  try {
    if (!text?.trim()) return { ok: false, error: 'JSON 为空' }
    return { ok: true, value: JSON.parse(text) }
  } catch (e) {
    return { ok: false, error: e?.message || 'JSON 解析失败' }
  }
}

function updateStats(result) {
  const diffs = result?.differences || []
  diffStats.added = diffs.filter(d => d.type === 'added').length
  diffStats.removed = diffs.filter(d => d.type === 'removed').length
  diffStats.modified = diffs.filter(d => d.type === 'modified').length
}



function handleLeftChange(text) {
  const p = safeParseJson(text)
  leftError.value = p.ok ? '' : p.error
}
function handleRightChange(text) {
  const p = safeParseJson(text)
  rightError.value = p.ok ? '' : p.error
}

function handleFileLoad({ side, content }) {
  if (side === 'left') leftJson.value = content
  else rightJson.value = content
}

function toggleRealtime(val) {
  settings.realtimeCompare = !!val
}

watch([leftJson, rightJson, () => settings.realtimeCompare], () => {
  if (settings.realtimeCompare) {
    // 简单防抖（避免频繁请求）
    if (compareTimer) clearTimeout(compareTimer)
    compareTimer = setTimeout(() => {
      if (canCompare.value) compareJson()
    }, 400)
  }
})
let compareTimer = null

function updateSettings(next) {
  Object.assign(settings, next)
  realtimeCompare.value = !!settings.realtimeCompare
}

function navigateToDiff(payload) {
  if (!payload?.path) return

  const diffType = (payload?.diff?.type || '').toLowerCase()

  // 清除之前的高亮
  leftEditor.value?.clearHighlight?.()
  rightEditor.value?.clearHighlight?.()

  // 根据差异类型高亮并滚动
  if (diffType === 'added') {
    rightEditor.value?.highlightPath?.(payload.path)
    scrollToEditor(rightEditor) // 滚动到右侧面板
  } else if (diffType === 'removed') {
    leftEditor.value?.highlightPath?.(payload.path)
    scrollToEditor(leftEditor) // 滚动到左侧面板
  } else {
    // 修改类型：两边都高亮，滚动到左侧（或你觉得更重要的那一侧）
    leftEditor.value?.highlightPath?.(payload.path)
    rightEditor.value?.highlightPath?.(payload.path)
    scrollToEditor(leftEditor) // 滚动到左侧面板
  }
}

function loadSample() {
  leftJson.value = JSON.stringify({ a: 1, arr: [1, 2], obj: { x: true } }, null, 2)
  rightJson.value = JSON.stringify({ a: 2, arr: [1, 3], obj: { x: true, y: 'new' } }, null, 2)
}

function loadMultilevel() {
  leftJson.value = createLeftJson();

  rightJson.value = createRightJson();
}


function clearAll() {
  leftJson.value = ''
  rightJson.value = ''
  leftError.value = ''
  rightError.value = ''
  diffResult.value = null
  updateStats({ differences: [] })
}


/**
 * 滚动页面到指定编辑器
 */
function scrollToEditor(editorRef) {
  if (!editorRef?.value?.$el) return

  const element = editorRef.value.$el
  const rect = element.getBoundingClientRect()
  const offset = 80 // 顶部留80px间距，避免紧贴边缘

  window.scrollTo({
    top: rect.top + window.scrollY - offset,
    behavior: 'smooth' // 平滑滚动
  })
}

// 同时格式化左右两侧
function formatAll() {
  try {
    let count = 0
    
    if (leftJson.value.trim()) {
      leftJson.value = jsonUtils.formatJson(leftJson.value, 2)
      count++
    }
    
    if (rightJson.value.trim()) {
      rightJson.value = jsonUtils.formatJson(rightJson.value, 2)
      count++
    }
    
    if (count > 0) {
      ElMessage.success(`成功格式化 ${count} 个编辑器`)
    } else {
      ElMessage.warning('没有可格式化的内容')
    }
  } catch (e) {
    ElMessage.error('格式化失败: ' + e.message)
  }
}

// 压缩（参数来自 dropdown command）
function compressJson(side) {
  try {
    const text = side === 'left' ? leftJson.value : rightJson.value
    if (!text.trim()) {
      ElMessage.warning(`${side === 'left' ? '左侧' : '右侧'} JSON 为空`)
      return
    }
    
    const compressed = jsonUtils.compressJson(text)
    
    if (side === 'left') {
      leftJson.value = compressed
    } else {
      rightJson.value = compressed
    }
    
    ElMessage.success(`${side === 'left' ? '左侧' : '右侧'}压缩成功`)
  } catch (e) {
    ElMessage.error('压缩失败: ' + e.message)
  }
}



function createLeftJson() {
  return JSON.stringify({
    "system": {
      "name": "enterprise-platform",
      "version": "2.5.1",
      "config": {
        "environment": "production",
        "debug": false,
        "database": {
          "primary": {
            "host": "db-cluster-01.internal",
            "port": 5432,
            "credentials": {
              "username": "admin",
              "password": {
                "encrypted": true,
                "value": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "encryption": {
                  "algorithm": "AES-256-GCM",
                  "keyId": "key-vault://production/db-master-key-2024",
                  "parameters": {
                    "keyLength": 256,
                    "mode": "gcm",
                    "metadata": {
                      "created": "2024-01-15T08:30:00Z",
                      "rotationPolicy": {
                        "enabled": true,
                        "intervalDays": 90,
                        "lastRotated": "2024-10-20T14:22:00Z",
                        "nextRotation": "2025-01-20T14:22:00Z",
                        "audit": {
                          "logRetention": {
                            "days": 365,
                            "storage": {
                              "type": "s3",
                              "bucket": "security-logs-prod",
                              "region": "us-east-1",
                              "encryption": {
                                "sse": "aws:kms",
                                "kmsKeyId": "arn:aws:kms:us-east-1:123456789012:key/1234abcd-12ab-34cd-56ef-1234567890ab"
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            },
            "connection": {
              "pool": {
                "min": 10,
                "max": 100,
                "idleTimeout": 30000
              }
            }
          },
          "replica": {
            "host": "db-cluster-replica.internal",
            "port": 5432,
            "readOnly": true
          }
        },
        "cache": {
          "redis": {
            "cluster": {
              "nodes": [
                "redis-01:6379",
                "redis-02:6379",
                "redis-03:6379"
              ],
              "tls": {
                "enabled": true,
                "cert": {
                  "path": "/etc/certs/redis/client.crt",
                  "key": {
                    "path": "/etc/certs/redis/client.key",
                    "permissions": "600"
                  }
                }
              }
            }
          }
        }
      },
      "monitoring": {
        "prometheus": {
          "enabled": true,
          "endpoint": "/metrics",
          "collectors": {
            "system": true,
            "application": true,
            "business": true
          }
        }
      }
    }
  }, null, 2);
}

function createRightJson() {
  return JSON.stringify({
    "system": {
      "name": "test-platform",
      "version": "3.0.0-beta",
      "config": {
        "environment": "staging",
        "debug": true,
        "database": {
          "primary": {
            "host": "db-test.internal",
            "port": 3306,
            "credentials": {
              "username": "test_user",
              "password": {
                "encrypted": true,
                "value": "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456",
                "encryption": {
                  "algorithm": "AES-128-CBC",
                  "keyId": "key-vault://staging/db-test-key-2024",
                  "parameters": {
                    "keyLength": 128,
                    "mode": "cbc",
                    "metadata": {
                      "created": "2024-03-20T10:15:00Z",
                      "rotationPolicy": {
                        "enabled": false,
                        "intervalDays": 30,
                        "lastRotated": "2024-11-10T09:00:00Z",
                        "nextRotation": null
                      }
                    }
                  }
                }
              }
            },
            "connection": {
              "pool": {
                "min": 5,
                "max": 50,
                "idleTimeout": 60000,
                "maxLifetime": 300000
              }
            }
          },
          "backup": {
            "host": "db-backup.internal",
            "port": 3306,
            "readOnly": true,
            "schedule": "0 2 * * *"
          }
        },
        "cache": {
          "redis": {
            "standalone": {
              "host": "redis-single.internal",
              "port": 6380,
              "tls": {
                "enabled": false
              },
              "auth": {
                "required": true,
                "password": {
                  "encrypted": false,
                  "value": "redis-test-password"
                }
              }
            }
          },
          "memoryCache": {
            "size": "512MB",
            "ttl": 3600
          }
        }
      },
      "monitoring": {
        "prometheus": {
          "enabled": false,
          "endpoint": "/prometheus",
          "collectors": {
            "system": true,
            "application": false,
            "business": false
          }
        },
        "datadog": {
          "enabled": true,
          "apiKey": "datadog-api-key-placeholder"
        }
      },
      "features": {
        "experimental": true,
        "featureFlags": ["new-ui", "ai-integration", "beta-api"]
      }
    }
  }, null, 2);
}
</script>
<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-area {
  display: grid;
  grid-template-columns: 1fr 110px 1fr;
  gap: 16px;
  min-height: 420px;
}

.editor-panel {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-primary);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.panel-title {
  font-weight: 700;
}

.action-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
}

.stats-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  align-items: center;
}

.stat-item {
  width: 86px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: var(--radius-md);
  padding: 6px 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
}

.stat-item.added {
  border-color: var(--diff-added);
}

.stat-item.removed {
  border-color: var(--diff-removed);
}

.stat-item.modified {
  border-color: var(--diff-modified);
}

.hint {
  color: var(--text-secondary);
  font-size: 12px;
  text-align: center;
}

.diff-viewer {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  padding: 10px;
}
</style>
