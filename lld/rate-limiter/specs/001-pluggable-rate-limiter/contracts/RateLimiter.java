package com.ratelimiter.core;

import com.ratelimiter.model.RequestMetadata;

/**
 * Interface for the RateLimiter component.
 */
public interface RateLimiter {
    /**
     * Checks if a request is allowed based on the metadata and current limit.
     * 
     * @param requestMetadata Information about the request (e.g., identifier)
     * @return true if the request is allowed, false if it's rate-limited
     */
    boolean allowRequest(RequestMetadata requestMetadata);
}
