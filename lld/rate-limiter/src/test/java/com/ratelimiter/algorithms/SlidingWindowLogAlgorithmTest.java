package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowLogAlgorithmTest {
    private Algorithm algorithm;
    private Storage storage;
    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        algorithm = new SlidingWindowLogAlgorithm();
        storage = new InMemoryStorage();
        config = new RateLimitConfig(2, 1000, AlgorithmType.SLIDING_WINDOW_LOG);
    }

    @Test
    void testAllowRequestWithinLimit() {
        assertTrue(algorithm.isAllowed("user1", config, storage));
        assertTrue(algorithm.isAllowed("user1", config, storage));
        assertFalse(algorithm.isAllowed("user1", config, storage));
    }

    @Test
    void testSlidingWindow() throws InterruptedException {
        assertTrue(algorithm.isAllowed("user2", config, storage));
        Thread.sleep(600);
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertFalse(algorithm.isAllowed("user2", config, storage));
        
        Thread.sleep(500); // First request (at T=0) should be expired, but second (at T=600) is still in window
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertFalse(algorithm.isAllowed("user2", config, storage));
    }
}
