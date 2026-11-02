package com.javaee.aiservice.aiops.alert;

import com.javaee.aiservice.aiops.model.FaultRecord;
import com.javaee.aiservice.internal.InternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AIOps 告警投递服务，按故障指纹做冷却去重后调用内部服务发送告警。
 *
 * <p>同一指纹在冷却窗口内只发送一次，冷却时长由 {@code ai.aiops.alert.cooldown-ms} 配置。</p>
 */
@Component
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private InternalService internalService;

    @Value("${ai.aiops.alert.cooldown-ms:300000}")
    private long cooldownMs;

    private final ConcurrentHashMap<String, Long> lastSentAt = new ConcurrentHashMap<>();

    /**
     * 在冷却窗口允许时发送告警。
     *
     * @param record 故障记录，其指纹用于冷却判定
     * @return 是否实际发送：true-已发送，false-处于冷却期被跳过
     */
    public boolean sendIfAllowed(FaultRecord record) {
        long now = System.currentTimeMillis();
        Long previous = lastSentAt.get(record.getFingerprint());
        if (previous != null && now - previous < cooldownMs) {
            log.debug("告警处于冷却期，跳过发送: fingerprint={}", record.getFingerprint());
            return false;
        }

        lastSentAt.put(record.getFingerprint(), now);
        Map<String, Object> alertParams = new HashMap<>();
        alertParams.put("faultId", record.getFaultId());
        alertParams.put("type", record.getType());
        alertParams.put("fingerprint", record.getFingerprint());
        alertParams.put("message", record.getMessage());
        alertParams.put("status", record.getStatus());
        alertParams.put("timestamp", now);

        log.warn("发送 AIOps 告警: {}", alertParams);
        internalService.sendAlert(alertParams);
        return true;
    }

    /**
     * 清除指定故障指纹的冷却记录，使其可立即再次发送。
     *
     * @param fingerprint 故障指纹
     */
    public void clearCooldown(String fingerprint) {
        lastSentAt.remove(fingerprint);
    }

    /**
     * 清空全部冷却记录。
     */
    public void reset() {
        lastSentAt.clear();
    }
}
