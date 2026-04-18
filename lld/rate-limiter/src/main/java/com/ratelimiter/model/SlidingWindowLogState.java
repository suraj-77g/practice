package com.ratelimiter.model;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindowLogState extends RateLimitState {
    private final ConcurrentLinkedQueue<Long> timestamps;

    public SlidingWindowLogState(String identifier) {
        super(identifier);
        this.timestamps = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLinkedQueue<Long> getTimestamps() {
        return timestamps;
    }
}
