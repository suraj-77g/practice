# Tasks: Pluggable Rate Limiter

**Input**: Design documents from `specs/001-pluggable-rate-limiter/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Test tasks are included as requested in the implementation plan (JUnit 5).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven project structure with `pom.xml` including JUnit 5 dependencies
- [x] T002 Configure Java 17 in `pom.xml` and setup base package `com.ratelimiter`
- [x] T003 [P] Create directory structure for `core`, `algorithms`, `model`, `storage`, `factory`, and `driver`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [x] T004 Implement `RequestMetadata` and `RateLimitConfig` in `src/main/java/com/ratelimiter/model/`
- [x] T005 Implement `AlgorithmType` enum and `RateLimitState` abstract class in `src/main/java/com/ratelimiter/model/`
- [x] T006 Implement `Storage` interface in `src/main/java/com/ratelimiter/core/Storage.java`
- [x] T007 Implement `InMemoryStorage` using `ConcurrentHashMap` in `src/main/java/com/ratelimiter/storage/InMemoryStorage.java`
- [x] T008 Implement `Algorithm` interface in `src/main/java/com/ratelimiter/core/Algorithm.java`
- [x] T009 Implement `RateLimiter` interface in `src/main/java/com/ratelimiter/core/RateLimiter.java` (per contract)
- [x] T010 Implement `DefaultRateLimiter` implementation in `src/main/java/com/ratelimiter/core/DefaultRateLimiter.java`
- [x] T011 Implement `RateLimiterFactory` in `src/main/java/com/ratelimiter/factory/RateLimiterFactory.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Request Filtering (Priority: P1) 🎯 MVP

**Goal**: Restrict requests based on IP, User ID, or API Key using a basic algorithm.

**Independent Test**: Verify that requests for a specific identifier are allowed/blocked based on a fixed limit.

### Tests for User Story 1
- [x] T012 [P] [US1] Create unit test for `FixedWindowAlgorithm` in `src/test/java/com/ratelimiter/algorithms/FixedWindowAlgorithmTest.java`
- [x] T013 [P] [US1] Create integration test for IP-based limiting in `src/test/java/com/ratelimiter/core/IpRateLimiterTest.java`

### Implementation for User Story 1
- [x] T014 [P] [US1] Implement `FixedWindowState` in `src/main/java/com/ratelimiter/model/FixedWindowState.java`
- [x] T015 [US1] Implement `FixedWindowAlgorithm` in `src/main/java/com/ratelimiter/algorithms/FixedWindowAlgorithm.java`
- [x] T016 [US1] Update `RateLimiterFactory` to support `FIXED_WINDOW`

**Checkpoint**: User Story 1 functional - basic filtering works.

---

## Phase 4: User Story 2 - Pluggable Algorithms (Priority: P1)

**Goal**: Support Token Bucket, Sliding Window Log, and Leaky Bucket algorithms.

**Independent Test**: Verify each algorithm correctly enforces limits according to its specific logic (e.g., burst handling).

### Tests for User Story 2
- [x] T017 [P] [US2] Create unit test for `TokenBucketAlgorithm` in `src/test/java/com/ratelimiter/algorithms/TokenBucketAlgorithmTest.java`
- [x] T018 [P] [US2] Create unit test for `SlidingWindowLogAlgorithm` in `src/test/java/com/ratelimiter/algorithms/SlidingWindowLogAlgorithmTest.java`
- [x] T019 [P] [US2] Create unit test for `LeakyBucketAlgorithm` in `src/test/java/com/ratelimiter/algorithms/LeakyBucketAlgorithmTest.java`

### Implementation for User Story 2
- [x] T020 [P] [US2] Implement `TokenBucketState` and `TokenBucketAlgorithm`
- [x] T021 [P] [US2] Implement `SlidingWindowLogState` and `SlidingWindowLogAlgorithm`
- [x] T022 [P] [US2] Implement `LeakyBucketState` and `LeakyBucketAlgorithm`
- [x] T023 [US2] Update `RateLimiterFactory` to support all new algorithms

**Checkpoint**: User Story 2 functional - all 4 algorithms are pluggable.

---

## Phase 5: User Story 3 - Criteria-Based Limiting (Priority: P2)

**Goal**: Ensure different criteria (IP, User ID, API Key) can have independent limits.

**Independent Test**: Configure a limiter with multiple criteria and verify they don't interfere.

### Tests for User Story 3
- [x] T024 [P] [US3] Create integration test for multi-criteria limiting in `src/test/java/com/ratelimiter/core/MultiCriteriaLimiterTest.java`

### Implementation for User Story 3
- [x] T025 [US3] Refactor `DefaultRateLimiter` or `InMemoryStorage` to ensure isolation between different criteria types for the same identifier if necessary (or verify existing isolation).

**Checkpoint**: User Story 3 functional - tiered limiting possible.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Concurrency validation, Driver program, and final documentation.

- [x] T026 [P] Implement `RateLimiterDriver` in `src/main/java/com/ratelimiter/driver/RateLimiterDriver.java` to showcase concurrent requests and algorithm switching.
- [x] T027 [P] Create a high-concurrency stress test in `src/test/java/com/ratelimiter/core/ConcurrencyStressTest.java`
- [x] T028 Update `README.md` (if created) or finalize `quickstart.md` with verified code snippets.
- [x] T029 [P] Run all tests and verify SC-003/SC-004 performance goals.

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Phase 1. BLOCKS all user stories.
- **User Stories (Phase 3-5)**: Depend on Phase 2. US1 and US2 can be done in parallel if interfaces are stable. US3 depends on US1/US2 implementation patterns.
- **Polish (Phase 6)**: Depends on all user stories.

### Parallel Opportunities
- All [P] tasks within a phase can run in parallel.
- Algorithm implementations in US2 (T020, T021, T022) can run in parallel.
- Unit tests for different algorithms can be written in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 & 2.
2. Complete Phase 3 (Fixed Window + IP filtering).
3. Validate with `FixedWindowAlgorithmTest`.

### Incremental Delivery
1. Foundation -> Core interfaces and storage ready.
2. US1 -> Basic functionality (Fixed Window).
3. US2 -> Advanced algorithms (Token Bucket, etc.).
4. US3 -> Tiered limiting support.
5. Polish -> Driver program and performance validation.
