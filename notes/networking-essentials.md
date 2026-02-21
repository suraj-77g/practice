# Networking Essentials for System Design

A structured reference for understanding networking concepts in distributed systems.

---

## 1. Protocol Stack (OSI Layers)

### Concept
Layered abstraction for network communication. Each layer builds on the one below, hiding complexity.

### Problem It Solves
Without layers, every application would need to handle electrical signals, routing, reliability, and data formatting. Layers let you focus on your level of abstraction.

### Diagram
```
┌───────────────────────────────────────────────────┐
│  L7 - Application   HTTP, WebSocket, gRPC, DNS    │ ← User Space (flexible)
├───────────────────────────────────────────────────┤
│  L4 - Transport     TCP, UDP, QUIC                │ ← Kernel Space (fast)
├───────────────────────────────────────────────────┤
│  L3 - Network       IP (routing & addressing)     │
└───────────────────────────────────────────────────┘
```

### Real-World Example
When you call `fetch('https://api.stripe.com/charges')`:
- L7 (HTTP) formats your request with headers and JSON body
- L4 (TCP) ensures reliable, ordered delivery
- L3 (IP) routes packets to Stripe's servers

### Big Picture
Higher layers = more features, more overhead. Lower layers = faster, harder to change.

### Key Points
- L3/L4 run in kernel (fast, OS-level)
- L7 runs in user space (your app, flexible)
- Each layer adds headers → more bytes per request

### When to Apply
- **Default**: Work at L7 (HTTP/REST). Let the OS handle L3/L4.
- **Optimize**: Drop to lower layers (raw TCP, UDP) only when L7 overhead is the bottleneck.

---

## 2. DNS (Domain Name System)

### Concept
Distributed database that maps human-readable names to IP addresses.

### Problem It Solves
Humans remember `google.com`, not `142.250.80.46`. IPs change; names don't have to.

### Diagram
```
Browser            Resolver         Root        TLD         Authoritative
   │                  │              │           │               │
   │─ stripe.com ────►│              │           │               │
   │                  │─ . ─────────►│           │               │
   │                  │◄─ .com NS ───│           │               │
   │                  │─ .com ──────────────────►│               │
   │                  │◄─ stripe.com NS ─────────│               │
   │                  │─ stripe.com ────────────────────────────►│
   │                  │◄─ 54.187.32.1 ───────────────────────────│
   │◄─ 54.187.32.1 ───│              │           │               │
```

### Real-World Example
- **Netflix**: DNS returns different IPs based on user location → routes to nearest CDN edge
- **AWS Route 53**: Health-checked DNS removes failed endpoints automatically

### Big Picture
DNS is the internet's phone book. It's also a simple load balancer and failover mechanism.

### Key Points
- **TTL**: How long to cache (60s–24hrs typical)
- **Propagation delay**: Changes take time to spread globally
- **Round-robin**: Return rotated IP list for basic load balancing

### When to Apply
- **Failover**: Multiple IPs, DNS removes unhealthy ones
- **Global routing**: GeoDNS routes users to nearest datacenter
- **Avoid for**: Fine-grained load balancing (use dedicated LB instead)

---

## 3. TCP vs UDP

### Concept
Two transport protocols with opposite tradeoffs: reliability vs speed.

### Problem It Solves
- **TCP**: Guarantees delivery and order. Essential when data must arrive intact.
- **UDP**: Fast, no guarantees. Essential when speed matters more than completeness.

### Diagram
```
TCP (Connection-Oriented):          UDP (Connectionless):

Client         Server               Client         Server
  │─── SYN ──────►│                   │─── data ─────►│
  │◄── SYN-ACK ───│                   │─── data ─────►│
  │─── ACK ──────►│  1.5 RTT setup    │─── data ─────►│  No setup
  │═══ DATA ═════►│                   │    (fire and forget)
  │◄── ACK ───────│  Every packet     │
```

### Real-World Example
- **TCP**: Stripe API (payment must not be lost), database connections
- **UDP**: Zoom video (dropped frame = minor glitch, waiting for retransmit = visible lag)
- **Both**: Discord uses TCP for chat, UDP for voice

### Big Picture
| Feature     | TCP              | UDP             |
|-------------|------------------|-----------------|
| Reliability | Guaranteed       | Best-effort     |
| Ordering    | Maintained       | None            |
| Overhead    | 20-60 byte header| 8 byte header   |
| Use case    | APIs, databases  | Video, gaming   |

### Key Points
- TCP retransmits lost packets → adds latency under loss
- UDP is "fire and forget" → app handles loss
- Browsers only support UDP via WebRTC

### When to Apply
- **TCP**: Default for everything
- **UDP**: Real-time media, gaming, high-volume telemetry where loss is acceptable

---

## 4. HTTP/HTTPS

### Concept
Stateless request-response protocol for the web. HTTPS adds encryption.

### Problem It Solves
Standard way for clients and servers to exchange data. Stateless = simple scaling (any server can handle any request).

### Diagram
```
Client                              Server
  │                                   │
  │─── GET /users/123 ───────────────►│
  │    Headers: Auth, Accept          │
  │                                   │
  │◄── 200 OK ────────────────────────│
  │    Headers: Content-Type          │
  │    Body: {"name": "Alice"}        │
```

### Real-World Example
Every web API. Stripe, Twilio, GitHub—all use HTTP/HTTPS with JSON.

### Big Picture
```
Status Codes:
  2xx → Success (200 OK, 201 Created)
  3xx → Redirect (301 Moved, 302 Found)
  4xx → Client error (400 Bad Request, 401 Unauthorized, 404 Not Found, 429 Rate Limited)
  5xx → Server error (500 Internal Error, 502 Bad Gateway, 503 Unavailable)
```

### Key Points
- **Stateless**: Server doesn't remember previous requests
- **HTTPS**: Encrypts content, but doesn't validate it came from YOUR client
- **Keep-Alive**: Reuse TCP connection for multiple requests
- **HTTP/2**: Multiplexed streams over single connection

### When to Apply
- **Always use HTTPS** for production
- **HTTP/2**: Enable for better performance (parallel requests, header compression)
- **Security**: Never trust user-supplied IDs in request body without server-side validation

---

## 5. REST API

### Concept
Architectural style: model everything as resources, use HTTP verbs for operations.

### Problem It Solves
Consistent, predictable API design. Clients know what to expect without reading docs for every endpoint.

### Diagram
```
Resource-based thinking:

GET    /users/123         → Read user
POST   /users             → Create user
PUT    /users/123         → Replace user (full update)
PATCH  /users/123         → Modify user (partial update)
DELETE /users/123         → Delete user

Nested resources:
GET    /users/123/orders  → User's orders
```

### Real-World Example
- **GitHub API**: `GET /repos/owner/repo/issues`
- **Stripe**: `POST /v1/charges`, `GET /v1/customers/{id}`

### Big Picture
REST is the default for public APIs. Simple, widely understood, works with any HTTP client.

### Key Points
- **Think resources, not methods**: `PATCH /games/1 {status: "started"}` not `POST /startGame`
- **Idempotent**: GET, PUT, DELETE are safe to retry
- **Not idempotent**: POST (create) needs idempotency keys for safe retry

### When to Apply
- **Public APIs**: Always default to REST
- **Internal services**: REST works, but consider gRPC for performance
- **Avoid**: RPC-style endpoints (`/doSomething`) in REST APIs

---

## 6. gRPC

### Concept
Binary RPC framework using Protocol Buffers over HTTP/2.

### Problem It Solves
JSON is verbose and slow to parse. gRPC is 10x faster for service-to-service communication.

### Diagram
```
┌─────────────────────────────────────────────────────────┐
│  .proto definition (compiled to client/server stubs)   │
│  ─────────────────────────────────────────────────────  │
│  service UserService {                                  │
│    rpc GetUser(GetUserRequest) returns (User);          │
│    rpc StreamUpdates(Req) returns (stream Update);      │
│  }                                                      │
└─────────────────────────────────────────────────────────┘

JSON:     {"id": "123", "name": "John"}     → 40 bytes
Protobuf: 0A0331323312084A6F686E            → 15 bytes
```

### Real-World Example
- **Google**: All internal services use gRPC
- **Netflix**: Microservices communicate via gRPC
- **Pattern**: gRPC internal, REST external

```
External clients ──REST──► API Gateway ──gRPC──► Internal Services
```

### Big Picture
gRPC trades human-readability for performance. Great for internal services, awkward for public APIs.

### Key Points
- **Binary**: Faster serialization, smaller payloads
- **Streaming**: Built-in bidirectional streaming support
- **Strong typing**: Catch errors at compile time
- **No browser support**: Can't use directly from browser

### When to Apply
- **Use**: Internal service-to-service, high-throughput, latency-sensitive
- **Avoid**: Public APIs, browser clients, when debugging visibility matters

---

## 7. Server-Sent Events (SSE)

### Concept
Server pushes events to client over a single long-lived HTTP connection.

### Problem It Solves
Client needs real-time updates from server without polling. Simpler than WebSockets when communication is one-way.

### Diagram
```
Client                          Server
  │                                │
  │─── GET /events ───────────────►│
  │                                │
  │◄── data: {"price": 142.50} ────│
  │◄── data: {"price": 142.55} ────│  Streaming response
  │◄── data: {"price": 142.48} ────│  (one HTTP response, many events)
  │          ...                   │
  │                                │
  │    (connection drops)          │
  │─── GET /events?lastId=123 ────►│  Auto-reconnect with last ID
```

### Real-World Example
- **Auction sites**: Live bid updates
- **Deployment tools**: Streaming build logs
- **Stock tickers**: Real-time price feeds

### Big Picture
SSE is simpler than WebSockets but one-way only. Good for "server broadcasts to clients" patterns.

### Key Points Explained

**One-way: Server → Client only**
```
Client                          Server
  │─── GET /events ───────────────►│  (one request)
  │◄── data: {"msg": "hello"} ─────│
  │◄── data: {"msg": "world"} ─────│  (many responses)
  │                                │
  ✗ Client cannot send messages back on this connection
    (must use separate HTTP request if needed)
```

**Auto-reconnect: EventSource API handles it**
```javascript
// Browser code - reconnection is automatic
const events = new EventSource('/events');
events.onmessage = (e) => console.log(e.data);
// If connection drops, browser retries after ~3 seconds
// No manual retry logic needed
```

**Last-Event-ID: Resume from where you left off**
```
Server sends:
  id: 101
  data: {"price": 50}
  
  id: 102
  data: {"price": 51}
  
  (connection drops)

Browser reconnects with header:
  GET /events
  Last-Event-ID: 102    ← "I received up to 102"

Server resumes:
  id: 103               ← No missed events
  data: {"price": 52}
```

**Limitations explained:**

Connection timeouts (30-60s):
```
Client ──────── Load Balancer ──────── Server
                     │
            "Idle 60s? Closing."
             
Solution: Server sends periodic heartbeat
  : ping    ← SSE comment (keeps connection alive)
```

Proxy batching (breaks streaming):
```
Expected:                      Bad proxy behavior:
  data: A → see A immediately    data: A ─┐
  data: B → see B immediately    data: B  ├─► buffered
  data: C → see C immediately    data: C ─┘
                                     └─► A,B,C arrive together
```

### When to Apply
- **Use**: Notifications, live feeds, any server-push scenario
- **Avoid**: When client also needs to send frequent messages (use WebSocket)

---

## 8. WebSockets

### Concept
Persistent, bidirectional TCP connection between client and server.

### Problem It Solves
Real-time, two-way communication. Both sides can send messages anytime without new HTTP requests.

### How It Works

**Step 1: HTTP Upgrade Handshake**
```
Client ──► Server

GET /chat HTTP/1.1
Host: server.example.com
Upgrade: websocket              ← "I want to upgrade"
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==

Server ──► Client

HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=

─── TCP connection now speaks WebSocket, not HTTP ───
```

**Step 2: Bidirectional Messages**
```
Client                          Server
  │                                │
  │─── {"type":"join","room":1} ──►│  Client sends
  │                                │
  │◄── {"type":"user_joined"} ─────│  Server sends
  │◄── {"type":"message",...} ─────│  Server sends
  │                                │
  │─── {"type":"typing"} ─────────►│  Client sends
  │                                │
  (either side can send anytime - full duplex)
```

**Step 3: Connection Close**
```
Either side sends close frame → other side acknowledges → TCP closed

Client                          Server
  │─── close frame ───────────────►│
  │◄── close frame ────────────────│
  │         (TCP connection ends)  │
```

### Diagram
```
Client                          Server
  │                                │
  │─── HTTP Upgrade ──────────────►│
  │◄── 101 Switching Protocols ────│
  │                                │
  │◄══════ message ═══════════════►│  Bidirectional
  │◄══════ message ═══════════════►│  Full-duplex
  │◄══════ message ═══════════════►│
  │                                │
  (connection stays open until closed)
```

### Real-World Example
- **Slack/Discord**: Chat messages in real-time
- **Google Docs**: Collaborative editing, cursor positions
- **Trading platforms**: Order book updates + order submission

### Big Picture: Stateful = Complex

**The Problem:**
```
User connects to Server A, Server A dies, user reconnects to Server B.
Server B has no knowledge of user's state (which rooms? which subscriptions?).
```

**Solution 1: Sticky Sessions (IP Hash)**
```
┌────────┐      ┌────┐      ┌──────────┐
│ User A │─────►│ LB │─────►│ Server 1 │  LB hashes User A's IP
│ User B │─────►│    │─────►│ Server 2 │  → always routes to Server 1
│ User A │─────►│    │─────►│ Server 1 │  reconnect → same server
└────────┘      └────┘      └──────────┘

Pros: Simple, no shared state needed
Cons: Server dies → user loses state, uneven load distribution
```

**Solution 2: Shared State (Redis)**
```
┌──────────┐     ┌──────────┐
│ Server 1 │     │ Server 2 │
└────┬─────┘     └────┬─────┘
     │                │
     └───────┬────────┘
             │
       ┌─────▼─────┐
       │   Redis   │  ← All connection state stored here
       │           │    user_123: {rooms: [1,2], prefs: {...}}
       └───────────┘

User connects to Server 1 → state saved to Redis
User reconnects to Server 2 → Server 2 reads state from Redis

Pros: Any server can handle any user, survives server death
Cons: Redis latency on every state access, Redis becomes SPOF
```

**Solution 3: Pub/Sub Fanout (Redis Pub/Sub or Kafka)**
```
User A (Server 1) sends message to Room 1
Room 1 has users on Server 1, Server 2, and Server 3

┌──────────┐       ┌──────────┐       ┌──────────┐
│ Server 1 │       │ Server 2 │       │ Server 3 │
│ User A ──┼──┐    │ User B   │       │ User C   │
└──────────┘  │    └────▲─────┘       └────▲─────┘
              │         │                  │
              ▼         │                  │
         ┌────────────────────────────────────┐
         │      Redis Pub/Sub or Kafka        │
         │      Channel: "room:1"             │
         └────────────────────────────────────┘
              │         │                  │
              │    "new message"      "new message"
              │         │                  │
              │    Server 2 broadcasts  Server 3 broadcasts
              │    to its local users   to its local users

Flow:
1. User A sends message → Server 1 receives
2. Server 1 publishes to "room:1" channel
3. All servers subscribed to "room:1" receive it
4. Each server broadcasts to its local WebSocket connections

Pros: Scales horizontally, decoupled servers
Cons: More complex, need to manage subscriptions
```

### Key Points
- **Stateful**: Connection is state both sides maintain
- **Protocol agnostic**: Define your own message format (usually JSON)
- **Upgrade**: Starts as HTTP, upgrades to WebSocket protocol
- **L4 load balancer**: Required (maintains TCP connection to same backend)
- **Heartbeat/Ping**: Send periodic pings to detect dead connections

### When to Apply
- **Use**: Chat, gaming, collaborative editing, anything bidirectional + high-frequency
- **Avoid**: If SSE suffices (simpler infra), or if request-response works
- **Consider**: Complexity cost—do you really need bidirectional?

---

## 9. WebRTC

### Concept
Peer-to-peer communication directly between browsers, using UDP.

### Problem It Solves
Video/audio calls without routing all traffic through a server. Lower latency, reduced server costs.

### The NAT Problem
```
Most devices have private IPs behind a router (NAT).
Peer B can't directly reach Peer A's private IP.

┌─────────────┐                         ┌─────────────┐
│   Peer A    │                         │   Peer B    │
│ 192.168.1.5 │                         │ 10.0.0.42   │
└──────┬──────┘                         └──────┬──────┘
       │                                       │
   ┌───▼───┐                               ┌───▼───┐
   │ NAT/  │                               │ NAT/  │
   │Router │                               │Router │
   └───┬───┘                               └───┬───┘
       │                                       │
  73.42.15.99                            98.12.34.56
  (public IP)                            (public IP)

Problem: How does 98.12.34.56 reach 192.168.1.5?
Answer: STUN + TURN for NAT traversal
```

### Signaling Server (You Build This)
```
Before P2P can happen, peers must exchange connection info.
WebRTC doesn't define HOW—you implement it (WebSocket, HTTP, etc.).

┌────────┐                                    ┌────────┐
│ Peer A │                                    │ Peer B │
└───┬────┘                                    └───┬────┘
    │                                             │
    │──── 1. "Here's my SDP offer" ──────────────►│
    │     (codecs, media capabilities)            │
    │                                             │
    │◄─── 2. "Here's my SDP answer" ──────────────│
    │     (agreed codecs)                         │
    │                                             │
    │──── 3. "ICE candidate: reach me at X" ─────►│
    │◄─── 4. "ICE candidate: reach me at Y" ──────│
    │                                             │
    ▼          (all via YOUR server)              ▼
         ┌─────────────────────────┐
         │    Signaling Server     │
         │  (WebSocket, Firebase,  │
         │   or any messaging)     │
         └─────────────────────────┘
```

### STUN (Discover Your Public IP)
```
STUN = Session Traversal Utilities for NAT

Peer asks STUN server: "What's my public IP and port?"
STUN replies: "You're at 73.42.15.99:54321"

┌────────┐                         ┌─────────────┐
│ Peer A │─── "What's my IP?" ────►│ STUN Server │
│        │                         │ (free, e.g. │
│        │◄── "73.42.15.99:54321" ─│  Google's)  │
└────────┘                         └─────────────┘

This also "punches a hole" in NAT:
  - Outbound packet creates NAT mapping
  - NAT now accepts inbound on that port
  - Peer B can send to 73.42.15.99:54321 → reaches Peer A

Works ~80-85% of the time. Fails with strict/symmetric NAT.
```

### TURN (Relay Fallback)
```
TURN = Traversal Using Relays around NAT

When direct connection fails (firewalls, symmetric NAT),
all traffic goes through a relay server.

┌────────┐                              ┌────────┐
│ Peer A │                              │ Peer B │
└───┬────┘                              └───┬────┘
    │                                       │
    │         ┌─────────────┐               │
    └────────►│ TURN Server │◄──────────────┘
              │   (relay)   │
              └─────────────┘
                    │
            All media flows
            through server

Downsides:
  - Adds latency (not truly P2P)
  - Costs money (you pay for bandwidth)
  - But guarantees connectivity
```

### Full Connection Flow
```
   PEER A                 SIGNALING               STUN                 PEER B
     │                     SERVER                SERVER                  │
     │                        │                    │                     │
     │ ──────────────────────────────────────────────────────────────────│
     │   PHASE 1: DISCOVER PUBLIC IPs (via STUN)                         │
     │ ──────────────────────────────────────────────────────────────────│
     │                        │                    │                     │
     │───── "What's my IP?" ──────────────────────►│                     │
     │◄──── "73.42.15.99:54321" ───────────────────│                     │
     │                        │                    │                     │
     │                        │                    │◄── "What's my IP?" ─│
     │                        │                    │─ "98.12.34.56:123" ─►│
     │                        │                    │                     │
     │ ──────────────────────────────────────────────────────────────────│
     │   PHASE 2: EXCHANGE CONNECTION INFO (via Signaling)               │
     │ ──────────────────────────────────────────────────────────────────│
     │                        │                    │                     │
     │── SDP Offer ──────────►│                    │                     │
     │   (my codecs, caps)    │── SDP Offer ──────────────────────────►│
     │                        │                    │                     │
     │                        │◄───────────────────────── SDP Answer ────│
     │◄───── SDP Answer ──────│                    │      (agreed codecs)│
     │                        │                    │                     │
     │── ICE Candidates ─────►│                    │                     │
     │   (ways to reach me)   │── ICE Candidates ─────────────────────►│
     │                        │                    │                     │
     │                        │◄────────────────────── ICE Candidates ───│
     │◄── ICE Candidates ─────│                    │   (ways to reach me)│
     │                        │                    │                     │
     │ ──────────────────────────────────────────────────────────────────│
     │   PHASE 3: CONNECT DIRECTLY (P2P)                                 │
     │ ──────────────────────────────────────────────────────────────────│
     │                        │                    │                     │
     │◄═══════════════════ Direct UDP (video/audio) ════════════════════►│
     │                        │                    │                     │
     │   (If direct fails → fallback to TURN relay server)               │
     │                        │                    │                     │
```

### Real-World Example
- **Zoom/Google Meet**: Video conferencing (TURN for reliability)
- **Discord**: Voice channels
- **Figma**: Multiplayer cursors

### Key Points
- **Signaling**: You build it (exchange SDP + ICE candidates)
- **STUN**: Free, discovers public IP, punches hole in NAT
- **TURN**: Expensive fallback, relays all traffic (~15-20% of calls need it)
- **UDP-based**: Tolerates packet loss (video glitch > freeze)
- **Encrypted**: All WebRTC traffic is secured by default

### When to Apply
- **Use**: Video/audio calling, screen sharing, P2P file transfer
- **Avoid**: Everything else—it's complex and overkill
- **Consider**: Do you really need P2P? Server relay is often simpler.

---

## 10. Load Balancing

### Concept
Distribute traffic across multiple servers to handle more load and provide redundancy.

### Problem It Solves
Single server can't handle all traffic. Need to spread load and survive server failures.

### Diagram
```
Client-Side LB:                     Dedicated LB:

┌────────┐   ┌───────────┐          ┌────────┐   ┌────┐   ┌──────────┐
│ Client │──►│ Registry  │          │ Client │──►│ LB │──►│ Server 1 │
│        │◄──│ (get list)│          │        │   │    │──►│ Server 2 │
│        │──────────────►│Server 1  │        │   │    │──►│ Server 3 │
└────────┘               │Server 2  └────────┘   └────┘   └──────────┘
                         └─────────
```

### L4 vs L7 Load Balancing
```
┌─────────────────────────────────────────────────────────────────────────┐
│                        L4 (Transport Layer)                             │
├─────────────────────────────────────────────────────────────────────────┤
│  Routes by: IP address + port                                           │
│  Sees: TCP/UDP packets (no HTTP awareness)                              │
│  Connection: Passes through (NAT or DSR)                                │
│  Speed: Very fast, minimal processing                                   │
│                                                                         │
│  ┌────────┐        ┌────────┐        ┌──────────┐                       │
│  │ Client │───TCP──│  L4 LB │───TCP──│  Server  │                       │
│  └────────┘        └────────┘        └──────────┘                       │
│              (same TCP connection, just routed)                         │
│                                                                         │
│  Use for: WebSockets, databases, any TCP/UDP, high throughput           │
│  Examples: AWS NLB, HAProxy (TCP mode)                                  │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                        L7 (Application Layer)                           │
├─────────────────────────────────────────────────────────────────────────┤
│  Routes by: URL path, headers, cookies, HTTP method                     │
│  Sees: Full HTTP request (decrypts TLS if needed)                       │
│  Connection: Terminates client connection, opens new to backend         │
│  Speed: Slower, but much smarter routing                                │
│                                                                         │
│  ┌────────┐        ┌────────┐        ┌──────────┐                       │
│  │ Client │───TLS──│  L7 LB │───TCP──│  Server  │                       │
│  └────────┘        └────────┘        └──────────┘                       │
│              (two separate connections)                                 │
│                                                                         │
│  Use for: HTTP APIs, path-based routing, A/B testing, canary deploys    │
│  Examples: AWS ALB, NGINX, Envoy                                        │
└─────────────────────────────────────────────────────────────────────────┘
```

### Load Balancing Algorithms

**1. Round Robin**
```
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
Request 4 → Server A  (cycle repeats)

Example: Stateless API servers behind NGINX
  upstream api {
    server api1.example.com;
    server api2.example.com;
    server api3.example.com;
  }

✓ Simple, no state to track
✓ Works when: all servers identical, requests ~equal cost
✗ Problem: one slow request blocks server, others pile up
```

**2. Weighted Round Robin**
```
Server A (weight=3): gets 3 requests
Server B (weight=1): gets 1 request

Example: Mixed fleet with old + new hardware
  upstream api {
    server new-beefy-server.com   weight=5;  # 32 CPU cores
    server old-small-server.com   weight=1;  # 4 CPU cores
  }

Example: Canary deployment (shift traffic gradually)
  upstream api {
    server stable-v1.com  weight=9;   # 90% traffic
    server canary-v2.com  weight=1;   # 10% traffic (testing)
  }

✓ Use when: heterogeneous servers, gradual rollouts
```

**3. Least Connections**
```
Server A: 47 active connections
Server B: 12 active connections  ← next request goes here
Server C: 89 active connections

Example: Video streaming servers (connections last minutes/hours)
  Some users watch 2hr movie, others browse 30s
  Round robin would overload servers with long-watchers

Example: WebSocket chat servers
  upstream chat {
    least_conn;
    server ws1.example.com;
    server ws2.example.com;
  }

✓ Use when: requests have varying duration
✓ Use when: long-lived connections (WebSocket, streaming)
✗ Doesn't account for connection "weight" (idle vs busy)
```

**4. Least Response Time**
```
Server A: avg 50ms response, 10 connections
Server B: avg 200ms response, 5 connections  
Server C: avg 30ms response, 15 connections ← might pick this

Picks server with: lowest (active_connections × avg_response_time)

Example: Geo-distributed backends
  US user → picks US server (lower latency) over EU server
  
Example: Heterogeneous workloads
  Some servers handle expensive queries, show slower response
  LB automatically routes away from them

✓ Smarter than least connections
✗ Requires LB to track response times (more overhead)
```

**5. IP Hash (Sticky Sessions)**
```
hash(client_ip) % num_servers = target server

Client 73.42.15.99  → hash → Server B (always)
Client 98.12.34.56  → hash → Server A (always)

Example: Shopping cart in server memory (not recommended, but exists)
  User adds items → stored in Server B's memory
  User must hit Server B for checkout to see items

Example: Local cache warming
  User's data cached on Server B
  Hitting same server = cache hits, faster response

upstream api {
  ip_hash;
  server api1.example.com;
  server api2.example.com;
}

✓ Sticky without cookies (works for non-HTTP too)
✗ Problem: corporate NAT → thousands of users behind one IP → one server overloaded
✗ Problem: adding/removing server reshuffles ALL mappings
```

**6. Consistent Hashing**
```
Servers placed on a "hash ring":

                    hash(key)
                        ↓
        ┌───────────────●───────────────┐
        │                               │
     Server A                        Server D
        │                               │
        │         (hash ring)           │
        │                               │
     Server B                        Server C
        │                               │
        └───────────────────────────────┘

Key hashed → walk clockwise → first server found

Example: Distributed cache (Memcached, Redis Cluster)
  cache_key = "user:12345:profile"
  hash(cache_key) → Server C
  Always goes to Server C → cache locality

Why better than IP hash?
  - Add Server E: only keys between D and E move
  - Remove Server B: only B's keys move to C
  - ~1/N keys affected, not all keys reshuffled

Example: Amazon DynamoDB partition routing
  Partition key hashed → determines which node stores data

✓ Minimal disruption when scaling up/down
✓ Essential for caches and distributed databases
```

**Quick Comparison**
```
┌────────────────────┬─────────────────────────────────────────────────┐
│ Algorithm          │ Best For                                        │
├────────────────────┼─────────────────────────────────────────────────┤
│ Round Robin        │ Stateless APIs, identical servers               │
│ Weighted RR        │ Mixed hardware, canary deploys                  │
│ Least Connections  │ WebSockets, streaming, long requests            │
│ Least Response     │ Heterogeneous backends, latency-sensitive       │
│ IP Hash            │ Simple sticky sessions (non-HTTP)               │
│ Consistent Hash    │ Caches, databases, any stateful partitioning    │
└────────────────────┴─────────────────────────────────────────────────┘
```

### Health Checks
```
LB must know which servers are healthy. Two types:

┌─────────────────────────────────────────────────────────────────────────┐
│  PASSIVE (observe traffic)                                              │
│  ─────────────────────────                                              │
│  LB watches real requests for failures                                  │
│  - 5 consecutive 5xx → mark unhealthy                                   │
│  - Pro: no extra traffic                                                │
│  - Con: only detects failures after users hit them                      │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  ACTIVE (probe servers)                                                 │
│  ──────────────────────                                                 │
│  LB periodically hits health endpoint                                   │
│                                                                         │
│  L4 check: TCP connect to port (is it listening?)                       │
│  L7 check: GET /health → expect 200 (is app working?)                   │
│                                                                         │
│  ┌────────┐    GET /health     ┌──────────┐                             │
│  │   LB   │───────────────────►│  Server  │                             │
│  │        │◄───── 200 OK ──────│  /health │                             │
│  └────────┘    (every 5s)      └──────────┘                             │
│                                                                         │
│  Good /health endpoint checks:                                          │
│    ✓ Database connection                                                │
│    ✓ Redis connection                                                   │
│    ✓ Critical dependencies                                              │
│    ✗ Don't check everything (or health check becomes bottleneck)        │
└─────────────────────────────────────────────────────────────────────────┘
```

### Connection Draining (Graceful Shutdown)
```
Problem: Server needs to restart, but has active connections.

Without draining:                With draining:
  LB removes server instantly      LB stops NEW connections
  Active requests → ERROR          Waits for existing to finish
                                   Then removes server

┌────────┐                        ┌────────┐
│ Server │ ←─ signal: shutdown    │ Server │
└────────┘                        └───┬────┘
    │                                 │
    ✗ dropped connections             │ finish current requests (30s timeout)
                                      │
                                      ✓ clean shutdown
```

### Sticky Sessions (Session Affinity)
```
Problem: Stateful apps need user to hit same server.

┌────────────────────────────────────────────────────────────────────────┐
│  Methods:                                                              │
│                                                                        │
│  1. Cookie-based (L7)                                                  │
│     LB injects: Set-Cookie: SERVERID=srv2                              │
│     Future requests include cookie → routed to srv2                    │
│                                                                        │
│  2. IP Hash (L4/L7)                                                    │
│     hash(client_ip) % num_servers → same server                        │
│     Problem: clients behind NAT all go to same server                  │
│                                                                        │
│  3. Application-managed                                                │
│     App generates session ID → stored in Redis                         │
│     Any server can handle request (stateless backend)                  │
│     RECOMMENDED: makes scaling easier                                  │
└────────────────────────────────────────────────────────────────────────┘
```

### Real-World Load Balancers
```
┌─────────────┬─────────────────────────────────────────────────────────┐
│ Type        │ Examples                                                │
├─────────────┼─────────────────────────────────────────────────────────┤
│ Software    │ NGINX: HTTP-focused, great for L7, config-based         │
│             │ HAProxy: TCP/HTTP, very fast, production-grade          │
│             │ Envoy: Modern, sidecar proxy, used in service mesh      │
├─────────────┼─────────────────────────────────────────────────────────┤
│ Cloud       │ AWS ALB: L7, path/host routing, integrates with ECS/EKS │
│             │ AWS NLB: L4, millions of RPS, static IP support         │
│             │ GCP LB: Global, anycast IPs, auto-scaling               │
├─────────────┼─────────────────────────────────────────────────────────┤
│ Client-side │ gRPC: Built-in, round-robin by default                  │
│             │ Ribbon: Netflix OSS, with Eureka for discovery          │
│             │ Envoy sidecar: Each pod has its own proxy               │
└─────────────┴─────────────────────────────────────────────────────────┘
```

### Common Patterns
```
1. TWO-TIER LB
   ┌──────────────────────────────────────────────────────────┐
   │                     Global LB (L4)                       │
   │                  (AWS NLB / GCP GLB)                     │
   └───────────────────────┬──────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
     ┌─────────┐      ┌─────────┐      ┌─────────┐
     │  L7 LB  │      │  L7 LB  │      │  L7 LB  │
     │ (NGINX) │      │ (NGINX) │      │ (NGINX) │
     └────┬────┘      └────┬────┘      └────┬────┘
          │                │                │
          ▼                ▼                ▼
       Servers          Servers          Servers

   Why: L4 handles scale + SSL termination at L7

2. PATH-BASED ROUTING (L7)
   /api/*     → API servers
   /static/*  → CDN origin
   /admin/*   → Admin servers
   /*         → Web servers

3. BLUE-GREEN / CANARY
   LB shifts traffic gradually:
   - 100% to blue (current)
   - 10% to green (new) → monitor errors
   - 50/50 → monitor
   - 100% to green → done
```

### Key Points
- **L4 for speed**: Raw TCP, WebSockets, databases
- **L7 for smarts**: Path routing, header inspection, SSL termination
- **Health checks are critical**: Unhealthy server = user errors
- **Prefer stateless backends**: Avoid sticky sessions when possible
- **Connection draining**: Always enable for graceful deploys

### When to Apply
- **L7 (ALB, NGINX)**: Default for HTTP APIs, microservices
- **L4 (NLB, HAProxy TCP)**: WebSockets, databases, high throughput
- **Client-side**: gRPC services, service mesh (Envoy sidecar)
- **Two-tier**: Large scale systems needing both L4 scale and L7 routing

---

## 11. CDN (Content Delivery Network)

### Concept
Geographically distributed cache. Serve content from servers close to users.

### Problem It Solves
Physics: light takes 56ms minimum NYC↔London. CDN puts content closer, reducing latency.

### Diagram
```
Without CDN:                    With CDN:

User (Tokyo)                    User (Tokyo)
     │                               │
     │ 150ms RTT                     │ 5ms RTT
     ▼                               ▼
┌──────────┐                    ┌──────────┐   cache    ┌──────────┐
│  Origin  │                    │   Edge   │───miss────►│  Origin  │
│  (US)    │                    │ (Tokyo)  │◄───────────│  (US)    │
└──────────┘                    └──────────┘            └──────────┘
                                     │
                                cache hit = 5ms
                                cache miss = 5ms + 150ms (first request)
```

### How CDN Caching Works
```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CACHE HIT (fast path)                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  User ──► Edge ──► "I have this cached" ──► Return immediately          │
│                                                                         │
│  Latency: ~5-20ms (edge RTT only)                                       │
│  Origin: never contacted                                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         CACHE MISS (slow path)                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  User ──► Edge ──► "Don't have it" ──► Origin ──► Cache + Return        │
│                                                                         │
│  Latency: edge RTT + origin RTT (first request penalty)                 │
│  Next request: cache hit                                                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         STALE-WHILE-REVALIDATE                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  User ──► Edge ──► "Cache expired, but serve stale" ──► Return          │
│                          │                                              │
│                          └──► Background: fetch fresh from origin       │
│                                                                         │
│  User gets fast response (stale but acceptable)                         │
│  Next user gets fresh content                                           │
│                                                                         │
│  Header: Cache-Control: max-age=60, stale-while-revalidate=300          │
│          (fresh for 60s, serve stale up to 5min while refreshing)       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Cache Headers (How You Control CDN Behavior)
```
┌─────────────────────────┬───────────────────────────────────────────────┐
│ Header                  │ What It Does                                  │
├─────────────────────────┼───────────────────────────────────────────────┤
│ Cache-Control: max-age  │ Cache for N seconds                           │
│   max-age=3600          │ Cache for 1 hour                              │
│                         │                                               │
│ Cache-Control: no-cache │ Must revalidate with origin before serving    │
│                         │ (can still cache, but checks freshness)       │
│                         │                                               │
│ Cache-Control: no-store │ Never cache (sensitive data)                  │
│                         │                                               │
│ Cache-Control: private  │ Only browser can cache, not CDN               │
│                         │ (user-specific data like account page)        │
│                         │                                               │
│ Cache-Control: public   │ CDN can cache (even if authenticated)         │
│                         │                                               │
│ s-maxage=N              │ CDN-specific max-age (overrides max-age)      │
│                         │ Browser uses max-age, CDN uses s-maxage       │
│                         │                                               │
│ ETag / Last-Modified    │ Conditional requests (304 Not Modified)       │
│                         │ Edge asks origin: "changed since X?"          │
└─────────────────────────┴───────────────────────────────────────────────┘

Example: API response cached at edge, not in browser
  Cache-Control: public, s-maxage=300, max-age=0

Example: Static assets with versioned URLs (cache forever)
  /app.a1b2c3.js → Cache-Control: public, max-age=31536000, immutable
```

### Cache Invalidation Strategies
```
┌─────────────────────────────────────────────────────────────────────────┐
│  1. TTL-BASED (most common)                                             │
│  ───────────────────────────                                            │
│  Set expiration time, content auto-expires                              │
│                                                                         │
│  Example: Cache-Control: max-age=3600                                   │
│  Problem: Users see stale content until TTL expires                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  2. VERSIONED URLS (best for static assets)                             │
│  ──────────────────────────────────────────                             │
│  Change URL when content changes → cache forever                        │
│                                                                         │
│  /styles.css        → /styles.a1b2c3.css   (hash in filename)           │
│  /app.js?v=1.0      → /app.js?v=1.1        (query param)                │
│                                                                         │
│  Benefits:                                                              │
│    - Cache forever (immutable)                                          │
│    - Instant updates (new URL = new cache)                              │
│    - No purge needed                                                    │
│                                                                         │
│  How: Webpack, Vite, etc. auto-generate hashed filenames                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  3. PURGE / INVALIDATE (for emergencies)                                │
│  ───────────────────────────────────────                                │
│  Tell CDN to delete cached content immediately                          │
│                                                                         │
│  # Cloudflare API                                                       │
│  curl -X POST "https://api.cloudflare.com/purge_cache" \                │
│       -d '{"files":["https://example.com/page.html"]}'                  │
│                                                                         │
│  Problems:                                                              │
│    - Slow to propagate (seconds to minutes)                             │
│    - Rate limited (can't purge everything constantly)                   │
│    - Expensive at scale                                                 │
│                                                                         │
│  Use for: urgent fixes, legal takedowns                                 │
│  Don't use for: regular deploys (use versioned URLs instead)            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Push vs Pull CDN
```
┌─────────────────────────────────────────────────────────────────────────┐
│  PULL CDN (most common)                                                 │
│  ──────────────────────                                                 │
│  Edge fetches from origin on first request                              │
│                                                                         │
│  User → Edge (miss) → Origin → Edge caches → User                       │
│                                                                         │
│  ✓ Simple: just point CDN at origin                                     │
│  ✓ Automatic: only popular content gets cached                          │
│  ✗ First request is slow (cache miss)                                   │
│  ✗ Origin must stay online                                              │
│                                                                         │
│  Examples: Cloudflare, AWS CloudFront, Fastly                           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  PUSH CDN                                                               │
│  ────────                                                               │
│  You upload content to edge before users request                        │
│                                                                         │
│  Deploy → Push to all edges → Users always hit cache                    │
│                                                                         │
│  ✓ No cold start (always cached)                                        │
│  ✓ Origin can go offline after push                                     │
│  ✗ Manual: must push on every update                                    │
│  ✗ Storage costs (pay for edge storage)                                 │
│                                                                         │
│  Examples: AWS S3 + CloudFront (origin is S3, "push" on deploy)         │
│            Video platforms pre-push popular content                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Edge Compute (CDN + Code)
```
Run code at the edge, not just cache static content.

┌────────┐         ┌─────────────────────────────┐         ┌──────────┐
│  User  │────────►│      Edge (CDN + Code)      │────────►│  Origin  │
│        │         │                             │         │ (if needed)
│        │◄────────│  - Auth token validation    │         │          │
└────────┘         │  - A/B test routing         │         └──────────┘
                   │  - Request transformation   │
                   │  - Personalization          │
                   │  - Bot detection            │
                   └─────────────────────────────┘

Examples:
  Cloudflare Workers    → JavaScript at edge
  AWS Lambda@Edge       → Lambda at CloudFront edges
  Vercel Edge Functions → React/Next.js at edge
  Fastly Compute@Edge   → WebAssembly at edge

Use cases:
  - Validate JWT at edge (reject bad requests before origin)
  - Add geolocation headers (origin sees user's country)
  - Rewrite URLs for A/B testing
  - Serve different content per country (localization)
  - Block bad bots before they hit origin
```

### Real-World CDN Providers
```
┌─────────────────┬───────────────────────────────────────────────────────┐
│ Provider        │ Strengths                                             │
├─────────────────┼───────────────────────────────────────────────────────┤
│ Cloudflare      │ Huge network, DDoS protection, Workers (edge code)    │
│                 │ Free tier, easy setup, fast propagation               │
│                 │                                                       │
│ AWS CloudFront  │ Deep AWS integration, Lambda@Edge                     │
│                 │ S3 origin, signed URLs for private content            │
│                 │                                                       │
│ Fastly          │ Real-time purge (<150ms), VCL customization           │
│                 │ Used by: GitHub, Stripe, NYTimes                      │
│                 │                                                       │
│ Akamai          │ Oldest/largest, enterprise features                   │
│                 │ Used by: banks, media companies, gaming               │
│                 │                                                       │
│ Vercel/Netlify  │ Frontend-focused, auto-deploy from Git                │
│                 │ Edge functions, instant rollback                      │
└─────────────────┴───────────────────────────────────────────────────────┘
```

### Real-World Examples
```
Netflix:
  - 95% of traffic from edge (Open Connect appliances in ISPs)
  - Video segments pre-pushed to edges
  - Origin only for catalog/search API

Shopify:
  - Storefront HTML cached at edge with short TTL
  - Dynamic cart/checkout hits origin
  - Edge rewrites for A/B testing themes

GitHub:
  - Uses Fastly for instant cache purge
  - Push event → purge CDN → users see update immediately
```

### Key Points
- **Cache-Control headers**: You control CDN behavior via HTTP headers
- **Versioned URLs**: Best strategy for static assets (cache forever)
- **Stale-while-revalidate**: Fast response + background refresh
- **Purge is expensive**: Use versioned URLs instead when possible
- **Edge compute**: Run code at edge for auth, routing, personalization

### When to Apply
- **Always**: Static assets (JS, CSS, images, fonts)
- **Usually**: Public API responses, product pages, blog posts
- **Carefully**: Short TTL for frequently changing content
- **Avoid caching**: User-specific data, authenticated responses (use `private`)
- **Consider edge compute**: Auth validation, geo-routing, A/B tests

---

## 12. Regional Partitioning

### Concept
Partition data and services by geographic region. Each region is self-contained.

### Problem It Solves
Global users, but data often has natural locality. Keep data close to users who need it.

### Diagram
```
┌─────────────────────────────────────────────────┐
│                 Global Router                   │
└─────────┬─────────────────┬─────────────────┬───┘
          │                 │                 │
          ▼                 ▼                 ▼
    ┌───────────┐     ┌───────────┐     ┌───────────┐
    │  US-West  │     │  US-East  │     │    EU     │
    │  ───────  │     │  ───────  │     │  ───────  │
    │  Servers  │     │  Servers  │     │  Servers  │
    │  Database │     │  Database │     │  Database │
    └───────────┘     └───────────┘     └───────────┘
```

### Real-World Example
**Uber**: Miami rider never needs NYC driver. Each city = separate partition.
- Millions of users globally → thousands per city
- Local queries are fast; cross-region is rare
- Profile sync across regions happens async

### Big Picture
Regional partitioning reduces latency and scales naturally when data has locality.

### Key Points
- **Co-location**: Servers and databases in same region = sub-ms queries
- **Cross-region**: Only for global data (user profiles, analytics)
- **Data sovereignty**: GDPR requires EU data stays in EU

### When to Apply
- **Use**: Location-based services (Uber, DoorDash), regional products
- **Consider**: Compliance requirements (GDPR, data residency)
- **Avoid**: When data is truly global with no natural partitioning

---

## 13. Retry with Exponential Backoff

### Concept
On failure, wait before retrying. Double the wait time each attempt. Add randomness.

### Problem It Solves
Transient failures are common. Retry helps, but naive retry can overwhelm recovering systems.

### Diagram
```
Request 1: Fail
  └─► Wait 100ms + jitter (random 0-50ms)
Request 2: Fail
  └─► Wait 200ms + jitter
Request 3: Fail
  └─► Wait 400ms + jitter
Request 4: Success ✓

Without jitter:                  With jitter:
1000 clients retry               1000 clients retry
at exactly 100ms                 spread over 100-150ms
     │                                │││││││
     ▼                                ▼▼▼▼▼▼▼
  Thundering herd                 Distributed load
```

### Real-World Example
AWS SDKs implement exponential backoff by default for all API calls.

### Big Picture
Backoff gives the system time to recover. Jitter prevents synchronized retry storms.

### Key Points
- **Exponential**: 100ms → 200ms → 400ms → 800ms
- **Jitter**: Random component prevents thundering herd
- **Max retries**: Cap at some limit (e.g., 5 attempts)
- **Max backoff**: Cap wait time (e.g., 32 seconds)

### When to Apply
- **Always**: Any network call to external service
- **Magic phrase**: "Retry with exponential backoff and jitter"

---

## 14. Idempotency

### Concept
Operation can be applied multiple times with the same result. Safe to retry.

### Problem It Solves
Network fails mid-request. Did it succeed? Retry safely if operation is idempotent.

### Diagram
```
Idempotency Key Pattern:

Client                              Server
  │                                   │
  │─ POST /payment ─────────────────►│
  │   Idempotency-Key: order_456     │  (processes, stores result)
  │◄─ timeout ────────────────────────│
  │                                   │
  │─ POST /payment ─────────────────►│
  │   Idempotency-Key: order_456     │  (key exists, returns cached result)
  │◄─ 200 OK ─────────────────────────│  (no double charge!)
```

### Real-World Example
**Stripe**: Every POST requires `Idempotency-Key` header.
```
POST /v1/charges
Idempotency-Key: user_123_order_456_20260128

→ First request: Creates charge, stores result
→ Retry (same key): Returns stored result, no new charge
```

### Big Picture
```
Naturally idempotent:           Needs idempotency key:
────────────────────            ──────────────────────
GET   (read)                    POST  (create)
PUT   (full replace)            PATCH (sometimes)
DELETE (remove)
```

### Key Points
- **Key design**: Include enough context (user + order + date)
- **Store results**: Server must store and return cached response
- **TTL**: Idempotency keys typically expire (24-48 hours)

### When to Apply
- **Always**: Payment processing, order creation, any mutation with side effects
- **Key strategy**: Make key unique per logical operation

---

## 15. Circuit Breaker

### Concept
Stop calling a failing service. Fail fast instead of waiting for timeout.

### Problem It Solves
Service is down. Continuing to call it: (1) wastes time on timeouts, (2) prevents recovery (thundering herd).

### Diagram
```
        ┌───────────────────────────────────────────┐
        │                                           │
        ▼                                           │
┌──────────────┐  failures > threshold  ┌──────────────┐
│    CLOSED    │───────────────────────►│    OPEN      │
│   (normal)   │                        │ (fail fast)  │
└──────────────┘                        └──────┬───────┘
        ▲                                      │
        │                                 timeout
        │                                      │
        │   test succeeds               ┌──────▼───────┐
        └───────────────────────────────│  HALF-OPEN   │
                                        │  (testing)   │──┐
                                        └──────────────┘  │ test fails
                                               ▲──────────┘
```

### Real-World Example
**Database restart scenario**:
- Without circuit breaker: 10K clients retry → overwhelm DB → crash again
- With circuit breaker: Requests fail fast → DB recovers → gradual traffic increase

### Big Picture
Circuit breaker protects both the client (fast failure) and the service (time to recover).

### Key Points
- **CLOSED**: Normal operation, counting failures
- **OPEN**: All requests fail immediately (no actual call)
- **HALF-OPEN**: Test with one request, decide next state
- **Cascading failure**: One failure causes others; circuit breaker stops the cascade

### When to Apply
- **Use**: External APIs, database calls, any inter-service communication
- **Combine with**: Retry (retry → too many failures → circuit opens)

---

## Quick Reference

### Protocol Selection
| Scenario                     | Protocol   | Load Balancer |
|------------------------------|------------|---------------|
| Public API                   | REST/HTTP  | L7            |
| Internal microservices       | gRPC       | Client-side   |
| Server → Client push         | SSE        | L7            |
| Bidirectional real-time      | WebSocket  | L4            |
| Video/audio calling          | WebRTC     | N/A (P2P)     |

### Latency Numbers
| Route                        | Latency    |
|------------------------------|------------|
| Same datacenter              | <1ms       |
| Cross-AZ (same region)       | 1-2ms      |
| Cross-region (US coasts)     | 60-80ms    |
| NYC ↔ London                 | ~80ms      |
| NYC ↔ Tokyo                  | ~150ms     |

### Connection Overhead
| Operation                    | Cost       |
|------------------------------|------------|
| TCP handshake                | 1.5 RTT    |
| TLS 1.3 handshake            | 1 RTT      |
| TLS 1.3 resumption           | 0 RTT      |
| New HTTPS connection         | 2.5-3 RTT  |

### Decision Tree
```
Need real-time updates?
├─ No → REST
└─ Yes
   ├─ Server → Client only? → SSE
   └─ Bidirectional?
      ├─ Video/audio? → WebRTC
      └─ Other → WebSocket

Internal service communication?
├─ Performance critical? → gRPC
└─ Otherwise → REST (simpler)

Need reliability?
├─ Yes → TCP (default)
└─ Tolerable loss? → UDP (gaming, video)
```
