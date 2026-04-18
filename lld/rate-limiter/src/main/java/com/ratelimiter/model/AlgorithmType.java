package com.ratelimiter.model;

public enum AlgorithmType {
    FIXED_WINDOW,
    SLIDING_WINDOW_LOG,
    TOKEN_BUCKET,
    LEAKY_BUCKET
}
