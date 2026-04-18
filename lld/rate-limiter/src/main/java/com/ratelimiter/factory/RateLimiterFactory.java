package com.ratelimiter.factory;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.DefaultRateLimiter;
import com.ratelimiter.core.RateLimiter;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.storage.InMemoryStorage;

public class RateLimiterFactory {
    private static final Storage SHARED_STORAGE = new InMemoryStorage();

    public static RateLimiter create(RateLimitConfig config) {
        Algorithm algorithm = getAlgorithm(config.getAlgorithmType());
        return new DefaultRateLimiter(config, algorithm, SHARED_STORAGE);
    }

    private static Algorithm getAlgorithm(com.ratelimiter.model.AlgorithmType type) {
        switch (type) {
            case FIXED_WINDOW:
                return new com.ratelimiter.algorithms.FixedWindowAlgorithm();
            case TOKEN_BUCKET:
                return new com.ratelimiter.algorithms.TokenBucketAlgorithm();
            case SLIDING_WINDOW_LOG:
                return new com.ratelimiter.algorithms.SlidingWindowLogAlgorithm();
            case LEAKY_BUCKET:
                return new com.ratelimiter.algorithms.LeakyBucketAlgorithm();
            default:
                throw new IllegalArgumentException("Unsupported algorithm type: " + type);
        }
    }
}
