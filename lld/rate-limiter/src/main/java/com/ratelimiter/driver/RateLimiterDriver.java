package com.ratelimiter.driver;

import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.factory.RateLimiterFactory;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.RequestMetadata;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RateLimiterDriver {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Rate Limiter Demonstration ===");
        
        showcaseAlgorithm(AlgorithmType.FIXED_WINDOW, 5, 1000);
        showcaseAlgorithm(AlgorithmType.TOKEN_BUCKET, 5, 1000);
        showcaseAlgorithm(AlgorithmType.SLIDING_WINDOW_LOG, 5, 1000);
        showcaseAlgorithm(AlgorithmType.LEAKY_BUCKET, 5, 1000);
        
        System.out.println("\n=== Concurrency Demonstration ===");
        showcaseConcurrency(AlgorithmType.TOKEN_BUCKET, 10, 1000);
    }

    private static void showcaseAlgorithm(AlgorithmType type, long limit, long window) throws InterruptedException {
        System.out.println("\nAlgorithm: " + type);
        RateLimitConfig config = new RateLimitConfig(limit, window, type);
        RateLimiter limiter = RateLimiterFactory.create(config);
        RequestMetadata metadata = new RequestMetadata("user1", "USER_ID");

        for (int i = 1; i <= limit + 2; i++) {
            boolean allowed = limiter.allowRequest(metadata);
            System.out.println("Request " + i + ": " + (allowed ? "ALLOWED" : "BLOCKED"));
        }
        
        System.out.println("Waiting for window to pass...");
        Thread.sleep(window + 100);
        
        boolean allowed = limiter.allowRequest(metadata);
        System.out.println("Request after wait: " + (allowed ? "ALLOWED" : "BLOCKED"));
    }

    private static void showcaseConcurrency(AlgorithmType type, long limit, long window) throws InterruptedException {
        System.out.println("Testing concurrency with " + type + " (Limit: " + limit + ")");
        RateLimitConfig config = new RateLimitConfig(limit, window, type);
        RateLimiter limiter = RateLimiterFactory.create(config);
        RequestMetadata metadata = new RequestMetadata("concurrent-user", "USER_ID");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                boolean allowed = limiter.allowRequest(metadata);
                if (allowed) {
                    System.out.print(".");
                } else {
                    System.out.print("X");
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("\nDone.");
    }
}
