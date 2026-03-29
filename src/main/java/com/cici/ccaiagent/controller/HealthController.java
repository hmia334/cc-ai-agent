package com.cici.ccaiagent.controller;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检测控制器
 * 用于检测系统运行状态
 */
@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "健康检测", description = "系统健康状态检测相关接口")
public class HealthController {

    /**
     * 健康检测接口
     *
     * @return 健康状态信息
     */
    @GetMapping
    @Operation(summary = "健康检测", description = "检测系统是否正常运行，返回系统基本信息")
    public ResponseEntity<HealthResponse> health() {
        log.info("执行健康检测...");
        
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setTimestamp(DateUtil.now());
        response.setMessage("系统运行正常");
        
        // 添加详细信息
        Map<String, Object> details = new HashMap<>();
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("osName", System.getProperty("os.name"));
        details.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        details.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
        details.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");
        response.setDetails(details);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 简单的存活检测
     *
     * @return 存活状态
     */
    @GetMapping("/live")
    @Operation(summary = "存活检测", description = "快速检测服务是否存活，适用于 Kubernetes 探针")
    @Parameter(name = "简化版本", description = "只返回基本状态")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "ALIVE");
        result.put("timestamp", DateUtil.now());
        return ResponseEntity.ok(result);
    }

    /**
     * 就绪检测
     *
     * @return 就绪状态
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检测", description = "检测服务是否已准备就绪，可以处理请求")
    public ResponseEntity<Map<String, Object>> ready() {
        log.info("执行就绪检测...");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "READY");
        result.put("timestamp", DateUtil.now());
        result.put("message", "服务已准备就绪");
        
        // 这里可以添加各种依赖检查，如数据库、Redis 等
        Map<String, String> checks = new HashMap<>();
        checks.put("database", "OK");  // TODO: 实际项目中应该检查真实数据库连接
        checks.put("cache", "OK");      // TODO: 实际项目中应该检查 Redis 等缓存
        result.put("checks", checks);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 健康响应数据类
     */
    @Data
    static class HealthResponse {
        @Parameter(description = "健康状态：UP/DOWN")
        private String status;
        
        @Parameter(description = "检测时间")
        private String timestamp;
        
        @Parameter(description = "健康消息")
        private String message;
        
        @Parameter(description = "详细系统信息")
        private Map<String, Object> details;
    }
}
