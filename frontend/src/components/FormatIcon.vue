<template>
  <div class="format-icon-wrapper">
    <el-icon :size="16" :color="color">
      <component :is="iconComponent" />
    </el-icon>
    <div class="format-info">
      <span class="format-name">{{ name }}</span>
      <span class="format-desc">{{ desc }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Monitor, Notebook, DataBoard, Grid } from '@element-plus/icons-vue'

const props = defineProps({
  icon: String,
  color: String,
  name: String,
  desc: String
})

const iconComponent = computed(() => {
  const map = {
    Monitor,
    Notebook,
    DataBoard,
    Grid
  }
  return map[props.icon]
})
</script>

<style scoped>
.format-icon-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}



.format-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.format-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 10px;
}
</style>