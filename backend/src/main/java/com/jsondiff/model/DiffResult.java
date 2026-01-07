package com.jsondiff.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

/**
 * JSON差异对比结果模型
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResult {
    
    /**
     * 对比时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 差异总数
     */
    private int totalDifferences;
    
    /**
     * 新增项数量
     */
    private int addedCount;
    
    /**
     * 删除项数量
     */
    private int removedCount;
    
    /**
     * 修改项数量
     */
    private int modifiedCount;
    
    /**
     * 差异详情列表
     */
    private List<DiffItem> differences;
    
    /**
     * 原始左侧JSON
     */
    private Object leftData;
    
    /**
     * 原始右侧JSON
     */
    private Object rightData;
    
    /**
     * 对比设置
     */
    private CompareSettings settings;
    
    /**
     * 对比耗时（毫秒）
     */
    private long duration;
    
    /**
     * 添加差异项
     */
    public void addDifference(DiffItem diff) {
        this.totalDifferences++;

        switch (diff.getType()) {
            case ADDED:
                this.addedCount++;
                break;
            case REMOVED:
                this.removedCount++;
                break;
            case MODIFIED:
                this.modifiedCount++;
                break;
        }
    }

    /**
     * 获取统计信息
     */
    public DiffStats getStats() {
        return new DiffStats(totalDifferences, addedCount, removedCount, modifiedCount);
    }
}