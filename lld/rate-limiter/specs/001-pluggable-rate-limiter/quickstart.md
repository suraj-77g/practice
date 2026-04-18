# Quickstart: Pluggable Rate Limiter

## Installation
The rate limiter is a Maven-based Java 17+ application. Add the project to your classpath or install as a JAR.

## Basic Usage

### 1. Configure the Rate Limiter
```java
RateLimitConfig config = new RateLimitConfig(10, 60000, AlgorithmType.TOKEN_BUCKET);
RateLimiter limiter = RateLimiterFactory.create(config);
```

### 2. Check for Request Eligibility
```java
RequestMetadata metadata = new RequestMetadata("192.168.1.1", "IP");
if (limiter.allowRequest(metadata)) {
    // Process the request
} else {
    // Respond with rate-limit error (e.g., HTTP 429)
}
```

## Running the Driver Program
Execute the main class `com.ratelimiter.driver.RateLimiterDriver` to see a demonstration of multiple algorithms handling concurrent requests.
```bash
mvn compile exec:java -Dexec.mainClass="com.ratelimiter.driver.RateLimiterDriver"
```
