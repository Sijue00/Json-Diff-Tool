package com.jsondiff.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.jsondiff.model.DiffItem;
import com.jsondiff.model.CompareSettings;
import com.jsondiff.model.DiffResult;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

/**
 * JSON比较器 - 核心算法实现
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
public class JsonComparator {
    
    private final ObjectMapper objectMapper;
    private final CompareSettings settings;
    private final List<DiffItem> differences;
    private final Map<String, Integer> lineNumberMap;
    private long startTime;
    
    /**
     * 构造函数
     */
    public JsonComparator(CompareSettings settings) {
        this.objectMapper = new ObjectMapper();
        this.settings = settings != null ? settings : new CompareSettings();
        this.differences = new ArrayList<>();
        this.lineNumberMap = new HashMap<>();
    }
    
    /**
     * 比较两个JSON对象
     */
    public DiffResult compare(Object left, Object right) {
        startTime = System.currentTimeMillis();
        differences.clear();
        lineNumberMap.clear();
        
        try {
            JsonNode leftNode = convertToJsonNode(left);
            JsonNode rightNode = convertToJsonNode(right);
            
            compareNodes("$", leftNode, rightNode, 0);
            
            DiffResult result = new DiffResult();

            result.setLeftData(left);
            result.setRightData(right);
            result.setSettings(settings);
            result.setDuration(System.currentTimeMillis() - startTime);

            result.setDifferences(differences);
            // 使用临时集合来避免并发修改
            List<DiffItem> tempDifferences = new ArrayList<>(differences);
            // 更新统计信息
            for (DiffItem diff : tempDifferences) {
                result.addDifference(diff);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("JSON比较失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 递归比较JSON节点
     */
    private void compareNodes(String path, JsonNode left, JsonNode right, int depth) {
        // 检查深度限制
        if (depth >= settings.getMaxDepth()) {
            return;
        }
        
        // 处理null值
        if (left == null && right == null) {
            return;
        }
        
        if (left == null) {
            differences.add(DiffItem.added(path, convertToObject(right)));
            return;
        }
        
        if (right == null) {
            differences.add(DiffItem.removed(path, convertToObject(left)));
            return;
        }
        
        // 检查节点类型
        if (left.getNodeType() != right.getNodeType()) {
            differences.add(DiffItem.modified(path, convertToObject(left), convertToObject(right)));
            return;
        }
        
        // 根据节点类型进行比较
        switch (left.getNodeType()) {
            case OBJECT:
                compareObjects(path, (ObjectNode) left, (ObjectNode) right, depth + 1);
                break;
            case ARRAY:
                compareArrays(path, (ArrayNode) left, (ArrayNode) right, depth + 1);
                break;
            case STRING:
                compareStrings(path, left.asText(), right.asText());
                break;
            case NUMBER:
                compareNumbers(path, left.asDouble(), right.asDouble());
                break;
            case BOOLEAN:
                compareBooleans(path, left.asBoolean(), right.asBoolean());
                break;
            case NULL:
                // null值不需要比较
                break;
            default:
                // 其他类型直接比较
                if (!left.equals(right)) {
                    differences.add(DiffItem.modified(path, convertToObject(left), convertToObject(right)));
                }
        }
    }
    
    /**
     * 比较对象节点
     */
    private void compareObjects(String path, ObjectNode left, ObjectNode right, int depth) {
        Set<String> leftKeys = new HashSet<>();
        left.fieldNames().forEachRemaining(leftKeys::add);
        
        Set<String> rightKeys = new HashSet<>();
        right.fieldNames().forEachRemaining(rightKeys::add);
        
        // 处理删除的键
        for (String key : leftKeys) {
            if (!rightKeys.contains(key)) {
                String childPath = path + "." + key;
                differences.add(DiffItem.removed(childPath, convertToObject(left.get(key))));
            }
        }
        
        // 处理新增的键
        for (String key : rightKeys) {
            if (!leftKeys.contains(key)) {
                String childPath = path + "." + key;
                differences.add(DiffItem.added(childPath, convertToObject(right.get(key))));
            }
        }
        
        // 处理共同的键
        for (String key : leftKeys) {
            if (rightKeys.contains(key)) {
                String childPath = path + "." + key;
                compareNodes(childPath, left.get(key), right.get(key), depth);
            }
        }
    }
    
    /**
     * 比较数组节点
     */
    private void compareArrays(String path, ArrayNode left, ArrayNode right, int depth) {
        int leftSize = left.size();
        int rightSize = right.size();
        
        // 比较数组长度变化
        if (settings.isIncludeArrayLength() && leftSize != rightSize) {
            String lengthPath = path + "._length";
            differences.add(DiffItem.modified(lengthPath, leftSize, rightSize));
        }
        
        // 比较数组元素
        int minSize = Math.min(leftSize, rightSize);
        for (int i = 0; i < minSize; i++) {
            String elementPath = path + "[" + i + "]";
            compareNodes(elementPath, left.get(i), right.get(i), depth);
        }
        
        // 处理新增的元素
        if (rightSize > leftSize) {
            for (int i = leftSize; i < rightSize; i++) {
                String elementPath = path + "[" + i + "]";
                differences.add(DiffItem.added(elementPath, convertToObject(right.get(i))));
            }
        }
        
        // 处理删除的元素
        if (leftSize > rightSize) {
            for (int i = rightSize; i < leftSize; i++) {
                String elementPath = path + "[" + i + "]";
                differences.add(DiffItem.removed(elementPath, convertToObject(left.get(i))));
            }
        }
    }
    
    /**
     * 比较字符串值
     */
    private void compareStrings(String path, String left, String right) {
        if (settings.isIgnoreWhitespace()) {
            left = left.trim();
            right = right.trim();
        }
        
        if (!settings.isCaseSensitive()) {
            left = left.toLowerCase();
            right = right.toLowerCase();
        }
        
        if (!left.equals(right)) {
            differences.add(DiffItem.modified(path, left, right));
        }
    }
    
    /**
     * 比较数值
     */
    private void compareNumbers(String path, double left, double right) {
        if (Math.abs(left - right) > 1e-10) {
            differences.add(DiffItem.modified(path, left, right));
        }
    }
    
    /**
     * 比较布尔值
     */
    private void compareBooleans(String path, boolean left, boolean right) {
        if (left != right) {
            differences.add(DiffItem.modified(path, left, right));
        }
    }
    
    /**
     * 将任意对象转换为JsonNode
     */
    private JsonNode convertToJsonNode(Object obj) {
        if (obj == null) {
            return NullNode.getInstance();
        }
        
        if (obj instanceof JsonNode) {
            return (JsonNode) obj;
        }
        
        if (obj instanceof String) {
            try {
                return objectMapper.readTree((String) obj);
            } catch (Exception e) {
                // 如果不是有效的JSON，返回文本节点
                return new TextNode((String) obj);
            }
        }
        
        return objectMapper.valueToTree(obj);
    }
    
    /**
     * 将JsonNode转换为Java对象
     */
    private Object convertToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        if (node.isTextual()) {
            return node.asText();
        }
        
        if (node.isNumber()) {
            return node.asDouble();
        }
        
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : node) {
                list.add(convertToObject(element));
            }
            return list;
        }
        
        if (node.isObject()) {
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(entry -> {
                map.put(entry.getKey(), convertToObject(entry.getValue()));
            });
            return map;
        }
        
        return node.toString();
    }
    
    /**
     * 计算JSON路径的行号映射
     */
    public void calculateLineNumbers(String jsonString, boolean isLeft) {
        if (!settings.isTrackLineNumbers() || StringUtils.isEmpty(jsonString)) {
            return;
        }
        
        String[] lines = jsonString.split("\\n");
        int lineNumber = 1;
        
        for (String line : lines) {
            // 简单的行号映射，实际项目中可能需要更复杂的算法
            lineNumberMap.put(isLeft ? "left_" + lineNumber : "right_" + lineNumber, lineNumber);
            lineNumber++;
        }
    }
    
    /**
     * 获取行号
     */
    public Integer getLineNumber(String path, boolean isLeft) {
        String key = (isLeft ? "left_" : "right_") + path;
        return lineNumberMap.get(key);
    }
}