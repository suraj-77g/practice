package com.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

public class FixedWindowState extends RateLimitState {
    private final AtomicLong counter;
    private final AtomicLong windowStartTime;

    public FixedWindowState(String identifier) {
        super(identifier);
        this.counter = new AtomicLong(0);
        this.windowStartTime = new AtomicLong(System.currentTimeMillis());
    }

    public AtomicLong getCounter() {
        return counter;
    }

    public AtomicLong getWindowStartTime() {
        return windowStartTime;
    }
}
