# Implementation Plan: Pluggable Rate Limiter

**Branch**: `001-pluggable-rate-limiter` | **Date**: 2026-04-18 | **Spec**: [specs/001-pluggable-rate-limiter/spec.md]

## Summary
Implement a thread-safe, modular rate-limiting library in core Java 17+. The system will support multiple algorithms (Token Bucket, Fixed Window, Sliding Window Log, Leaky Bucket) via a pluggable strategy pattern. It will handle concurrent requests using `ConcurrentHashMap` and `Atomic` classes and provide a driver program for demonstration.

## Technical Context
**Language/Version**: Java 17  
**Primary Dependencies**: Maven, JUnit 5  
**Storage**: In-memory (ConcurrentHashMap)  
**Testing**: JUnit 5  
**Project Type**: Library/CLI  
**Performance Goals**: > 10,000 req/s in-memory overhead < 1ms  
**Constraints**: Minimal dependencies, core Java focus.  

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Minimal Dependency Core Java
- [x] SOLID Design Principles
- [x] Interface-Driven Development
- [x] Testability & Modularity
- [x] Simplicity & YAGNI

## Project Structure

### Documentation (this feature)
```text
specs/001-pluggable-rate-limiter/
├── plan.md              # This file
├── research.md          # Concurrency and algorithm decisions
├── data-model.md        # Entities and Architecture Diagram
├── quickstart.md        # Usage instructions
└── contracts/           # Interface definitions
    └── RateLimiter.java
```

### Source Code (repository root)
```text
src/main/java/com/ratelimiter/
├── core/                # Core interfaces (RateLimiter, Algorithm, Storage)
├── algorithms/          # Implementations (TokenBucket, FixedWindow, etc.)
├── model/               # Data models (RateLimitConfig, RequestMetadata)
├── storage/             # In-memory storage implementation
├── factory/             # RateLimiterFactory
└── driver/              # RateLimiterDriver (Main program)

src/test/java/com/ratelimiter/
├── algorithms/          # Algorithm-specific unit tests
└── core/                # Integration tests for the RateLimiter
```

## Architecture Diagram (ASCII)
```text
+----------------+       +-------------------+       +-----------------------+
| Driver Program | ----> |    RateLimiter    | ----> |  Algorithm Interface  |
+----------------+       +---------+---------+       +-----------+-----------+
                                   |                             |
                                   v                             v
                         +-------------------+       +-----------+-----------+
                         | RateLimitConfig   |       |  FixedWindowAlgorithm |
                         +-------------------+       |  TokenBucketAlgorithm |
                                                     |  SlidingWindowAlg...  |
                                                     +-----------+-----------+
                                                                 |
                                                                 v
                                                     +-----------------------+
                                                     |   Storage Interface   |
                                                     +-----------+-----------+
                                                                 |
                                                                 v
                                                     +-----------------------+
                                |   InMemoryStorage     |
                                | (ConcurrentHashMap)   |
                                +-----------------------+
```

## Complexity Tracking
| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
