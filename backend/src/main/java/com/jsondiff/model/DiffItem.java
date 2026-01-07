package com.jsondiff.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * 单个差异项模型
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiffItem {
    
    /**
     * 差异类型枚举
     */
    public enum DiffType {
        ADDED,      // 新增
        REMOVED,    // 删除
        MODIFIED;    // 修改

        @JsonValue
        public String toJson() {
            return name().toLowerCase();
        }
    }
    
    /**
     * JSON路径
     */
    private String path;
    
    /**
     * 差异类型
     */
    private DiffType type;
    
    /**
     * 旧值（仅修改和删除类型有）
     */
    private Object oldValue;
    
    /**
     * 新值（仅修改和新增类型有）
     */
    private Object newValue;
    
    /**
     * 路径深度
     */
    private int depth;
    
    /**
     * 左侧面板行号
     */
    private Integer leftLineNumber;
    
    /**
     * 右侧面板行号
     */
    private Integer rightLineNumber;
    
    /**
     * 父路径
     */
    private String parentPath;
    
    /**
     * 键名
     */
    private String key;
    
    /**
     * 值的类型变化（仅修改类型有）
     */
    private String typeChange;
    
    /**
     * 创建新增差异项
     */
    public static DiffItem added(String path, Object newValue) {
        DiffItem item = new DiffItem();
        item.setPath(path);
        item.setType(DiffType.ADDED);
        item.setNewValue(newValue);
        item.setDepth(calculateDepth(path));
        item.setKey(getLastKey(path));
        item.setParentPath(getParentPath(path));
        return item;
    }
    
    /**
     * 创建删除差异项
     */
    public static DiffItem removed(String path, Object oldValue) {
        DiffItem item = new DiffItem();
        item.setPath(path);
        item.setType(DiffType.REMOVED);
        item.setOldValue(oldValue);
        item.setDepth(calculateDepth(path));
        item.setKey(getLastKey(path));
        item.setParentPath(getParentPath(path));
        return item;
    }
    
    /**
     * 创建修改差异项
     */
    public static DiffItem modified(String path, Object oldValue, Object newValue) {
        DiffItem item = new DiffItem();
        item.setPath(path);
        item.setType(DiffType.MODIFIED);
        item.setOldValue(oldValue);
        item.setNewValue(newValue);
        item.setDepth(calculateDepth(path));
        item.setKey(getLastKey(path));
        item.setParentPath(getParentPath(path));
        
        // 检测类型变化
        String oldType = getValueType(oldValue);
        String newType = getValueType(newValue);
        if (!oldType.equals(newType)) {
            item.setTypeChange(oldType + " -> " + newType);
        }
        
        return item;
    }
    
    /**
     * 计算路径深度
     */
    private static int calculateDepth(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        return path.split("\\.").length;
    }
    
    /**
     * 获取路径的最后一个键
     */
    private static String getLastKey(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String[] parts = path.split("\\.");
        return parts[parts.length - 1];
    }
    
    /**
     * 获取父路径
     */
    private static String getParentPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(0, lastDot) : "";
    }
    
    /**
     * 获取值的类型
     */
    private static String getValueType(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return "boolean";
        }
        if (value instanceof Number) {
            return "number";
        }
        if (value instanceof String) {
            return "string";
        }
        if (value instanceof List) {
            return "array";
        }
        if (value instanceof Map) {
            return "object";
        }
        return "unknown";
    }
}