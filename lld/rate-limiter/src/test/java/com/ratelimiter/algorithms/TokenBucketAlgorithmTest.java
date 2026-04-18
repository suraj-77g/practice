package com.ratelimiter.algorithms;

import com.ratelimiter.core.Algorithm;
import com.ratelimiter.core.Storage;
import com.ratelimiter.model.AlgorithmType;
import com.ratelimiter.model.RateLimitConfig;
import com.ratelimiter.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketAlgorithmTest {
    private Algorithm algorithm;
    private Storage storage;
    private RateLimitConfig config;

    @BeforeEach
    void setUp() {
        algorithm = new TokenBucketAlgorithm();
        storage = new InMemoryStorage();
        config = new RateLimitConfig(10, 1000, AlgorithmType.TOKEN_BUCKET);
    }

    @Test
    void testAllowRequestWithinLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(algorithm.isAllowed("user1", config, storage));
        }
        assertFalse(algorithm.isAllowed("user1", config, storage));
    }

    @Test
    void testRefill() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            assertTrue(algorithm.isAllowed("user2", config, storage));
        }
        assertFalse(algorithm.isAllowed("user2", config, storage));
        
        Thread.sleep(200); // 10 tokens / 1000ms = 1 token / 100ms. 200ms should add 2 tokens.
        
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertTrue(algorithm.isAllowed("user2", config, storage));
        assertFalse(algorithm.isAllowed("user2", config, storage));
    }
}
