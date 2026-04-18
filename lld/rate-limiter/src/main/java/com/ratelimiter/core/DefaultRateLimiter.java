package com.ratelimiter.core;

import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.model.RequestMetadata;

public class DefaultRateLimiter implements RateLimiter {
    private final RateLimitConfig config;
    private final Algorithm algorithm;
    private final Storage storage;

    public DefaultRateLimiter(RateLimitConfig config, Algorithm algorithm, Storage storage) {
        this.config = config;
        this.algorithm = algorithm;
        this.storage = storage;
    }

    @Override
    public boolean allowRequest(RequestMetadata requestMetadata) {
        String compositeKey = requestMetadata.getCriteriaType() + ":" + 
                             requestMetadata.getIdentifier() + ":" + 
                             config.getAlgorithmType().name();
        return algorithm.isAllowed(compositeKey, config, storage);
    }
}
