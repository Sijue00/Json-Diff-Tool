<template>
  <div class="navigator">
    <div v-if="!hasDiff" class="empty-state">
      <el-empty description="暂无差异数据" />
    </div>
    <div v-else class="navigator-tree">
      <el-tree ref="treeRef" :data="treeData" :props="treeProps" :default-expand-all="true"
        @node-click="handleNodeClick">
        <template #default="{ data }">
          <div class="tree-node" :class="`tree-node-${data.type}`">
            <div class="node-icon">
              <el-icon v-if="data.type === 'added'" color="#10b981">
                <Plus />
              </el-icon>
              <el-icon v-else-if="data.type === 'removed'" color="#ef4444">
                <Minus />
              </el-icon>
              <el-icon v-else-if="data.type === 'modified'" color="#f59e0b">
                <Edit />
              </el-icon>
              <el-icon v-else color="#64748b">
                <Folder />
              </el-icon>
            </div>
            <div class="node-content">
              <div class="node-path">{{ data.label }}</div>
              <div class="node-details" v-if="data.details">
                <span v-if="data.oldValue !== undefined" class="old-value">
                  旧: {{ formatValue(data.oldValue) }}
                </span>
                <span v-if="data.newValue !== undefined" class="new-value">
                  新: {{ formatValue(data.newValue) }}
                </span>
              </div>
            </div>
            <div class="node-stats" v-if="data.children?.length">
              <el-tag size="small" type="info">{{ data.children.length }}</el-tag>
            </div>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
</template>
<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  diffResult: { type: Object, default: null }
})
const emit = defineEmits(['navigate'])

const treeRef = ref(null)
const treeData = ref([])

const treeProps = {
  children: 'children',
  label: 'label'
}

const hasDiff = computed(() => Array.isArray(props.diffResult?.differences) && props.diffResult.differences.length > 0)

function formatValue(v) {
  if (typeof v === 'string') return v.length > 50 ? v.slice(0, 50) + '…' : v
  try {
    return JSON.stringify(v)
  } catch {
    return String(v)
  }
}

function buildTree(differences) {
  const root = { label: 'root', type: 'folder', children: [] }

  for (const diff of differences) {
    const path = String(diff.path ?? '').replace(/^\$\.?/, '')
    const segs = path ? path.split('.').filter(Boolean) : ['(root)']

    let cur = root
    for (let i = 0; i < segs.length; i++) {
      const seg = segs[i]
      const isLeaf = i === segs.length - 1

      cur.children ||= []
      let next = cur.children.find(n => n.label === seg && n.type === 'folder')
      if (!next) {
        next = { label: seg, type: 'folder', children: [] }
        cur.children.push(next)
      }

      if (isLeaf) {
        // 差异节点
        next.children ||= []
        next.children.push({
          label: seg,
          type: diff.type,
          details: true,
          path: diff.path,  // 必须保留完整JSONPath
          oldValue: diff.oldValue,
          newValue: diff.newValue,
          isLeaf: true      // 标记为叶子节点，方便判断
        })
      }

      cur = next
    }
  }

  return root.children
}

function handleNodeClick(data) {
  if (data?.path && data.isLeaf) {  // 只处理叶子差异节点
    emit('navigate', { path: data.path, diff: data })
  }
}

watch(
  () => props.diffResult,
  (val) => {
    const diffs = val?.differences || []
    treeData.value = buildTree(diffs)
  },
  { immediate: true, deep: true }
)
</script>
<style scoped>
.navigator {
  padding: 8px;
}

/* DiffNavigator.vue */
.navigator-tree {
  padding: 8px 12px;
}

/* 关键：解除 el-tree 默认的固定行高 */
:deep(.el-tree-node__content) {
  height: auto;
  align-items: flex-start;
  padding: 6px 0;
}

.empty-state {
  padding: 20px 0;
}

.tree-node {
  width: 100%;
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 6px 4px;
}

.node-icon {
  width: 22px;
  display: flex;
  justify-content: center;
  margin-top: 2px;
}

.node-content {
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.node-path {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.node-details {
  margin-top: 3px;
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.old-value {
  color: #ef4444;
}

.new-value {
  color: #10b981;
}
</style>
