import { jsonUtils } from './jsonUtils'

// 计算统计
function calculateStats(diffs) {
  return {
    total: diffs.length,
    added: diffs.filter(d => d.type === 'added').length,
    removed: diffs.filter(d => d.type === 'removed').length,
    modified: diffs.filter(d => d.type === 'modified').length
  }
}

// 生成Markdown
function generateMarkdown(diffs, leftJson, rightJson, config) {
  const stats = calculateStats(diffs)
  const timestamp = new Date().toLocaleString('zh-CN')
  
  let content = `# JSON 差异报告\n\n**生成时间**: ${timestamp}\n\n`
  
  if (config.includeStats) {
    content += `## 📊 统计信息\n\n| 类型 | 数量 |\n|------|------|\n`
    content += `| 新增 | ${stats.added} 项 |\n| 删除 | ${stats.removed} 项 |\n| 修改 | ${stats.modified} 项 |\n| **总计** | **${stats.total} 项** |\n\n`
  }

  content += `## 📋 差异详情\n\n`

  diffs.forEach((diff, index) => {
    const path = config.includePaths ? diff.path : '(路径已隐藏)'
    content += `### ${index + 1}. ${path}\n\n**操作类型**: \`${diff.type.toUpperCase()}\`\n\n`
    
    if (diff.oldValue !== undefined) {
      const oldVal = config.prettyPrint 
        ? jsonUtils.formatJson(jsonUtils.safeStringify(diff.oldValue))
        : jsonUtils.compressJson(jsonUtils.safeStringify(diff.oldValue))
      content += `**原值**:\n\`\`\`json\n${oldVal}\n\`\`\`\n\n`
    }
    
    if (diff.newValue !== undefined) {
      const newVal = config.prettyPrint 
        ? jsonUtils.formatJson(jsonUtils.safeStringify(diff.newValue))
        : jsonUtils.compressJson(jsonUtils.safeStringify(diff.newValue))
      content += `**新值**:\n\`\`\`json\n${newVal}\n\`\`\`\n\n`
    }
    
    content += `---\n\n`
  })

  if (config.includeOriginal) {
    content += `## 📄 原始数据对比\n\n### 源数据 (Left)\n\`\`\`json\n${leftJson || '// 空数据'}\n\`\`\`\n\n### 目标数据 (Right)\n\`\`\`json\n${rightJson || '// 空数据'}\n\`\`\`\n\n`
  }

  content += `*由 JSON Diff 工具生成*`
  return content
}

// 生成JSON
function generateJson(diffs, leftJson, rightJson, config) {
  const output = {
    metadata: {
      generatedAt: new Date().toISOString(),
      version: '1.0.0'
    }
  }

  if (config.includeStats) output.summary = calculateStats(diffs)

  output.differences = diffs.map(diff => {
    const item = { type: diff.type }
    if (config.includePaths) item.path = diff.path
    if (diff.oldValue !== undefined) item.oldValue = diff.oldValue
    if (diff.newValue !== undefined) item.newValue = diff.newValue
    return item
  })

  if (config.includeOriginal) {
    output.originalData = {
      left: jsonUtils.safeParse(leftJson),
      right: jsonUtils.safeParse(rightJson)
    }
  }

  return jsonUtils.safeStringify(output, null, config.prettyPrint ? 2 : 0)
}

// 生成CSV
function generateCsv(diffs, leftJson, rightJson, config) {
  const headers = config.includePaths ? ['Path', 'Type', 'OldValue', 'NewValue'] : ['Type', 'OldValue', 'NewValue']
  
  const rows = diffs.map(diff => {
    const row = []
    if (config.includePaths) row.push(jsonUtils.escapeCsvValue(diff.path))
    row.push(
      diff.type,
      diff.oldValue !== undefined ? jsonUtils.escapeCsvValue(jsonUtils.safeStringify(diff.oldValue, null, 0)) : '',
      diff.newValue !== undefined ? jsonUtils.escapeCsvValue(jsonUtils.safeStringify(diff.newValue, null, 0)) : ''
    )
    return row.join(',')
  })

  let csv = headers.join(',') + '\n' + rows.join('\n')

  if (config.includeStats) {
    const stats = calculateStats(diffs)
    csv += `\n\n统计信息\n新增,${stats.added}\n删除,${stats.removed}\n修改,${stats.modified}\n总计,${stats.total}`
  }

  if (config.includeOriginal) {
    csv += `\n\n原始数据\nLeft,${jsonUtils.escapeCsvValue(leftJson)}\nRight,${jsonUtils.escapeCsvValue(rightJson)}`
  }

  return csv
}

// 生成HTML字符串 - 卡片式布局
function generateHtmlString(diffs, leftJson, rightJson, config) {
  const stats = calculateStats(diffs)
  const timestamp = new Date().toLocaleString('zh-CN')
  
  const diffCards = diffs.map((diff, index) => {
    const path = config.includePaths ? jsonUtils.escapeHtml(diff.path) : '(路径已隐藏)';
    const typeClass = `type-${diff.type}`;
    const oldVal = diff.oldValue !== undefined 
      ? jsonUtils.escapeHtml(jsonUtils.safeStringify(diff.oldValue, null, config.prettyPrint ? 2 : 0))
      : '';
    const newVal = diff.newValue !== undefined
      ? jsonUtils.escapeHtml(jsonUtils.safeStringify(diff.newValue, null, config.prettyPrint ? 2 : 0))
      : '';
    
    return `
      <div class="diff-card">
        <div class="diff-header">
          <div class="path-section"><span class="path-text">${path}</span></div>
          <span class="type-tag ${typeClass}">${diff.type.toUpperCase()}</span>
        </div>
        <div class="diff-body">
          ${oldVal ? `<div class="value-panel old-value"><div class="panel-label">原值</div><pre>${oldVal}</pre></div>` : ''}
          ${newVal ? `<div class="value-panel new-value"><div class="panel-label">新值</div><pre>${newVal}</pre></div>` : ''}
        </div>
      </div>
    `;
  }).join('');

  const statsHtml = config.includeStats ? `
    <div class="stats-container">
      <div class="stat-item added"><span class="stat-label">新增</span><span class="stat-value">${stats.added}</span></div>
      <div class="stat-item removed"><span class="stat-label">删除</span><span class="stat-value">${stats.removed}</span></div>
      <div class="stat-item modified"><span class="stat-label">修改</span><span class="stat-value">${stats.modified}</span></div>
      <div class="stat-item total"><span class="stat-label">总计</span><span class="stat-value">${stats.total}</span></div>
    </div>
  ` : '';

  const originalHtml = config.includeOriginal ? `
    <div class="original-section">
      <h3>📄 原始数据对比</h3>
      <div class="original-grid">
        <div class="original-panel"><h4>源数据 (Left)</h4><pre class="original-json">${jsonUtils.escapeHtml(leftJson)}</pre></div>
        <div class="original-panel"><h4>目标数据 (Right)</h4><pre class="original-json">${jsonUtils.escapeHtml(rightJson)}</pre></div>
      </div>
    </div>
  ` : '';

  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JSON 差异报告</title>
  <style>
    *{margin:0;padding:0;box-sizing:border-box}
    body{font-family:'Segoe UI','Microsoft YaHei',sans-serif;line-height:1.6;color:#333;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;padding:20px}
    .report-container{max-width:1200px;margin:0 auto;background:white;border-radius:12px;box-shadow:0 10px 40px rgba(0,0,0,.1);overflow:hidden}
    .report-header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:30px;text-align:center}
    .report-header h1{font-size:28px;margin-bottom:10px;font-weight:600}
    .report-header .timestamp{opacity:.9;font-size:14px}
    .report-content{padding:30px}
    .stats-container{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:20px;margin:30px 0;padding:20px;background:#f8f9fa;border-radius:8px;border:1px solid #e9ecef}
    .stat-item{text-align:center;padding:15px;border-radius:6px;background:white;box-shadow:0 2px 8px rgba(0,0,0,.05)}
    .stat-item.added{border-top:4px solid #67c23a}
    .stat-item.removed{border-top:4px solid #f56c6c}
    .stat-item.modified{border-top:4px solid #e6a23c}
    .stat-item.total{border-top:4px solid #409eff}
    .stat-label{display:block;font-size:14px;color:#666;margin-bottom:5px}
    .stat-value{display:block;font-size:24px;font-weight:bold}
    .diff-list{margin-top:40px}
    .diff-list h2{color:#2c3e50;margin-bottom:20px;padding-bottom:10px;border-bottom:2px solid #eaeaea}
    .diff-card{border:1px solid #e0e0e0;border-radius:8px;margin-bottom:16px;overflow:hidden;transition:all .3s;background:#fff}
    .diff-card:hover{box-shadow:0 4px 16px rgba(0,0,0,.1);border-color:#409eff}
    .diff-header{display:flex;justify-content:space-between;align-items:center;padding:12px 16px;background:#f5f7fa;border-bottom:1px solid #e0e0e0;gap:12px}
    .path-section{flex:1;overflow:hidden}
    .path-text{font-family:'Consolas',monospace;font-size:14px;color:#409eff;font-weight:500;word-break:break-all;line-height:1.4}
    .type-tag{padding:4px 12px;border-radius:4px;font-size:12px;font-weight:bold;color:white;white-space:nowrap;flex-shrink:0}
    .type-added{background:#67c23a}
    .type-removed{background:#f56c6c}
    .type-modified{background:#e6a23c}
    .diff-body{display:grid;grid-template-columns:1fr 1fr;gap:0}
    .value-panel{padding:16px;position:relative}
    .value-panel.old-value{background:#fff5f5}
    .value-panel.new-value{background:#f6ffed}
    .value-panel+.value-panel::before{content:'';position:absolute;left:0;top:16px;bottom:16px;width:1px;background:#e0e0e0}
    .panel-label{font-size:12px;font-weight:600;color:#666;margin-bottom:8px;text-transform:uppercase}
    .value-panel pre{margin:0;font-family:'Consolas','Monaco',monospace;font-size:13px;line-height:1.5;white-space:pre-wrap;word-break:break-all;max-height:300px;overflow-y:auto}
    .original-section{margin-top:40px}
    .original-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-top:20px}
    .original-panel{border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;background:white}
    .original-panel h4{background:#f8f9fa;padding:15px 20px;margin:0;border-bottom:1px solid #e0e0e0;font-size:16px}
    .original-json{padding:20px;margin:0;max-height:400px;overflow-y:auto;font-family:'Consolas','Monaco',monospace;font-size:13px;line-height:1.5;background:#fafafa}
    @media (max-width:768px){
      .diff-body{grid-template-columns:1fr}
      .value-panel+.value-panel::before{display:none}
      .original-grid{grid-template-columns:1fr}
      .diff-header{flex-direction:column;align-items:flex-start;gap:8px}
      .type-tag{align-self:flex-start}
    }
    .footer{text-align:center;padding:20px;color:#666;font-size:14px;border-top:1px solid #eaeaea;margin-top:30px}
  </style>
</head>
<body>
  <div class="report-container">
    <div class="report-header">
      <h1>📊 JSON 差异分析报告</h1>
      <div class="timestamp">生成时间: ${timestamp}</div>
    </div>
    <div class="report-content">${statsHtml}<div class="diff-list"><h2>📋 差异明细 (共 ${stats.total} 处)</h2>${diffCards}</div>${originalHtml}</div>
    <div class="footer">本报告由 JSON Diff 工具生成 • ${new Date().toISOString()}</div>
  </div>
</body>
</html>`;
}

export {
  generateMarkdown,
  generateJson,
  generateCsv,
  generateHtmlString
}