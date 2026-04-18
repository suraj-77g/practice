package com.ratelimiter.model;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketState extends RateLimitState {
    private final AtomicLong tokens;
    private final AtomicLong lastRefillTime;

    public TokenBucketState(String identifier, long initialTokens) {
        super(identifier);
        this.tokens = new AtomicLong(initialTokens);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    public AtomicLong getTokens() {
        return tokens;
    }

    public AtomicLong getLastRefillTime() {
        return lastRefillTime;
    }
}
