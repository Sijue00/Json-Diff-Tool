<template>
  <el-dialog v-model="visible" title="设置" width="620px" :close-on-click-modal="false" @close="handleClose">
    <div class="settings-content">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="对比选项" name="compare">
          <el-form label-width="120px">
            <el-form-item label="忽略顺序">
              <el-switch v-model="localSettings.ignoreOrder" active-text="启用" inactive-text="禁用" />
              <div class="form-tip">忽略数组和对象属性的顺序差异</div>
            </el-form-item>
            <el-form-item label="忽略空白">
              <el-switch v-model="localSettings.ignoreWhitespace" active-text="启用" inactive-text="禁用" />
              <div class="form-tip">忽略字符串中的空白字符差异</div>
            </el-form-item>
            <el-form-item label="大小写敏感">
              <el-switch v-model="localSettings.caseSensitive" active-text="启用" inactive-text="禁用" />
              <div class="form-tip">字符串比较时是否区分大小写</div>
            </el-form-item>
            <el-form-item label="最大深度">
              <el-input-number v-model="localSettings.maxDepth" :min="1" :max="1000" controls-position="right" />
              <div class="form-tip">JSON 递归比较最大深度</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="显示选项" name="display">
          <el-form label-width="120px">
            <el-form-item label="主题">
              <el-radio-group v-model="localSettings.theme">
                <el-radio value="light">亮色</el-radio>
                <el-radio value="dark">暗色</el-radio>
                <el-radio value="auto">跟随系统</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="字体大小">
              <el-slider v-model="localSettings.fontSize" :min="12" :max="20" :step="1" show-input />
              <div class="form-tip">编辑器字体大小（px）</div>
            </el-form-item>
            <el-form-item label="显示行号">
              <el-switch v-model="localSettings.showLineNumbers" active-text="显示" inactive-text="隐藏" />
            </el-form-item>
            <el-form-item label="自动换行">
              <el-switch v-model="localSettings.wordWrap" active-text="启用" inactive-text="禁用" />
            </el-form-item>
            <el-form-item label="Minimap">
              <el-switch v-model="localSettings.minimap" active-text="显示" inactive-text="隐藏" />
              <div class="form-tip">编辑器右侧缩略图</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="功能选项" name="features">
          <el-form label-width="120px">
            <el-form-item label="实时对比">
              <el-switch v-model="localSettings.realtimeCompare" active-text="启用" inactive-text="禁用" />
              <div class="form-tip">输入时自动触发对比</div>
            </el-form-item>
            <el-form-item label="自动保存">
              <el-switch v-model="localSettings.autoSave" active-text="启用" inactive-text="禁用" />
            </el-form-item>
            <el-form-item label="历史记录数量">
              <el-input-number v-model="localSettings.maxHistory" :min="0" :max="50" controls-position="right" />
            </el-form-item>
            <el-form-item label="文件大小限制">
              <el-input-number v-model="localSettings.maxFileSize" :min="1" :max="100" controls-position="right" />
              <div class="form-tip">最大文件大小（MB）</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  settings: { type: Object, required: true }
})
const emit = defineEmits(['update:modelValue', 'update:settings'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const activeTab = ref('compare')
const localSettings = reactive({ ...props.settings })

watch(
  () => props.settings,
  (v) => Object.assign(localSettings, v || {}),
  { deep: true, immediate: true }
)

function handleSave() {
  emit('update:settings', { ...localSettings })
  visible.value = false
}

function handleClose() {
  visible.value = false
}

function handleReset() {
  Object.assign(localSettings, {
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
}
</script>
<style scoped>
.form-tip {
  margin-top: 6px;
  color: var(--text-secondary);
  font-size: 12px;
}
</style>
