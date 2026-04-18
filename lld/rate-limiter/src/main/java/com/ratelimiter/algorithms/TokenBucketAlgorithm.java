package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.TokenBucketState;

public class TokenBucketAlgorithm implements Algorithm {

    @Override
    public boolean isAllowed(String identifier, RateLimitConfig config, Storage storage) {
        TokenBucketState state = storage.getOrCompute(identifier, id -> new TokenBucketState(id, config.getLimit()));
        
        synchronized (state) {
            refillTokens(state, config);
            
            if (state.getTokens().get() > 0) {
                state.getTokens().decrementAndGet();
                return true;
            }
            return false;
        }
    }

    private void refillTokens(TokenBucketState state, RateLimitConfig config) {
        long currentTime = System.currentTimeMillis();
        long lastRefill = state.getLastRefillTime().get();
        long timeElapsed = currentTime - lastRefill;
        
        // Refill rate: limit / timeWindowMs
        double refillRate = (double) config.getLimit() / config.getTimeWindowMs();
        long tokensToAdd = (long) (timeElapsed * refillRate);
        
        if (tokensToAdd > 0) {
            long newTokens = Math.min(config.getLimit(), state.getTokens().get() + tokensToAdd);
            state.getTokens().set(newTokens);
            state.getLastRefillTime().set(currentTime);
        }
    }
}
