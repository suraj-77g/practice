package com.ratelimiter.core;

import com.ratelimiter.model.RateLimitConfig;

/**
 * Strategy interface for rate-limiting algorithms.
 */
public interface Algorithm {
    /**
     * Checks if the request is allowed for the given identifier.
     * 
     * @param identifier The identifier (IP/User/API Key)
     * @param config The rate limit configuration
     * @param storage The state storage
     * @return true if allowed, false otherwise
     */
    boolean isAllowed(String identifier, RateLimitConfig config, Storage storage);
}
