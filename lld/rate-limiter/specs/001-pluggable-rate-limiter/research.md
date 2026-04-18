# Research: Pluggable Rate Limiter

## Concurrency Control in Java

### Decision: Use `ConcurrentHashMap` and `AtomicLong`/`AtomicReference`
- **Rationale**: For in-memory rate limiting, thread safety is critical. `ConcurrentHashMap` provides efficient concurrent access for storing state per identifier (IP/User). `Atomic` classes ensure thread-safe updates to counters and timestamps without explicit synchronization (using CAS operations), which minimizes contention and improves performance.
- **Alternatives Considered**: 
  - `synchronized` blocks: Too coarse-grained, can lead to performance bottlenecks under high load.
  - `ReentrantLock`: Better than `synchronized` but still more overhead than atomic variables for simple counter increments.

## Rate Limiting Algorithms implementation

### Decision: Implement 4 core algorithms
- **Fixed Window**: Simple counter per time window. Use `currentTimeMillis() / windowSize` as the map key.
- **Sliding Window Log**: Store timestamps of all requests in a sorted list (e.g., `ConcurrentLinkedQueue`). Remove old timestamps and check size. (High memory usage).
- **Token Bucket**: Maintain a bucket of tokens. Refill based on time elapsed since last request. Use `AtomicReference` to update bucket state (tokens, lastRefillTime) atomically.
- **Leaky Bucket (as a meter)**: Similar to Token Bucket but focuses on constant output rate.

### Rationale
These algorithms cover most common use cases and demonstrate the pluggable architecture effectively.

## Project Structure

### Decision: Single Maven Module
- **Rationale**: Given the "minimal dependencies" and "simple" requirements, a single module with clean package separation is sufficient.
- **Package Layout**:
  - `com.ratelimiter.core`: Interfaces (`RateLimiter`, `Algorithm`, `Storage`)
  - `com.ratelimiter.algorithms`: Implementations (FixedWindow, TokenBucket, etc.)
  - `com.ratelimiter.model`: Data classes (`RequestMetadata`, `Config`)
  - `com.ratelimiter.storage`: In-memory storage implementation.
  - `com.ratelimiter.factory`: Factory for creating rate limiters.
  - `com.ratelimiter.driver`: Main class for demonstration.
