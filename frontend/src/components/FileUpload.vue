<template>
  <div ref="dropZoneRef" class="drop-zone" :class="{ 'is-dragover': isDragOver }" @drop.prevent="handleDrop"
    @dragover.prevent="handleDragOver" @dragleave.prevent="handleDragLeave">
    <input ref="fileInputRef" type="file" class="hidden-input" accept=".json,.yaml,.yml,.xml,.txt"
      @change="handleFilePick" />

    <el-tooltip content="点击上传或拖拽文件到这里" placement="top">
      <el-button size="small" circle @click="handleUploadClick">
        <el-icon>
          <Upload />
        </el-icon>
      </el-button>
    </el-tooltip>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  side: { type: String, required: true } // left | right
})

const emit = defineEmits(['file-loaded'])

const isDragOver = ref(false)
const dropZoneRef = ref(null)
const fileInputRef = ref(null)

function handleUploadClick() {
  fileInputRef.value?.click()
}

function handleDragOver() {
  isDragOver.value = true
}
function handleDragLeave() {
  isDragOver.value = false
}

function readFile(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result ?? ''))
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsText(file)
  })
}

async function handleDrop(e) {
  isDragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  await loadFile(file)
}

async function handleFilePick(e) {
  const file = e.target?.files?.[0]
  // 允许重复选择同一个文件
  e.target.value = ''
  if (!file) return
  await loadFile(file)
}

async function loadFile(file) {
  try {
    const text = await readFile(file)
    emit('file-loaded', { side: props.side, content: text, filename: file.name })
    ElMessage.success(`已加载: ${file.name}`)
  } catch (err) {
    ElMessage.error(err?.message || '加载文件失败')
  }
}
</script>
<style scoped>
.drop-zone {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.is-dragover {
  outline: 2px dashed var(--accent-color);
  outline-offset: 2px;
  border-radius: 999px;
}

.hidden-input {
  display: none;
}
</style>
