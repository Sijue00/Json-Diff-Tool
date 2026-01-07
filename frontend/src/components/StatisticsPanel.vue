<template>
  <div class="stats-panel">
    <div class="charts-container">
      <div class="chart-box">
        <h4>差异类型分布</h4>
        <div ref="pieChartRef" class="chart"></div>
      </div>
      <div class="chart-box">
        <h4>类型统计</h4>
        <div ref="barChartRef" class="chart"></div>
      </div>
    </div>
    <div class="detailed-stats">
      <el-collapse v-model="activeCollapse">
        <el-collapse-item title="路径列表" name="paths">
          <el-table :data="pathStats" style="width: 100%" max-height="240">
            <el-table-column prop="path" label="路径" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="getTagType(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>
<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

// 获取父组件传值
const props = defineProps({
  diffResult: { type: Object, default: null },
  stats: { type: Object, default: () => ({ added: 0, removed: 0, modified: 0 }) }
})

const activeCollapse = ref(['paths'])  // 默认展开路径列表
const pieChartRef = ref(null)          // 饼图DOM引用
const barChartRef = ref(null)          // 柱状图DOM引用
let pie = null, bar = null             // ECharts 实例

const differences = computed(() => props.diffResult?.differences || [])

const pathStats = computed(() => differences.value.map(d => ({ path: d.path, type: d.type })))

function getTagType(type) {
  if (type === 'added') return 'success'
  if (type === 'removed') return 'danger'
  if (type === 'modified') return 'warning'
  return 'info'
}
function getTypeLabel(type) {
  if (type === 'added') return '新增'
  if (type === 'removed') return '删除'
  if (type === 'modified') return '修改'
  return type
}

function renderCharts() {
  const added = props.stats?.added ?? 0
  const removed = props.stats?.removed ?? 0
  const modified = props.stats?.modified ?? 0

  if (!pie && pieChartRef.value) pie = echarts.init(pieChartRef.value)
  if (!bar && barChartRef.value) bar = echarts.init(barChartRef.value)

  pie?.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: '70%',
        data: [
          { value: added, name: '新增' },
          { value: removed, name: '删除' },
          { value: modified, name: '修改' }
        ]
      }
    ]
  })

  bar?.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['新增', '删除', '修改'] },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: [added, removed, modified] }]
  })

  pie?.resize()
  bar?.resize()
}

onMounted(() => {
  renderCharts()
  window.addEventListener('resize', renderCharts)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', renderCharts)
  pie?.dispose()
  bar?.dispose()
  pie = null
  bar = null
})

watch(
  () => [props.diffResult, props.stats],
  () => renderCharts(),
  { deep: true }
)
</script>
<style scoped>
.stats-panel {
  padding: 10px;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.chart-box {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 10px;
  background: var(--bg-primary);
}

.chart {
  height: 260px;
}
</style>
