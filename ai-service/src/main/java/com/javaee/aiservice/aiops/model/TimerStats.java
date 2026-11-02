package com.javaee.aiservice.aiops.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 计时器统计快照，汇总某一指标的样本数、极值、均值与分位耗时。
 */
public class TimerStats {

    /** 样本数量。 */
    private final int count;
    /** 最小耗时，单位毫秒。 */
    private final long min;
    /** 最大耗时，单位毫秒。 */
    private final long max;
    /** 平均耗时，单位毫秒。 */
    private final double avg;
    /** 95 分位耗时，单位毫秒。 */
    private final long p95;
    /** 99 分位耗时，单位毫秒。 */
    private final long p99;

    public TimerStats(int count, long min, long max, double avg, long p95, long p99) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.p95 = p95;
        this.p99 = p99;
    }

    public int getCount() {
        return count;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    public long getP95() {
        return p95;
    }

    public long getP99() {
        return p99;
    }

    /**
     * 转换为便于序列化的指标映射。
     *
     * @return 以统计项名称为键、数值为值的映射
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", count);
        result.put("min", min);
        result.put("max", max);
        result.put("avg", avg);
        result.put("p95", p95);
        result.put("p99", p99);
        return result;
    }

    /**
     * 构造全零的空统计，用于无样本场景。
     *
     * @return 各统计项均为 0 的空快照
     */
    public static TimerStats empty() {
        return new TimerStats(0, 0L, 0L, 0.0, 0L, 0L);
    }
}
