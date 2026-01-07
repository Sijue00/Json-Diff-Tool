<template>
  <el-dialog v-model="visible" title="导出对比结果" width="900px" :close-on-click-modal="false" :before-close="handleClose">
    <div class="export-content">
      <!-- 格式选择 -->
      <div class="export-section">
        <div class="section-header">
          <el-icon>
            <Files />
          </el-icon>
          <span class="section-title">选择导出格式</span>
        </div>
        <el-radio-group v-model="exportFormat" class="format-grid">
          <el-radio value="html" class="format-card">
            <format-icon icon="Monitor" color="#409EFF" name="HTML报告" desc="带样式的完整网页报告(不支持预览)" />
          </el-radio>
          <el-radio value="markdown" class="format-card">
            <format-icon icon="Notebook" color="#67C23A" name="Markdown" desc="简洁的文本格式" />
          </el-radio>
          <el-radio value="json" class="format-card">
            <format-icon icon="DataBoard" color="#E6A23C" name="JSON差异" desc="机器可读的结构化数据" />
          </el-radio>
          <el-radio value="csv" class="format-card">
            <format-icon icon="Grid" color="#F56C6C" name="CSV表格" desc="适合Excel分析的表格数据" />
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 导出配置 -->
      <div class="export-section">
        <div class="section-header">
          <el-icon>
            <Setting />
          </el-icon>
          <span class="section-title">导出配置</span>
        </div>
        <el-form :model="exportConfig" label-width="120px" class="config-form">
          <div class="config-grid">
            <el-form-item label="包含统计">
              <el-switch v-model="exportConfig.includeStats" active-text="是" inactive-text="否" />
            </el-form-item>
            <el-form-item label="包含原始数据">
              <el-switch v-model="exportConfig.includeOriginal" active-text="是" inactive-text="否" />
            </el-form-item>
            <el-form-item label="包含完整路径">
              <el-switch v-model="exportConfig.includePaths" active-text="是" inactive-text="否" />
            </el-form-item>
            <el-form-item label="格式化输出">
              <el-switch v-model="exportConfig.prettyPrint" active-text="是" inactive-text="否" />
            </el-form-item>
          </div>

          <el-form-item label="文件名">
            <div class="filename-input-group">
              <el-input v-model="exportConfig.filename" placeholder="留空使用默认名称" clearable class="filename-input" />
              <el-button type="info" plain @click="resetFilename" :icon="Refresh" title="重置为默认文件名" />
            </div>
            <div class="filename-tip">
              默认文件名: <code>{{ defaultFilename }}.{{ getExt() }}</code>
            </div>
          </el-form-item>

          <el-form-item label="字符编码">
            <el-select v-model="exportConfig.encoding" placeholder="选择编码">
              <el-option label="UTF-8 (推荐)" value="utf-8" />
              <el-option label="UTF-8 with BOM" value="utf-8-bom" />
              <el-option label="GBK (中文Windows)" value="gbk" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 实时预览HTML预览会卡死，故去掉掉-->
      <div class="export-section preview-section" v-if="showPreview && exportFormat !== 'html'">
        <div class="section-header">
          <el-icon>
            <View />
          </el-icon>
          <span class="section-title">实时预览</span>
          <el-tag type="info" size="small">
            {{ previewStats.lines }} 行, {{ previewStats.chars }} 字符
          </el-tag>
        </div>

        <div class="text-preview">
          <div class="preview-toolbar">
            <el-button type="primary" link @click="copyPreview" :icon="CopyDocument">
              复制内容
            </el-button>
          </div>
          <pre :class="exportFormat" class="preview-content">{{ previewContent }}</pre>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <!-- HTML格式下禁用预览按钮 -->
        <el-button @click="togglePreview" :type="showPreview ? 'info' : 'default'" :icon="showPreview ? Hide : View"
          :disabled="exportFormat === 'html'" :title="exportFormat === 'html' ? 'HTML格式暂不支持预览' : ''">
          {{ showPreview ? '隐藏预览' : '显示预览' }}
        </el-button>

        <div class="footer-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :loading="exporting" @click="handleExport" :icon="Download"
            :disabled="differences.length === 0">
            导出文件
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Files, Setting, View, Hide, Download, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { generateMarkdown, generateJson, generateCsv, generateHtmlString } from '../utils/exportGenerators'
import FormatIcon from './FormatIcon.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  diffResult: { type: Object, default: null },
  leftJson: { type: String, default: '' },
  rightJson: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v)
})

const exporting = ref(false)
const showPreview = ref(false)
const exportFormat = ref('html')

const exportConfig = ref({
  includeStats: true,
  includeOriginal: false,
  includePaths: true,
  prettyPrint: true,
  filename: '',
  encoding: 'utf-8'
})

// 差异数据
const differences = computed(() => props.diffResult?.differences || [])
const stats = computed(() => ({
  total: differences.value.length,
  added: differences.value.filter(d => d.type === 'added').length,
  removed: differences.value.filter(d => d.type === 'removed').length,
  modified: differences.value.filter(d => d.type === 'modified').length
}))

// 毫秒级文件名
const defaultFilename = computed(() => {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `json-diff-${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_` +
    `${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}_` +
    `${String(now.getMilliseconds()).padStart(3, '0')}`
})

// 预览内容（HTML格式不生成）
const previewContent = computed(() => {
  if (!showPreview.value || differences.value.length === 0 || exportFormat.value === 'html') return ''
  try {
    const content = generateExportContent()
    return typeof content === 'string' ? content : JSON.stringify(content, null, 2)
  } catch (error) {
    return `预览生成失败: ${error.message}`
  }
})

const previewStats = computed(() => {
  const content = previewContent.value
  return {
    lines: content.split('\n').length,
    chars: content.length
  }
})

// 生成导出内容
function generateExportContent() {
  const { value: diffs } = differences
  const { leftJson, rightJson } = props
  const config = exportConfig.value

  switch (exportFormat.value) {
    case 'html':
      return generateHtmlString(diffs, leftJson, rightJson, config)
    case 'markdown':
      return generateMarkdown(diffs, leftJson, rightJson, config)
    case 'json':
      return generateJson(diffs, leftJson, rightJson, config)
    case 'csv':
      return generateCsv(diffs, leftJson, rightJson, config)
    default:
      return ''
  }
}

// 导出处理
async function handleExport() {
  if (differences.value.length === 0) {
    ElMessage.warning('没有差异数据可供导出')
    return
  }

  exporting.value = true
  try {
    const content = generateExportContent()
    const blob = createBlob(content)
    const filename = `${exportConfig.value.filename || defaultFilename.value}.${getExt()}`

    downloadFile(blob, filename)

    ElMessage.success({ message: `文件导出成功: ${filename}`, duration: 3000 })
    setTimeout(() => visible.value = false, 500)
  } catch (error) {
    ElMessage.error(`导出失败: ${error.message}`)
  } finally {
    exporting.value = false
  }
}

// 创建Blob
function createBlob(content) {
  const mimeTypes = { html: 'text/html', markdown: 'text/markdown', json: 'application/json', csv: 'text/csv' }
  const mimeType = mimeTypes[exportFormat.value] || 'text/plain'
  const encoding = exportConfig.value.encoding

  let data = content
  if (encoding === 'utf-8-bom' && typeof content === 'string') {
    data = '\uFEFF' + content // UTF-8 BOM
  }

  return new Blob([data], { type: `${mimeType};charset=utf-8` })
}

// 下载文件
function downloadFile(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

// 复制预览
async function copyPreview() {
  try {
    await navigator.clipboard.writeText(previewContent.value)
    ElMessage.success('预览内容已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 工具函数
function getExt() {
  return { html: 'html', markdown: 'md', json: 'json', csv: 'csv' }[exportFormat.value] || 'txt'
}

function resetFilename() {
  exportConfig.value.filename = ''
  ElMessage.success('已重置为默认文件名')
}

// HTML格式下禁止预览
function togglePreview() {
  if (exportFormat.value === 'html') {
    ElMessage.warning('HTML 格式暂不支持预览，请直接导出')
    return
  }
  showPreview.value = !showPreview.value
}

function handleClose() {
  if (exporting.value) {
    ElMessage.warning('正在导出中，请稍候...')
    return
  }
  ElMessageBox.confirm('确定要取消导出吗？未保存的配置将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => visible.value = false).catch(() => { })
}

// 切换格式时，如果是HTML则关闭预览
watch(exportFormat, (newFormat) => {
  if (newFormat === 'html') {
    showPreview.value = false
  }
})

// 监听对话框
watch(visible, (val) => {
  if (val) {
    showPreview.value = false
    exportConfig.value.filename = ''
  }
})
</script>

<style scoped>
.export-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.export-section {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 20px;
  background: var(--el-bg-color);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.format-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.format-card {
  margin: 0 !important;
  width: 100%;
  border-radius: 8px;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.format-card:hover {
  border-color: var(--el-color-primary);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.format-card.is-checked {
  border-color: var(--el-color-primary);
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, #ffffff 100%);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.25);
  transform: translateY(-2px);
}

.format-card.is-checked::after {
  content: '✓';
  position: absolute;
  top: 8px;
  right: 8px;
  color: var(--el-color-primary);
  font-weight: bold;
  font-size: 16px;
  background: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.format-card :deep(.el-radio__input) {
  display: none;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.filename-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.filename-input {
  flex: 1;
}

.filename-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.filename-tip code {
  background: #f4f4f4;
  padding: 2px 6px;
  border-radius: 3px;
}

.preview-section :deep(.export-section) {
  padding: 0;
}

.text-preview .preview-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid var(--el-border-color-lighter);
  text-align: right;
}

.text-preview .preview-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  margin: 0;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  background: #fafafa;
}

.preview-content.html {
  color: #e06c75;
}

.preview-content.markdown {
  color: #61afef;
}

.preview-content.json {
  color: #98c379;
}

.preview-content.csv {
  color: #d19a66;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.footer-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .format-grid {
    grid-template-columns: 1fr;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }
}
</style>