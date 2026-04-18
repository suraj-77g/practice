package com.ratelimiter.core;

import com.ratelimiter.factory.RateLimiterFactory;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.RequestMetadata;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyStressTest {

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threads = 50;
        int requestsPerThread = 100;
        int totalRequests = threads * requestsPerThread;
        int limit = 1000;
        
        RateLimitConfig config = new RateLimitConfig(limit, 10000, AlgorithmType.FIXED_WINDOW);
        RateLimiter limiter = RateLimiterFactory.create(config);
        RequestMetadata metadata = new RequestMetadata("stress-user", "USER_ID");
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        AtomicInteger allowedCount = new AtomicInteger(0);
        
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    if (limiter.allowRequest(metadata)) {
                        allowedCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(limit, allowedCount.get(), "Allowed requests should exactly match the limit under high concurrency in a single window");
    }
}
