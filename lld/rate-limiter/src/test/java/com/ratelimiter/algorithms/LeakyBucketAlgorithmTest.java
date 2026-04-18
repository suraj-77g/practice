package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LeakyBucketAlgorithmTest {
    private Algorithm algorithm;
    private Storage storage;
    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        algorithm = new LeakyBucketAlgorithm();
        storage = new InMemoryStorage();
        config = new RateLimitConfig(10, 1000, AlgorithmType.LEAKY_BUCKET);
    }

    @Test
    void testAllowRequestWithinLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(algorithm.isAllowed("user1", config, storage));
        }
        assertFalse(algorithm.isAllowed("user1", config, storage));
    }

    @Test
    void testLeak() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            assertTrue(algorithm.isAllowed("user2", config, storage));
        }
        assertFalse(algorithm.isAllowed("user2", config, storage));
        
        Thread.sleep(200); // 10 units / 1000ms = 1 unit / 100ms. 200ms should leak 2 units.
        
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertFalse(algorithm.isAllowed("user2", config, storage));
    }
}
