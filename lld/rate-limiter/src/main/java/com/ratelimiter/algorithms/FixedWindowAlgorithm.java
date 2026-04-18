package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.FixedWindowState;
import com.ratelimiter.model.RateLimitConfig;

public class FixedWindowAlgorithm implements Algorithm {

    @Override
    public boolean isAllowed(String identifier, RateLimitConfig config, Storage storage) {
        FixedWindowState state = storage.getOrCompute(identifier, FixedWindowState::new);
        
        synchronized (state) {
            long currentTime = System.currentTimeMillis();
            long windowStart = state.getWindowStartTime().get();
            
            if (currentTime - windowStart >= config.getTimeWindowMs()) {
                state.getWindowStartTime().set(currentTime);
                state.getCounter().set(0);
            }
            
            return state.getCounter().incrementAndGet() <= config.getLimit();
        }
    }
}
