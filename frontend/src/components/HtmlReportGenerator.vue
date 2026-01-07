<template>
  <div class="html-report-container">
    <!-- 统计信息 -->
    <div v-if="config.includeStats" class="stats-grid">
      <div v-for="stat in statItems" :key="stat.type" class="stat-card" :class="`stat-${stat.type}`">
        <el-icon :size="24">
          <component :is="stat.icon" />
        </el-icon>
        <span class="stat-label">{{ stat.label }}</span>
        <span class="stat-value">{{ stat.value }}</span>
      </div>
    </div>

    <!-- 差异卡片列表 -->
    <div class="diff-list">
      <h2 class="section-title">📋 差异明细 (共 {{ stats.total }} 处)</h2>

      <div v-for="(diff, index) in differences" :key="index" class="diff-card">
        <!-- 第一层：路径和操作类型 -->
        <div class="diff-header">
          <div class="path-section">
            <el-icon size="14" color="#409EFF">
              <Position />
            </el-icon>
            <span class="path-text" :title="diff.path">
              {{ config.includePaths ? diff.path : '(路径已隐藏)' }}
            </span>
          </div>
          <el-tag :type="typeMap[diff.type]" size="default" effect="dark" class="type-tag">
            {{ diff.type.toUpperCase() }}
          </el-tag>
        </div>

        <!-- 第二层：原值和新值 -->
        <div class="diff-body">
          <div class="value-column old-value" v-if="diff.oldValue !== undefined">
            <div class="column-header">
              <el-icon size="12">
                <Delete />
              </el-icon>
              <span>原始值</span>
            </div>
            <pre class="value-content">{{ formatValue(diff.oldValue) }}</pre>
          </div>

          <div class="value-column new-value" v-if="diff.newValue !== undefined">
            <div class="column-header">
              <el-icon size="12">
                <Plus />
              </el-icon>
              <span>新值</span>
            </div>
            <pre class="value-content">{{ formatValue(diff.newValue) }}</pre>
          </div>
        </div>
      </div>
    </div>

    <!-- 原始数据对比 -->
    <div v-if="config.includeOriginal" class="original-section">
      <h2 class="section-title">📄 原始数据</h2>
      <div class="original-grid">
        <div class="original-panel">
          <div class="panel-header left-header">
            <el-icon>
              <Document />
            </el-icon>
            <span>源数据 (Left)</span>
          </div>
          <pre class="original-content">{{ leftJson }}</pre>
        </div>
        <div class="original-panel">
          <div class="panel-header right-header">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span>目标数据 (Right)</span>
          </div>
          <pre class="original-content">{{ rightJson }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Position, Delete, Plus, Document, DocumentChecked } from '@element-plus/icons-vue'
import { jsonUtils } from '../utils/jsonUtils'

const props = defineProps({
  differences: { type: Array, default: () => [] },
  leftJson: { type: String, default: '' },
  rightJson: { type: String, default: '' },
  config: { type: Object, default: () => ({}) },
  stats: { type: Object, default: () => ({}) }
})

const typeMap = {
  added: 'success',
  removed: 'danger',
  modified: 'warning'
}

const statItems = computed(() => [
  { type: 'added', label: '新增', value: props.stats.added || 0, icon: 'Plus' },
  { type: 'removed', label: '删除', value: props.stats.removed || 0, icon: 'Delete' },
  { type: 'modified', label: '修改', value: props.stats.modified || 0, icon: 'Edit' },
  { type: 'total', label: '总计', value: props.stats.total || 0, icon: 'List' }
])

function formatValue(value) {
  const str = jsonUtils.safeStringify(value, null, props.config.prettyPrint ? 2 : 0)
  return props.config.prettyPrint ? jsonUtils.formatJson(str) : jsonUtils.compressJson(str)
}
</script>

<style scoped>
.html-report-container {
  padding: 24px;
  background: #ffffff;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  color: white;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-added {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-removed {
  background: linear-gradient(135deg, #f56c6c 0%, #fb7a7a 100%);
}

.stat-modified {
  background: linear-gradient(135deg, #e6a23c 0%, #ee9c3a 100%);
}

.stat-total {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-label {
  font-size: 13px;
  margin-top: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 4px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
}

.diff-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s;
  background: white;
}

.diff-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e0e0e0;
}

.path-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.path-text {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #409eff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-tag {
  font-weight: bold;
  min-width: 60px;
  text-align: center;
}

.diff-body {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.value-column {
  padding: 16px;
  position: relative;
}

.value-column:last-child::before {
  content: '';
  position: absolute;
  left: 0;
  top: 16px;
  bottom: 16px;
  width: 1px;
  background: #e0e0e0;
}

.old-value {
  background: #fff5f5;
}

.new-value {
  background: #f6ffed;
}

.column-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #666;
}

.value-content {
  margin: 0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.original-section {
  margin-top: 32px;
}

.original-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.original-panel {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.original-panel h4 {
  margin: 0;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
}

.left-header {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.right-header {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.original-content {
  padding: 20px;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background: #fafafa;
}

@media (max-width: 768px) {
  .diff-body {
    grid-template-columns: 1fr;
  }

  .value-column:last-child::before {
    display: none;
  }

  .original-grid {
    grid-template-columns: 1fr;
  }
}
</style>