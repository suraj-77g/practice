package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.LeakyBucketState;
import com.ratelimiter.model.RateLimitConfig;

public class LeakyBucketAlgorithm implements Algorithm {

    @Override
    public boolean isAllowed(String identifier, RateLimitConfig config, Storage storage) {
        LeakyBucketState state = storage.getOrCompute(identifier, LeakyBucketState::new);
        
        synchronized (state) {
            leakWater(state, config);
            
            if (state.getWater().get() < config.getLimit()) {
                state.getWater().incrementAndGet();
                return true;
            }
            return false;
        }
    }

    private void leakWater(LeakyBucketState state, RateLimitConfig config) {
        long currentTime = System.currentTimeMillis();
        long lastLeak = state.getLastLeakTime().get();
        long timeElapsed = currentTime - lastLeak;
        
        // Leak rate: limit / timeWindowMs (water units per ms)
        double leakRate = (double) config.getLimit() / config.getTimeWindowMs();
        long waterToLeak = (long) (timeElapsed * leakRate);
        
        if (waterToLeak > 0) {
            long newWater = Math.max(0, state.getWater().get() - waterToLeak);
            state.getWater().set(newWater);
            state.getLastLeakTime().set(currentTime);
        }
    }
}
