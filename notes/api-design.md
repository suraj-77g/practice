# API Design Essentials

> Concise guide for system design interviews. Focus on making good choices quickly, not perfect API specs.

---

## Quick Decision: Which API Protocol?

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         START HERE                                      │
│                             │                                           │
│                             ▼                                           │
│                    Is it user-facing?                                   │
│                      /          \                                       │
│                    YES           NO                                     │
│                    /              \                                     │
│                   ▼                ▼                                    │
│         Need flexible         Internal service                         │
│         data fetching?        communication?                           │
│           /      \                  │                                   │
│         YES       NO                ▼                                   │
│          │         │             gRPC/RPC                               │
│          ▼         ▼           (binary, fast)                          │
│       GraphQL    REST                                                   │
│                (default)                                                │
└─────────────────────────────────────────────────────────────────────────┘

TL;DR: Default to REST. It covers 90% of cases.
```

---

## 1. REST API

### Concept
Resource-oriented API using HTTP methods. Resources are **things** (nouns), not **actions** (verbs).

### Resource Modeling
```
Think: What EXISTS in your system? (not what users DO)

Ticketmaster example:
  Resources: events, venues, tickets, bookings

  GET    /events                 # List all events
  GET    /events/{id}            # Get specific event
  GET    /events/{id}/tickets    # Tickets for an event
  POST   /events/{id}/bookings   # Create booking
  GET    /bookings/{id}          # Get specific booking
  DELETE /bookings/{id}          # Cancel booking

Rules:
  ✓ Resources are plural nouns (events, not event)
  ✓ Use path for hierarchy (/events/{id}/tickets)
  ✓ Use query params for filters (/events?city=NYC&date=2024-01-01)
```

### HTTP Methods
```
┌──────────┬─────────────────────────┬────────────┬────────────────────────┐
│ Method   │ Purpose                 │ Idempotent │ Example                │
├──────────┼─────────────────────────┼────────────┼────────────────────────┤
│ GET      │ Read resource           │ Yes        │ GET /events/123        │
│ POST     │ Create new resource     │ NO         │ POST /events/123/book  │
│ PUT      │ Replace entire resource │ Yes        │ PUT /users/456         │
│ PATCH    │ Update part of resource │ Yes        │ PATCH /users/456       │
│ DELETE   │ Remove resource         │ Yes        │ DELETE /bookings/789   │
└──────────┴─────────────────────────┴────────────┴────────────────────────┘

Idempotent = calling multiple times has same effect as calling once.
POST is NOT idempotent → retry creates duplicates (need idempotency keys).
```

### Passing Data to APIs
```
┌─────────────────────────────────────────────────────────────────────────┐
│  1. PATH PARAMETERS → Identify specific resource (required)             │
│  ─────────────────────────────────────────────────────────────          │
│  /events/123                                                            │
│  /users/456/orders/789                                                  │
│                                                                         │
│  Without the ID, request doesn't make sense.                            │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  2. QUERY PARAMETERS → Filter/modify results (optional)                 │
│  ────────────────────────────────────────────────────────               │
│  /events?city=NYC&date=2024-01-01&limit=20                              │
│  /tickets?event_id=123&section=VIP                                      │
│                                                                         │
│  Can request without them, results just aren't filtered.                │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  3. REQUEST BODY → Data payload for create/update (POST, PUT, PATCH)    │
│  ───────────────────────────────────────────────────────────────────    │
│  POST /events/123/bookings                                              │
│  {                                                                      │
│    "tickets": [{"section": "VIP", "quantity": 2}],                      │
│    "payment_method": "credit_card"                                      │
│  }                                                                      │
└─────────────────────────────────────────────────────────────────────────┘

Combined example:
  POST /events/123/bookings?notify=true
  Body: {"tickets": [...], "payment_method": "credit_card"}

  - Path: which event (required)
  - Query: send notification? (optional behavior)
  - Body: booking details (the actual data)
```

### HTTP Status Codes
```
┌───────┬─────────────────────────────────────────────────────────────────┐
│ Code  │ Meaning                                                         │
├───────┼─────────────────────────────────────────────────────────────────┤
│ 200   │ OK - Success                                                    │
│ 201   │ Created - Resource created (POST success)                       │
│ 204   │ No Content - Success, nothing to return (DELETE)                │
├───────┼─────────────────────────────────────────────────────────────────┤
│ 400   │ Bad Request - Invalid input (client's fault)                    │
│ 401   │ Unauthorized - Not authenticated (who are you?)                 │
│ 403   │ Forbidden - Authenticated but not allowed (you can't do this)   │
│ 404   │ Not Found - Resource doesn't exist                              │
│ 409   │ Conflict - Resource state conflict (duplicate, version mismatch)│
│ 429   │ Too Many Requests - Rate limited                                │
├───────┼─────────────────────────────────────────────────────────────────┤
│ 500   │ Internal Server Error - Server's fault                          │
│ 502   │ Bad Gateway - Upstream service failed                           │
│ 503   │ Service Unavailable - Server overloaded/maintenance             │
└───────┴─────────────────────────────────────────────────────────────────┘

Key distinction: 4xx = client's fault, 5xx = server's fault
```

### Nested vs Flat Resources
```
When to use nested (path):
  /events/{id}/tickets     → Tickets ALWAYS belong to an event
  /users/{id}/orders       → Orders ALWAYS belong to a user

When to use flat (query params):
  /tickets?event_id=123&section=VIP   → Multiple optional filters
  /orders?user_id=456&status=pending  → Flexible querying

Rule of thumb:
  - Required relationship → nested path
  - Optional filter → query param
```

---

## 2. GraphQL

### Concept
Single endpoint with query language. Client specifies exactly what data it needs.

### Problem It Solves
```
REST Problem:

  Mobile app needs:        Web dashboard needs:
    - event name             - event name, date, description
    - date                   - venue name, address, capacity
                             - all tickets with pricing
                             - sales analytics

  REST solutions (both bad):
    1. Multiple endpoints → maintenance nightmare
    2. One fat endpoint → mobile over-fetches MBs of unused data

GraphQL solution:
  Single endpoint, client asks for exactly what it needs.
```

### How It Works
```
Query (client sends):
┌─────────────────────────────────────────┐
│  query {                                │
│    event(id: "123") {                   │
│      name                               │
│      date                               │
│      venue {                            │
│        name                             │
│        address                          │
│      }                                  │
│      tickets {                          │
│        section                          │
│        price                            │
│      }                                  │
│    }                                    │
│  }                                      │
└─────────────────────────────────────────┘

Response (server returns exactly this shape):
┌─────────────────────────────────────────┐
│  {                                      │
│    "event": {                           │
│      "name": "Taylor Swift",            │
│      "date": "2024-06-15",              │
│      "venue": {                         │
│        "name": "Madison Square Garden", │
│        "address": "4 Penn Plaza, NYC"   │
│      },                                 │
│      "tickets": [                       │
│        {"section": "VIP", "price": 500},│
│        {"section": "GA", "price": 150}  │
│      ]                                  │
│    }                                    │
│  }                                      │
└─────────────────────────────────────────┘
```

### GraphQL Schema
```
type Event {
  id: ID!
  name: String!
  date: DateTime!
  venue: Venue!
  tickets: [Ticket!]!
}

type Venue {
  id: ID!
  name: String!
  address: String!
}

type Query {
  event(id: ID!): Event
  events(limit: Int, after: String): [Event!]!
}

type Mutation {
  createBooking(eventId: ID!, tickets: [TicketInput!]!): Booking
}
```

### N+1 Problem (GraphQL's Gotcha)
```
Query: Get 100 events with their venues

Naive implementation:
  1 query: SELECT * FROM events LIMIT 100
  100 queries: SELECT * FROM venues WHERE id = ?  (one per event)
  = 101 database queries 😱

Solution: DataLoader (batching)
  1 query: SELECT * FROM events LIMIT 100
  1 query: SELECT * FROM venues WHERE id IN (1,2,3,...100)
  = 2 database queries ✓
```

### When to Use
```
✓ Mobile + web need different data from same backend
✓ Frontend teams need to iterate without backend changes
✓ Complex, nested data relationships
✓ Interviewer mentions "over-fetching" or "under-fetching"

✗ Simple CRUD (REST is simpler)
✗ File uploads (GraphQL awkward for this)
✗ Real-time (use WebSockets alongside)
```

---

## 3. gRPC / RPC

### Concept
Action-oriented API. Call remote functions as if they were local. Binary protocol (faster than JSON).

### REST vs RPC Mental Model
```
REST (resource-oriented):           RPC (action-oriented):
  GET /events/123                     getEvent(eventId: "123")
  POST /events/123/bookings           createBooking(eventId, userId, tickets)
  GET /users/456/permissions          checkPermission(userId, resource)

REST: "What things exist and how do I manipulate them?"
RPC:  "What actions can I perform?"
```

### Protocol Buffers (Protobuf)
```
Define service contract in .proto file:

service TicketService {
  rpc GetEvent(GetEventRequest) returns (Event);
  rpc CreateBooking(CreateBookingRequest) returns (Booking);
  rpc StreamUpdates(EventId) returns (stream Update);  // streaming!
}

message GetEventRequest {
  string event_id = 1;
}

message Event {
  string id = 1;
  string name = 2;
  int64 date = 3;
  Venue venue = 4;
}

Benefits:
  - Generates client/server code in multiple languages
  - Compile-time type safety (catch errors before deploy)
  - Binary format (~10x smaller than JSON)
  - HTTP/2 (multiplexing, streaming)
```

### When to Use
```
✓ Internal service-to-service communication
✓ Performance critical (binary is faster than JSON)
✓ Polyglot environment (Go, Java, Python all speak protobuf)
✓ Streaming needed (gRPC has built-in support)
✓ Interviewer mentions "microservices" or "internal APIs"

✗ Public APIs (REST is more accessible)
✗ Browser clients (limited gRPC-web support)
✗ Simple systems (overkill)
```

### Common Pattern: REST External, gRPC Internal
```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   Mobile/Web ──── REST ────► API Gateway ──── gRPC ────► Services       │
│                                   │                                     │
│                                   │         ┌──────────────┐            │
│                                   ├─ gRPC ──│ Booking Svc  │            │
│                                   │         └──────────────┘            │
│                                   │         ┌──────────────┐            │
│                                   ├─ gRPC ──│ Payment Svc  │            │
│                                   │         └──────────────┘            │
│                                   │         ┌──────────────┐            │
│                                   └─ gRPC ──│ Inventory Svc│            │
│                                             └──────────────┘            │
│                                                                         │
│   External: REST (easy for clients)                                     │
│   Internal: gRPC (fast, type-safe)                                      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Pagination

### Problem
```
GET /events → Returns 10 million events → 💥 Server dies, client dies

Solution: Return data in pages.
```

### Offset-Based Pagination
```
GET /events?offset=0&limit=20    → Records 1-20
GET /events?offset=20&limit=20   → Records 21-40
GET /events?offset=40&limit=20   → Records 41-60

Implementation:
  SELECT * FROM events ORDER BY created_at LIMIT 20 OFFSET 40

Pros:
  ✓ Simple to implement
  ✓ Can "jump to page 5"

Cons:
  ✗ Slow on large offsets (DB scans all skipped rows)
  ✗ Inconsistent if data changes while paginating
     (new record added → you see duplicate or miss one)
```

### Cursor-Based Pagination
```
GET /events?limit=20
Response:
{
  "events": [...],
  "next_cursor": "eyJpZCI6MTAwfQ=="   // encoded: {"id": 100}
}

GET /events?cursor=eyJpZCI6MTAwfQ==&limit=20
Response:
{
  "events": [...],
  "next_cursor": "eyJpZCI6MTIwfQ=="
}

Implementation:
  SELECT * FROM events WHERE id > 100 ORDER BY id LIMIT 20

Pros:
  ✓ Fast (uses index, no offset scan)
  ✓ Stable (not affected by new records)

Cons:
  ✗ Can't jump to arbitrary page
  ✗ Cursor must be opaque (encoded)
```

### When to Use Which
```
Offset-based:
  - Admin dashboards (small datasets, need page jumping)
  - Static content (doesn't change often)

Cursor-based:
  - Infinite scroll feeds (Twitter, Instagram)
  - Large datasets
  - Real-time data (new items being added)
  - High-traffic APIs
```

---

## 5. API Versioning

### Problem
```
API evolves, but old clients still exist.

v1: GET /users/123 → {"name": "John", "email": "john@x.com"}

v2: GET /users/123 → {"full_name": "John Doe", "email": "john@x.com"}
                      ↑ renamed field, broke all v1 clients
```

### URL Versioning (Recommended)
```
GET /v1/users/123
GET /v2/users/123

Pros:
  ✓ Explicit, visible in URL
  ✓ Easy to route (different code paths)
  ✓ Easy to test (just change URL)

Cons:
  ✗ URL "clutter"
```

### Header Versioning
```
GET /users/123
Accept-Version: v2

or

GET /users/123
API-Version: 2

Pros:
  ✓ Clean URLs

Cons:
  ✗ Hidden, easy to forget
  ✗ Harder to test in browser
```

### Interview Tip
```
Usually don't need to mention versioning unless asked.
If asked, say: "I'd use URL versioning (/v1/...) for clarity."
```

---

## 6. Authentication & Authorization

### The Difference
```
Authentication: WHO are you? (identity)
Authorization:  WHAT can you do? (permissions)

Example:
  1. User logs in → authenticated as john@example.com
  2. John tries to delete someone else's booking
  3. Server checks: is John allowed? → NO (authorization fails)
```

### API Keys
```
For: Server-to-server, 3rd party developers

GET /events
Authorization: Bearer sk_live_abc123def456...

How it works:
  1. Generate unique key per client
  2. Store in DB with permissions
  3. Client sends key with every request
  4. Server looks up key, checks permissions

Use for:
  ✓ Internal services
  ✓ External developer APIs (Stripe, Twilio)

Not for:
  ✗ User-facing apps (users shouldn't manage keys)
```

### JWT (JSON Web Tokens)
```
For: User sessions in web/mobile apps

How it works:
  1. User logs in with username/password
  2. Server creates signed JWT with user info
  3. Client stores JWT, sends with every request
  4. Server verifies signature, extracts user info (no DB lookup!)

┌─────────────────────────────────────────────────────────────────────────┐
│  JWT Structure (3 parts, base64 encoded, separated by dots)             │
│                                                                         │
│  eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMTIzIn0.signature                 │
│  \_____header_____/ \______payload_______/ \_signature_/                │
│                                                                         │
│  Header:  {"alg": "HS256", "typ": "JWT"}                                │
│  Payload: {"user_id": "123", "role": "customer", "exp": 1640995200}     │
│  Signature: HMAC(header + payload, secret_key)                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

Benefits:
  ✓ Stateless (no session storage on server)
  ✓ Self-contained (carries user info)
  ✓ Works across services (any service can verify)
```

### Role-Based Access Control (RBAC)
```
Define roles with permissions:

┌─────────────────┬─────────────────────────────────────────────────────┐
│ Role            │ Permissions                                         │
├─────────────────┼─────────────────────────────────────────────────────┤
│ customer        │ book tickets, view own bookings, cancel own booking │
│ venue_manager   │ create events, view sales for their venues          │
│ admin           │ everything                                          │
└─────────────────┴─────────────────────────────────────────────────────┘

Assign users to roles:
  john@example.com → customer
  manager@venue.com → venue_manager

Check on every request:
  1. Authenticate (valid JWT?)
  2. Authorize (does role allow this action on this resource?)
```

---

## 7. Rate Limiting

### Problem
```
Without limits:
  - One bad actor sends 1M requests/second
  - Your servers crash
  - All users affected
```

### Common Strategies
```
┌─────────────────────────────────────────────────────────────────────────┐
│  Per-User Limits                                                        │
│  ───────────────                                                        │
│  Authenticated user: 1000 requests/hour                                 │
│  Track by: user_id from JWT                                             │
│                                                                         │
│  Example: Stripe API → 100 requests/second per API key                  │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  Per-IP Limits (for unauthenticated)                                    │
│  ───────────────────────────────────                                    │
│  100 requests/minute per IP                                             │
│  Problem: Many users behind same NAT/proxy                              │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  Per-Endpoint Limits                                                    │
│  ───────────────────                                                    │
│  POST /bookings → 10 requests/minute (prevent ticket scalping)          │
│  GET /events    → 1000 requests/minute (read-heavy, allow more)         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Response When Limited
```
HTTP 429 Too Many Requests

Headers:
  X-RateLimit-Limit: 1000
  X-RateLimit-Remaining: 0
  X-RateLimit-Reset: 1640995200  (Unix timestamp when limit resets)
  Retry-After: 3600              (seconds until retry)
```

---

## 8. Idempotency

### Problem
```
User clicks "Pay" → request times out → did it go through?

If client retries:
  - Payment succeeds again → user charged twice! 💸
```

### Solution: Idempotency Keys
```
Client generates unique key per operation:

POST /payments
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
{
  "amount": 100,
  "currency": "USD"
}

Server logic:
  1. Check: have I seen this idempotency key before?
  2. If yes: return cached response (don't process again)
  3. If no: process payment, cache response with key

Result: Retry-safe. Same key = same response, no duplicate charge.
```

### Which Methods Need It?
```
GET, PUT, DELETE → Already idempotent (safe to retry)
POST             → NOT idempotent (needs idempotency key)

Critical for:
  - Payments
  - Order creation
  - Any state-changing POST
```

---

## Quick Reference

### API Protocol Decision
```
User-facing, simple CRUD?         → REST
Need flexible client queries?     → GraphQL
Internal services, performance?   → gRPC
Real-time updates?                → WebSocket/SSE (not traditional API)
```

### REST Checklist
```
□ Resources are nouns (plural): /events, /bookings
□ Use correct HTTP methods: GET read, POST create, PUT/PATCH update, DELETE remove
□ Path params for IDs: /events/{id}
□ Query params for filters: /events?city=NYC
□ Body for data payload: POST body with JSON
□ Proper status codes: 2xx success, 4xx client error, 5xx server error
□ Pagination for lists: cursor-based for large/dynamic data
□ Auth: JWT for users, API keys for services
□ Rate limiting: protect your system
□ Idempotency keys for POST: prevent duplicates
```

### Interview Time Budget
```
┌─────────────────────────────────────────────────────────────────────────┐
│  API Design: ~5 minutes (out of 45-60 min interview)                    │
│                                                                         │
│  - List 3-5 key endpoints                                               │
│  - Mention auth requirement                                             │
│  - Move on to system architecture                                       │
│                                                                         │
│  Common mistake: Spending 15 min perfecting API when bigger             │
│  architectural challenges await.                                        │
└─────────────────────────────────────────────────────────────────────────┘
```
