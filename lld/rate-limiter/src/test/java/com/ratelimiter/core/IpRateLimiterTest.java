package com.ratelimiter.core;

import com.ratelimiter.factory.RateLimiterFactory;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.RequestMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IpRateLimiterTest {

    @Test
    void testIpBasedLimiting() {
        RateLimitConfig config = new RateLimitConfig(3, 1000, AlgorithmType.FIXED_WINDOW);
        RateLimiter limiter = RateLimiterFactory.create(config);
        
        RequestMetadata metadata = new RequestMetadata("127.0.0.1", "IP");
        
        assertTrue(limiter.allowRequest(metadata));
        assertTrue(limiter.allowRequest(metadata));
        assertTrue(limiter.allowRequest(metadata));
        assertFalse(limiter.allowRequest(metadata));
        
        // Different IP should have its own limit
        RequestMetadata metadata2 = new RequestMetadata("192.168.1.1", "IP");
        assertTrue(limiter.allowRequest(metadata2));
    }
}
