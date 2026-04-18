package com.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

public class LeakyBucketState extends RateLimitState {
    private final AtomicLong water; // Current water level
    private final AtomicLong lastLeakTime;

    public LeakyBucketState(String identifier) {
        super(identifier);
        this.water = new AtomicLong(0);
        this.lastLeakTime = new AtomicLong(System.currentTimeMillis());
    }

    public AtomicLong getWater() {
        return water;
    }

    public AtomicLong getLastLeakTime() {
        return lastLeakTime;
    }
}
