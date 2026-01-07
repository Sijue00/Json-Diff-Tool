package com.jsondiff.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * JSON对比设置模型
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
public class CompareSettings {
    
    /**
     * 是否忽略数组和对象属性的顺序差异
     */
    private boolean ignoreOrder = false;
    
    /**
     * 是否忽略字符串中的空白字符差异
     */
    private boolean ignoreWhitespace = true;
    
    /**
     * 字符串比较时是否区分大小写
     */
    private boolean caseSensitive = true;
    
    /**
     * JSON对象递归比较的最大深度
     */
    private int maxDepth = 100;
    
    /**
     * 是否忽略null值和undefined值的差异
     */
    private boolean ignoreNull = false;
    
    /**
     * 是否进行精确比较（包括数据类型）
     */
    private boolean strict = false;
    
    /**
     * 是否显示相同的值
     */
    private boolean showUnchanged = false;
    
    /**
     * 最大差异数量限制（0表示不限制）
     */
    private int maxDifferences = 0;
    
    /**
     * 是否包含数组长度变化
     */
    private boolean includeArrayLength = true;
    
    /**
     * 是否跟踪行号信息
     */
    private boolean trackLineNumbers = true;
    
    /**
     * 构造函数 - 使用默认设置
     */
    public CompareSettings() {
        // 使用默认值
    }
    
    /**
     * 构造函数 - 自定义设置
     */
    public CompareSettings(boolean ignoreOrder, boolean ignoreWhitespace, boolean caseSensitive, int maxDepth) {
        this.ignoreOrder = ignoreOrder;
        this.ignoreWhitespace = ignoreWhitespace;
        this.caseSensitive = caseSensitive;
        this.maxDepth = maxDepth;
    }
    
    /**
     * 验证设置参数
     */
    public boolean validate() {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("最大深度必须大于0");
        }
        if (maxDifferences < 0) {
            throw new IllegalArgumentException("最大差异数量不能为负数");
        }
        return true;
    }
    
    /**
     * 获取设置摘要
     */
    public String getSummary() {
        return String.format(
            "CompareSettings[ignoreOrder=%s, ignoreWhitespace=%s, caseSensitive=%s, maxDepth=%d]",
            ignoreOrder, ignoreWhitespace, caseSensitive, maxDepth
        );
    }
}