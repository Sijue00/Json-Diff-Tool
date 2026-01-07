<template>
  <el-container class="main-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo">
          <el-icon><DataAnalysis /></el-icon>
          <span class="logo-text">JSON对比工具</span>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="toggleTheme">
              <el-icon><View /></el-icon>
              {{ isDark ? '亮色' : '暗色' }}
            </el-button>
            <el-button @click="showAbout = true">
              <el-icon><InfoFilled /></el-icon>
              关于
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <HomePage />
    </el-main>
    <el-dialog
      v-model="showAbout"
      title="关于 JSON 对比工具"
      width="620px"
      :close-on-click-modal="false"
    >
      <div class="about-content">
        <h3>JSON对比工具 v1.0.0</h3>
        <p>结构化 JSON 差异对比工具，支持实时对比、差异导航、统计、导出等功能。</p>
        <h4>主要功能</h4>
        <ul>
          <li>结构化 JSON 对比（非文本 diff）</li>
          <li>实时对比 / 手动对比</li>
          <li>差异导航树 + 统计图</li>
          <li>导出 HTML/Markdown/JSON/CSV</li>
        </ul>
        <h4>技术栈</h4>
        <p>Vue 3 + Element Plus +  Spring Boot</p>
      </div>
    </el-dialog>
  </el-container>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import HomePage from './views/HomePage.vue'

const showAbout = ref(false)
const isDark = ref(false)

const applyTheme = (dark) => {
  isDark.value = dark
  document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light')
}

const toggleTheme = () => applyTheme(!isDark.value)

onMounted(() => {
  // 默认跟随系统（你也可以改成 always light）
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)')?.matches
  applyTheme(!!prefersDark)
})
</script>
<style scoped>
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-text {
  font-weight: 700;
}
.about-content{
  flex: auto;
}
.about-content h3 {
  margin: 0 0 8px;
}
</style>
