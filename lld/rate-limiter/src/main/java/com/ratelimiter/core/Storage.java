package com.ratelimiter.core;

import com.ratelimiter.model.RateLimitState;
import java.util.function.Function;

/**
 * Interface for storing and retrieving rate-limiting state.
 */
public interface Storage {
    /**
     * Retrieves or creates a state for the given identifier.
     * 
     * @param identifier The identifier (IP/User/API Key)
     * @param stateCreator Function to create a new state if it doesn't exist
     * @param <T> The type of RateLimitState
     * @return The state associated with the identifier
     */
    <T extends RateLimitState> T getOrCompute(String identifier, Function<String, T> stateCreator);
}
