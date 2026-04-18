package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FixedWindowAlgorithmTest {
    private Algorithm algorithm;
    private Storage storage;
    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        algorithm = new FixedWindowAlgorithm();
        storage = new InMemoryStorage();
        config = new RateLimitConfig(2, 1000, AlgorithmType.FIXED_WINDOW);
    }

    @Test
    void testAllowRequestWithinLimit() {
        assertTrue(algorithm.isAllowed("user1", config, storage));
        assertTrue(algorithm.isAllowed("user1", config, storage));
    }

    @Test
    void testBlockRequestExceedingLimit() {
        assertTrue(algorithm.isAllowed("user1", config, storage));
        assertTrue(algorithm.isAllowed("user1", config, storage));
        assertFalse(algorithm.isAllowed("user1", config, storage));
    }

    @Test
    void testResetAfterWindow() throws InterruptedException {
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertFalse(algorithm.isAllowed("user2", config, storage));
        
        Thread.sleep(1100);
        
        assertTrue(algorithm.isAllowed("user2", config, storage));
    }
}
