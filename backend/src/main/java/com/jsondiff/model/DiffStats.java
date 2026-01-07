package com.jsondiff.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 差异统计信息模型
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
public class DiffStats {
    
    /**
     * 差异总数
     */
    private int total;
    
    /**
     * 新增项数量
     */
    private int added;
    
    /**
     * 删除项数量
     */
    private int removed;
    
    /**
     * 修改项数量
     */
    private int modified;
    
    /**
     * 构造函数
     */
    public DiffStats(int total, int added, int removed, int modified) {
        this.total = total;
        this.added = added;
        this.removed = removed;
        this.modified = modified;
    }
    
    /**
     * 获取新增项百分比
     */
    public double getAddedPercentage() {
        return total > 0 ? (double) added / total * 100 : 0;
    }
    
    /**
     * 获取删除项百分比
     */
    public double getRemovedPercentage() {
        return total > 0 ? (double) removed / total * 100 : 0;
    }
    
     /**
     * 获取修改项百分比
     */
    public double getModifiedPercentage() {
        return total > 0 ? (double) modified / total * 100 : 0;
    }
    
    /**
     * 添加统计信息
     */
    public void add(DiffStats other) {
        this.total += other.total;
        this.added += other.added;
        this.removed += other.removed;
        this.modified += other.modified;
    }
    
    /**
     * 转换为字符串
     */
    @Override
    public String toString() {
        return String.format(
            "DiffStats[total=%d, added=%d, removed=%d, modified=%d]",
            total, added, removed, modified
        );
    }
}