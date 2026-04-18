package com.ratelimiter.model;

/**
 * Base class for rate-limiting state per identifier.
 */
public abstract class RateLimitState {
    private final String identifier;

    protected RateLimitState(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
