package com.ratelimiter.core;

import com.ratelimiter.factory.RateLimiterFactory;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.RequestMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultiCriteriaLimiterTest {

    @Test
    void testCriteriaIsolation() {
        // Same identifier "123" for both IP and USER_ID should have separate limits
        RateLimitConfig config = new RateLimitConfig(1, 1000, AlgorithmType.FIXED_WINDOW);
        RateLimiter limiter = RateLimiterFactory.create(config);
        
        RequestMetadata ipMetadata = new RequestMetadata("123", "IP");
        RequestMetadata userMetadata = new RequestMetadata("123", "USER_ID");
        
        assertTrue(limiter.allowRequest(ipMetadata));
        assertFalse(limiter.allowRequest(ipMetadata));
        
        // This should pass because it's a different criteria even if identifier is same
        assertTrue(limiter.allowRequest(userMetadata));
        assertFalse(limiter.allowRequest(userMetadata));
    }
}
