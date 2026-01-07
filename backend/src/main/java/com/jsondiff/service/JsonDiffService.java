package com.jsondiff.service;

import com.jsondiff.model.*;
import com.jsondiff.util.JsonComparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * JSON对比服务
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class JsonDiffService {
    
    private final ObjectMapper objectMapper;
    
    public JsonDiffService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * 执行JSON对比
     */
    public DiffResult compare(Map<String, Object> request) {
        log.info("开始JSON对比");
        
        try {
            // 提取参数
            Object leftData = request.get("left");
            Object rightData = request.get("right");
            Map<String, Object> settingsMap = (Map<String, Object>) request.get("settings");
            
            // 解析JSON数据
            Object leftJson = parseJsonData(leftData);
            Object rightJson = parseJsonData(rightData);
            
            // 创建对比设置
            CompareSettings settings = createCompareSettings(settingsMap);
            
            // 执行对比
            JsonComparator comparator = new JsonComparator(settings);
            DiffResult result = comparator.compare(leftJson, rightJson);
            
            log.info("JSON对比完成，发现 {} 处差异", result.getTotalDifferences());
            return result;
            
        } catch (Exception e) {
            log.error("JSON对比失败", e);
            throw new RuntimeException("对比失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证JSON数据
     */
    public Map<String, Object> validate(Map<String, Object> request) {
        log.info("开始验证JSON");
        
        try {
            Object data = request.get("data");
            Object jsonData = parseJsonData(data);
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("type", getJsonType(jsonData));
            result.put("size", calculateSize(jsonData));
            
            log.info("JSON验证通过");
            return result;
            
        } catch (Exception e) {
            log.error("JSON验证失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 格式化JSON
     */
    public Map<String, Object> format(Map<String, Object> request) {
        log.info("开始格式化JSON");
        
        try {
            Object data = request.get("data");
            int indent = (int) request.getOrDefault("indent", 2);
            
            Object jsonData = parseJsonData(data);
            String formatted = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("formatted", formatted);
            result.put("size", formatted.length());
            
            log.info("JSON格式化完成");
            return result;
            
        } catch (Exception e) {
            log.error("JSON格式化失败", e);
            throw new RuntimeException("格式化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 压缩JSON
     */
    public Map<String, Object> compress(Map<String, Object> request) {
        log.info("开始压缩JSON");
        
        try {
            Object data = request.get("data");
            Object jsonData = parseJsonData(data);
            
            String compressed = objectMapper.writeValueAsString(jsonData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("compressed", compressed);
            result.put("size", compressed.length());
            
            log.info("JSON压缩完成");
            return result;
            
        } catch (Exception e) {
            log.error("JSON压缩失败", e);
            throw new RuntimeException("压缩失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换数据格式
     */
    public Map<String, Object> convert(Map<String, Object> request) {
        log.info("开始转换数据格式");
        
        try {
            String content = (String) request.get("content");
            String fromFormat = (String) request.get("fromFormat");
            String toFormat = (String) request.get("toFormat");
            
            // 解析源格式
            Object data = parseFromFormat(content, fromFormat);
            
            // 转换为目标格式
            String result = convertToFormat(data, toFormat);
            
            Map<String, Object> response = new HashMap<>();
            response.put("converted", result);
            response.put("fromFormat", fromFormat);
            response.put("toFormat", toFormat);
            
            log.info("数据格式转换完成: {} -> {}", fromFormat, toFormat);
            return response;
            
        } catch (Exception e) {
            log.error("数据格式转换失败", e);
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取示例数据
     */
    public Map<String, Object> getSample(String type) {
        log.info("获取示例数据: {}", type);
        
        Map<String, Object> sample = new HashMap<>();
        
        switch (type) {
            case "basic":
                sample.put("left", createBasicSampleLeft());
                sample.put("right", createBasicSampleRight());
                break;
            case "complex":
                sample.put("left", createComplexSampleLeft());
                sample.put("right", createComplexSampleRight());
                break;
            case "array":
                sample.put("left", createArraySampleLeft());
                sample.put("right", createArraySampleRight());
                break;
            default:
                sample.put("left", createBasicSampleLeft());
                sample.put("right", createBasicSampleRight());
        }
        
        return sample;
    }
    
    /**
     * 解析JSON数据
     */
    private Object parseJsonData(Object data) {
        if (data == null) {
            return null;
        }
        
        if (data instanceof String) {
            String str = ((String) data).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(str, Object.class);
            } catch (Exception e) {
                throw new RuntimeException("无效的JSON格式", e);
            }
        }
        
        return data;
    }
    
    /**
     * 创建对比设置
     */
    private CompareSettings createCompareSettings(Map<String, Object> settingsMap) {
        CompareSettings settings = new CompareSettings();
        
        if (settingsMap != null) {
            settings.setIgnoreOrder((Boolean) settingsMap.getOrDefault("ignoreOrder", false));
            settings.setIgnoreWhitespace((Boolean) settingsMap.getOrDefault("ignoreWhitespace", true));
            settings.setCaseSensitive((Boolean) settingsMap.getOrDefault("caseSensitive", true));
            settings.setMaxDepth((Integer) settingsMap.getOrDefault("maxDepth", 100));
            settings.setIgnoreNull((Boolean) settingsMap.getOrDefault("ignoreNull", false));
            settings.setStrict((Boolean) settingsMap.getOrDefault("strict", false));
        }
        
        return settings;
    }
    
    /**
     * 获取JSON类型
     */
    private String getJsonType(Object data) {
        if (data == null) return "null";
        if (data instanceof Map) return "object";
        if (data instanceof List) return "array";
        if (data instanceof String) return "string";
        if (data instanceof Number) return "number";
        if (data instanceof Boolean) return "boolean";
        return "unknown";
    }
    
    /**
     * 计算JSON大小
     */
    private int calculateSize(Object data) {
        if (data == null) return 0;
        if (data instanceof Map) return ((Map<?, ?>) data).size();
        if (data instanceof List) return ((List<?>) data).size();
        return 1;
    }
    
    /**
     * 从指定格式解析
     */
    private Object parseFromFormat(String content, String format) {
        try {
            switch (format.toLowerCase()) {
                case "json":
                    return objectMapper.readValue(content, Object.class);
                case "xml":
                    // 这里应该使用XML解析器，简化实现
                    throw new UnsupportedOperationException("XML解析暂不支持");
                case "yaml":
                case "yml":
                    // 这里应该使用YAML解析器，简化实现
                    throw new UnsupportedOperationException("YAML解析暂不支持");
                default:
                    throw new IllegalArgumentException("不支持的格式: " + format);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换为目标格式
     */
    private String convertToFormat(Object data, String format) {
        try {
            switch (format.toLowerCase()) {
                case "json":
                    return objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(data);
                case "xml":
                    // 这里应该使用XML转换器，简化实现
                    throw new UnsupportedOperationException("XML转换暂不支持");
                case "yaml":
                case "yml":
                    // 这里应该使用YAML转换器，简化实现
                    throw new UnsupportedOperationException("YAML转换暂不支持");
                default:
                    throw new IllegalArgumentException("不支持的格式: " + format);
            }
        } catch (Exception e) {
            throw new RuntimeException("转换失败: " + e.getMessage(), e);
        }
    }
    
    // 示例数据创建方法
    private Map<String, Object> createBasicSampleLeft() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("name", "张三");
        sample.put("age", 30);
        sample.put("email", "zhangsan@example.com");
        
        Map<String, Object> address = new HashMap<>();
        address.put("city", "北京");
        address.put("zipcode", "100000");
        sample.put("address", address);
        
        sample.put("hobbies", Arrays.asList("阅读", "游泳", "编程"));
        
        return sample;
    }
    
    private Map<String, Object> createBasicSampleRight() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("name", "张三");
        sample.put("age", 31);
        sample.put("email", "zhangsan@example.com");
        
        Map<String, Object> address = new HashMap<>();
        address.put("city", "上海");
        address.put("zipcode", "200000");
        address.put("district", "浦东新区");
        sample.put("address", address);
        
        sample.put("hobbies", Arrays.asList("阅读", "跑步", "编程", "音乐"));
        sample.put("phone", "13800138000");
        
        return sample;
    }
    
    private Map<String, Object> createComplexSampleLeft() {
        // 创建更复杂的示例数据
        Map<String, Object> sample = new HashMap<>();
        sample.put("id", 1);
        sample.put("users", Arrays.asList(
            createUser("user1", "张三", 25),
            createUser("user2", "李四", 30)
        ));
        return sample;
    }
    
    private Map<String, Object> createComplexSampleRight() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("id", 1);
        sample.put("users", Arrays.asList(
            createUser("user1", "张三", 26),
            createUser("user3", "王五", 28)
        ));
        sample.put("metadata", createMetadata());
        return sample;
    }
    
    private Map<String, Object> createUser(String id, String name, int age) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("age", age);
        return user;
    }
    
    private Map<String, Object> createMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0");
        metadata.put("timestamp", LocalDateTime.now().toString());
        return metadata;
    }
    
    private List<Object> createArraySampleLeft() {
        return Arrays.asList(1, 2, 3, 4, 5);
    }
    
    private List<Object> createArraySampleRight() {
        return Arrays.asList(1, 2, 3, 6, 7, 8);
    }
}