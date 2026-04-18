package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.SlidingWindowLogState;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlidingWindowLogAlgorithm implements Algorithm {

    @Override
    public boolean isAllowed(String identifier, RateLimitConfig config, Storage storage) {
        SlidingWindowLogState state = storage.getOrCompute(identifier, SlidingWindowLogState::new);
        
        synchronized (state) {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - config.getTimeWindowMs();
            
            ConcurrentLinkedQueue<Long> timestamps = state.getTimestamps();
            
            // Remove timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
                timestamps.poll();
            }
            
            if (timestamps.size() < config.getLimit()) {
                timestamps.offer(currentTime);
                return true;
            }
            return false;
        }
    }
}
