package com.jsondiff.controller;

import com.jsondiff.model.DiffResult;
import com.jsondiff.service.JsonDiffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON对比控制器
 * 
 * @author JSON Diff Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class JsonDiffController {
    
    @Autowired
    private JsonDiffService jsonDiffService;
    
    /**
     * JSON对比接口
     */
    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<DiffResult>> compare(@RequestBody Map<String, Object> request) {
        log.info("收到JSON对比请求");
        
        try {
            DiffResult result = jsonDiffService.compare(request);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("JSON对比失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * JSON验证接口 ---前端校验
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(@RequestBody Map<String, Object> request) {
        log.info("收到JSON验证请求");
        
        try {
            Map<String, Object> result = jsonDiffService.validate(request);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("JSON验证失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * JSON格式化接口
     */
    @PostMapping("/format")
    public ResponseEntity<ApiResponse<Map<String, Object>>> format(@RequestBody Map<String, Object> request) {
        log.info("收到JSON格式化请求");
        
        try {
            Map<String, Object> result = jsonDiffService.format(request);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("JSON格式化失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * JSON压缩接口
     */
    @PostMapping("/compress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> compress(@RequestBody Map<String, Object> request) {
        log.info("收到JSON压缩请求");
        
        try {
            Map<String, Object> result = jsonDiffService.compress(request);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("JSON压缩失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 数据格式转换接口
     */
    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convert(@RequestBody Map<String, Object> request) {
        log.info("收到数据格式转换请求");
        
        try {
            Map<String, Object> result = jsonDiffService.convert(request);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("数据格式转换失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取示例数据接口----前端生成
     */
    @GetMapping("/samples/{type}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSample(@PathVariable String type) {
        log.info("收到获取示例数据请求，类型: {}", type);
        
        try {
            Map<String, Object> result = jsonDiffService.getSample(type);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("获取示例数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "ok");
        status.put("service", "json-diff-backend");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    /**
     * API响应包装类
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private long timestamp;
        
        public ApiResponse() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public ApiResponse(boolean success, String message, T data) {
            this();
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, "success", data);
        }
        
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }
        
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}