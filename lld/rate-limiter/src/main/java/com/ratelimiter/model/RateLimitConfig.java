package com.ratelimiter.model;

public class RateLimitConfig {
    private final long limit;
    private final long timeWindowMs;
    private final AlgorithmType algorithmType;

    public RateLimitConfig(long limit, long timeWindowMs, AlgorithmType algorithmType) {
        this.limit = limit;
        this.timeWindowMs = timeWindowMs;
        this.algorithmType = algorithmType;
    }

    public long getLimit() {
        return limit;
    }

    public long getTimeWindowMs() {
        return timeWindowMs;
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }
}
