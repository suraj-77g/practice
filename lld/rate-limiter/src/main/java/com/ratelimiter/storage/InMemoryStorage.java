package com.ratelimiter.storage;

import com.ratelimiter.core.Storage;
import com.ratelimiter.model.RateLimitState;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryStorage implements Storage {
    private final ConcurrentHashMap<String, RateLimitState> stateMap = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RateLimitState> T getOrCompute(String identifier, Function<String, T> stateCreator) {
        return (T) stateMap.computeIfAbsent(identifier, stateCreator);
    }
}
