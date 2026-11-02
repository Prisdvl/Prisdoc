package com.javaee.aiservice.controller;

import com.javaee.aiservice.aiops.FaultDetector;
import com.javaee.aiservice.aiops.MonitoringService;
import com.javaee.aiservice.security.RequestUserContext;
import com.javaee.common.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AIOps 控制器，提供监控指标、健康检查与故障处理接口。
 *
 * <p>重置指标、故障检测与故障处理属于管理操作，仅管理员可调用。</p>
 */
@RestController
@RequestMapping("/api/ai/aiops")
@Tag(name = "AIOps", description = "系统监控、故障检测、故障处理接口")
public class AIOpsController {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private FaultDetector faultDetector;

    @Autowired
    private RequestUserContext requestUserContext;

    /**
     * 获取全部监控指标。
     *
     * @return 计数器与耗时等指标数据
     */
    @GetMapping("/monitor")
    @Operation(summary = "获取监控指标", description = "获取系统的监控指标")
    public Result<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = monitoringService.getAllMetrics();
        return Result.success(metrics);
    }

    /**
     * 检查系统健康状态。
     *
     * @return 健康检查结果
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统健康状态")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = monitoringService.healthCheck();
        return Result.success(health);
    }

    /**
     * 重置监控指标与故障记录。
     *
     * @return 成功时不含数据
     */
    @PostMapping("/metrics/reset")
    @Operation(summary = "重置指标", description = "重置所有监控指标")
    public Result<Void> resetMetrics() {
        requireAdmin();
        monitoringService.resetMetrics();
        faultDetector.resetFaults();
        return Result.success();
    }

    /**
     * 触发一次故障检测。
     *
     * @return 本次检测到的故障列表
     */
    @GetMapping("/detect")
    @Operation(summary = "检测故障", description = "检测系统中的故障")
    public Result<List<Map<String, Object>>> detectFaults() {
        requireAdmin();
        List<Map<String, Object>> faults = faultDetector.detectFaults();
        return Result.success(faults);
    }

    /**
     * 获取全部故障记录。
     *
     * @return 故障记录列表
     */
    @GetMapping("/faults")
    @Operation(summary = "获取故障列表", description = "获取所有故障记录")
    public Result<List<Map<String, Object>>> getAllFaults() {
        requireAdmin();
        List<Map<String, Object>> faults = faultDetector.getAllFaults();
        return Result.success(faults);
    }

    /**
     * 将指定故障标记为已处理。
     *
     * @param faultId 故障 ID
     * @return 处理结果
     */
    @PostMapping("/faults/{faultId}/resolve")
    @Operation(summary = "处理故障", description = "处理指定的故障")
    public Result<Map<String, Object>> resolveFault(
            @Parameter(description = "故障ID") @PathVariable String faultId) {
        requireAdmin();
        Map<String, Object> result = faultDetector.resolveFault(faultId);
        return Result.success(result);
    }

    /**
     * 累加指定计数器的值。
     *
     * @param name 指标名称
     * @param delta 增量
     * @return 成功时不含数据
     */
    @PostMapping("/metrics/counter")
    @Operation(summary = "记录计数器", description = "记录计数器指标")
    public Result<Void> incrementCounter(
            @Parameter(description = "指标名称") @RequestParam String name,
            @Parameter(description = "增量") @RequestParam(defaultValue = "1") long delta) {
        monitoringService.incrementCounter(name, delta);
        return Result.success();
    }

    /**
     * 记录一次耗时采样。
     *
     * @param name 指标名称
     * @param duration 耗时，单位毫秒
     * @return 成功时不含数据
     */
    @PostMapping("/metrics/timer")
    @Operation(summary = "记录耗时", description = "记录耗时指标")
    public Result<Void> recordTimer(
            @Parameter(description = "指标名称") @RequestParam String name,
            @Parameter(description = "耗时(ms)") @RequestParam long duration) {
        monitoringService.recordTimer(name, duration);
        return Result.success();
    }

    private void requireAdmin() {
        if (!requestUserContext.isAdmin()) {
            throw new SecurityException("AIOps 管理接口仅管理员可访问");
        }
    }
}
