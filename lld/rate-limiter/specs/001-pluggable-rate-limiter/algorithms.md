# Rate Limiting Algorithms

This document explains the four algorithms implemented in the Pluggable Rate Limiter.

## 1. Fixed Window

**Concept**: Time is divided into fixed intervals (e.g., 1 minute). A counter is maintained for each interval.

**ASCII Diagram**:
```text
Limit: 3 requests/window
Window 1 (0-60s)         Window 2 (60-120s)
[ R1, R2, R3 ] | (Reset) | [ R1, R2, R3 ]
      ^                   ^
   Allowed             Allowed
```

**Code Logic**:
- Calculate `windowStart = currentTime - (currentTime % windowSize)`.
- If current window is different from stored window, reset counter.
- Increment and check if `counter <= limit`.

---

## 2. Sliding Window Log

**Concept**: Tracks the exact timestamp of every request. On each check, it "slides" the window and removes expired timestamps.

**ASCII Diagram**:
```text
Window Size: 60s, Limit: 3
Log: [ 10s, 25s, 45s, 55s ]
        ^      (Now: 70s)
[ (Expired), 25s, 45s, 55s ] -> Size 3 (Limit reached)
```

**Code Logic**:
- Maintain a `Queue` of timestamps.
- Remove all timestamps `< (currentTime - windowSize)`.
- If `queue.size() < limit`, add `currentTime` and return `true`.

---

## 3. Token Bucket

**Concept**: A bucket holds tokens. Tokens are added at a constant rate. A request is allowed if it can consume a token.

**ASCII Diagram**:
```text
Refill (+) 1 token/sec
      |
      v
+-----------+
|  O  O  O  | (Max capacity: 5)
+-----------+
      |
      v Consumption (-) 1 token/request
```

**Code Logic**:
- Calculate `tokensToAdd = (currentTime - lastRefill) * refillRate`.
- Update `tokens = min(capacity, currentTokens + tokensToAdd)`.
- If `tokens > 0`, decrement and return `true`.

---

## 4. Leaky Bucket

**Concept**: Requests enter a bucket (queue). The bucket leaks (processes) at a constant rate. If the bucket overflows, requests are rejected.

**ASCII Diagram**:
```text
Request (v)
+-----------+
|  .  .  .  | (Bucket Capacity)
+-----------+
      |
      v Leak (-) Constant Rate
```

**Code Logic**:
- Calculate `waterToLeak = (currentTime - lastLeak) * leakRate`.
- Update `currentWater = max(0, currentWater - waterToLeak)`.
- If `currentWater < capacity`, increment `currentWater` and return `true`.
