<template>
  <div class="editor-wrap">
    <div ref="containerRef" class="editor"></div>

    <div v-if="error" class="error-bar">
      <el-icon>
        <WarningFilled />
      </el-icon>
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as monaco from 'monaco-editor'
import { ElMessage } from 'element-plus'
import { parseTree, findNodeAtLocation } from 'jsonc-parser'

const props = defineProps({
  modelValue: { type: String, default: '' },
  error: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'change'])

const containerRef = ref(null)
let editor = null
let suppress = false

// 用于高亮
let decorationIds = []
let clearTimer = null

function clearHighlight() {
  if (!editor) return
  decorationIds = editor.deltaDecorations(decorationIds, [])
  if (clearTimer) {
    clearTimeout(clearTimer)
    clearTimer = null
  }
}

/**
 * "$.arr[1].x" => ["arr", 1, "x"]
 */
function pathToSegments(path) {
  if (!path || path === '$') return []
  let p = path

  if (p.startsWith('$.')) p = p.slice(2)
  else if (p.startsWith('$')) p = p.slice(1)
  if (!p) return []

  const segs = []
  const re = /([^\.\[\]]+)|\[(\d+)\]/g
  let m
  while ((m = re.exec(p))) {
    if (m[1]) segs.push(m[1])
    else segs.push(Number(m[2]))
  }
  return segs
}

/**
 * 在编辑器中定位并高亮某个 JSONPath
 * - 滚动到位置
 * - 选中范围
 * - 加 whole-line 高亮（肉眼可见）
 */
function highlightPath(path, { autoClearMs = 2500 } = {}) {
  if (!editor) return
  const model = editor.getModel()
  if (!model) return

  const text = model.getValue()
  const errors = []
  const root = parseTree(text, errors, { allowTrailingComma: true })

  if (!root || errors.length) {
    // JSON 本身不合法时没法定位，给个轻提示
    ElMessage.warning('当前 JSON 无法解析，无法定位到差异位置')
    return
  }

  // 后端可能会给 "$.arr._length" 这种定位不到具体节点的路径
  // 这里做降级：找不到就逐级退到父节点
  let segs = pathToSegments(path)
  let node = null
  for (let i = segs.length; i >= 0; i--) {
    const trySegs = segs.slice(0, i)
    node = findNodeAtLocation(root, trySegs)
    if (node) break
  }
  if (!node) {
    ElMessage.info(`未找到可定位节点：${path}`)
    return
  }

  // 用 offset/length 转成 monaco 的 position
  const start = model.getPositionAt(node.offset)
  const end = model.getPositionAt(node.offset + Math.max(node.length, 1))


  editor.revealPositionInCenter(start)
  editor.setSelection(
    new monaco.Range(start.lineNumber, start.column, end.lineNumber, end.column)
  )
  editor.focus()

  // whole-line 高亮
  decorationIds = editor.deltaDecorations(decorationIds, [
    {
      range: new monaco.Range(start.lineNumber, 1, end.lineNumber, 1),
      options: { isWholeLine: true, className: 'diff-highlight-line' }
    }
  ])

  if (autoClearMs > 0) {
    if (clearTimer) clearTimeout(clearTimer)
    clearTimer = setTimeout(() => clearHighlight(), autoClearMs)
  }
}

// 暴露给父组件调用
defineExpose({
  highlightPath,
  clearHighlight
})

onMounted(() => {
  try {
    // ✅ 最小化错误屏蔽：只屏蔽 Monaco Worker 噪音
    if (import.meta.env.DEV) {
      const originalError = console.error
      console.error = function (...args) {
        const errMsg = String(args[0]?.stack || args[0] || '')
        if (errMsg.includes('Unexpected usage') && errMsg.includes('editorSimpleWorker')) {
          return // 只屏蔽这个特定的错误
        }
        originalError.apply(console, args)
      }
    }

    editor = monaco.editor.create(containerRef.value, {
      value: props.modelValue ?? '',
      language: 'json',
      automaticLayout: true,
      minimap: { enabled: false },
      fontSize: 14,
      tabSize: 2
    })

    editor.onDidChangeModelContent(() => {
      if (suppress) return
      const v = editor.getValue()
      emit('update:modelValue', v)
      emit('change', v)

      // 用户自己编辑时，清一下旧高亮（不然会残留误导）
      clearHighlight()
    })
  } catch (e) {
    ElMessage.error('Monaco 初始化失败：' + (e?.message || 'unknown'))
  }
})

watch(
  () => props.modelValue,
  (v) => {
    if (!editor) return
    const cur = editor.getValue()
    if (v === cur) return
    suppress = true
    editor.setValue(v ?? '')
    suppress = false

    // 外部刷新内容时也清理高亮，避免错位
    clearHighlight()
  }
)

onBeforeUnmount(() => {
  if (clearTimer) clearTimeout(clearTimer)
  clearTimer = null
  editor?.dispose()
  editor = null
})
</script>

<style scoped>
.editor-wrap {
  position: relative;
  flex: 1;
  min-height: 360px;
}

.editor {
  height: 100%;
  min-height: 360px;
}

.error-bar {
  position: absolute;
  left: 8px;
  right: 8px;
  bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(239, 68, 68, 0.12);
  border: 1px solid rgba(239, 68, 68, 0.35);
  color: var(--text-primary);
  font-size: 12px;
}

/* 高亮行（scoped 下需要 :deep 才能作用到 monaco 内部渲染） */
:deep(.diff-highlight-line) {
  background: rgba(249, 115, 22, 0.14) !important;
}
</style>
