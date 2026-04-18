# Data Model: Pluggable Rate Limiter

## Entities

### `RateLimitConfig`
- `long limit`: Maximum allowed requests.
- `long timeWindowMs`: Time window for the limit (e.g., 60000 for 1 minute).
- `AlgorithmType algorithm`: Enum (FIXED_WINDOW, TOKEN_BUCKET, etc.).
- `String criteria`: The criteria this config applies to (IP, USER_ID, API_KEY).

### `RequestMetadata`
- `String identifier`: The value of the criteria (e.g., "192.168.1.1").
- `String criteriaType`: (IP, USER_ID, API_KEY).

### `RateLimitState` (Base Interface/Abstract Class)
- Used to store the state per identifier (e.g., counters, timestamps).
- `FixedWindowState extends RateLimitState`: `AtomicLong counter`, `AtomicLong windowStartTime`.
- `TokenBucketState extends RateLimitState`: `AtomicLong tokens`, `AtomicLong lastRefillTime`.

## Relationships

- `RateLimiter` uses one `RateLimitConfig`.
- `RateLimiter` uses one `Algorithm` implementation.
- `Algorithm` uses `Storage` to retrieve and update `RateLimitState`.
- `Storage` maps `identifier` (String) to `RateLimitState`.

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
