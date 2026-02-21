# System Design First Principles: The Physics of Software

> Most engineers pick components (Redis, Kafka, S3) like Lego blocks. Senior engineers
> reason from physical constraints — latency, I/O, bandwidth, queuing math, and cost —
> and the architecture draws itself.

---

## 1. The Latency Hierarchy: Data Has Distance

Your product page takes 3 seconds. Someone says "add Redis." But where is the actual
bottleneck — RAM, disk, or network? You can't answer without knowing the latency hierarchy.

Data lives somewhere physical. The time to reach it spans **seven orders of
magnitude**. Scale 1 nanosecond to 1 human second and you can feel it:

```
 Latency Hierarchy (1 ns ≈ 1 human second)

 Source            Actual       Scaled           Analogy
 ─────────────────────────────────────────────────────────────────
 CPU Register      <1 ns        <1 s            thought in your head
 L1 Cache           1 ns         1 s            notebook on your desk
 L2 Cache           4 ns         4 s            drawer under desk
 L3 Cache          10 ns        10 s            filing cabinet nearby
 RAM              100 ns       ~1.5 min         bookshelf across the room
                  ─ ─ ─ ─ ─ THE CLIFF ─ ─ ─ ─ ─
 SSD              150 μs       ~2 days          drive to a warehouse
 HDD                3 ms       ~1 month         ship via ocean cargo
 Network (region)  500 μs      ~6 days          next-day delivery
 Network (x-ocean) 150 ms      ~5 years         voyager probe & back
```

Everything above the cliff feels instant. Everything below is a road trip.
Local variable vs network call: **~1,000,000x** difference.

**First Principle — "Data has distance."** Your job is to minimize the distance
between compute and the data it needs.

### Why Caching Exists

The cliff between RAM (100 ns) and SSD (150 μs) is a **1,500x jump**. That single
fact is why caching is an entire discipline.

```
  Without cache                       With cache (Redis / Memcached)

  Client ──► App ──► DB (SSD)         Client ──► App ──┬── Cache (RAM)
                       │                                │     │
                   150 μs/read                     HIT: 1 μs  │ MISS
                                                       │      ▼
                                                       │     DB
                                                       │   (only on miss)
                                                       ▼
                                                 99% served 1,500x faster
```

> Before adding any component, ask: *"Where does the data physically live,
> and can I move it closer?"*

> **Staff-level mistake**: Saying "add a cache" in a design review without first
> identifying *which* hop is the bottleneck. Caching a 5 ms DB call when the real
> cost is a 200 ms cross-region network hop saves almost nothing.

Now that we know data has distance, the next question: *how* does hardware
actually move that data?

---

## 2. Mechanical Sympathy: Sequential vs. Random I/O

Your database choice (Postgres vs Cassandra) isn't about features — it's about
which I/O pattern matches your workload. Get this wrong and no amount of tuning helps.

Coined by racing driver Jackie Stewart — you don't have to build the engine, but you
must understand how it works. In software: know how bits move on storage hardware.

**Sequential I/O** (contiguous blocks) is dramatically faster than **random I/O**
(scattered locations) — on *every* storage medium:

- **HDD**: sequential ~200 MB/s, random ~1-2 MB/s (**100-200x gap**)
- **SSD (NVMe)**: sequential ~3,500 MB/s, random ~50-200 MB/s (**5-20x gap**)
  *(SATA SSDs cap at ~550 MB/s sequential)*

Why SSDs still favor sequential despite having no moving parts:
- **Prefetch** — controller loads next blocks before you ask (free for sequential)
- **Page waste** — SSD reads a full 4 KB page even for 100 bytes; random = 97% waste
- **FTL lookups** — each random address needs a separate translation

### How This Shapes Database Design: LSM-Trees vs B-Trees

This single hardware fact drives the biggest split in database engine design.

```
  LSM-Tree (Cassandra, RocksDB)           B-Tree (MySQL, PostgreSQL)
  ─────────────────────────────           ────────────────────────────

  "Write user_42"                         "Write user_42"
        │                                       │
        ▼                                       ▼
  Append to MemTable (RAM)              Find the exact page on disk
        │                               where user_42 belongs
        ▼  (when full)                          │
  Flush as sorted SSTable                       ▼
  SEQUENTIAL APPEND to disk             RANDOM SEEK + update in place
        │                                       │
        ▼                                       ▼
  ┌────────────────────────┐            ┌────────────────────────┐
  │ disk: ████████████████ │            │ disk:  █  █    █   █ █ │
  │      (contiguous)      │            │       (scattered)      │
  └────────────────────────┘            └────────────────────────┘
  Writes: FAST (sequential)             Writes: SLOW (random I/O)
  Reads: slower (merge SSTables)        Reads: FAST (sorted, one lookup)
```

> **LSM trade-off**: Compaction rewrites data 10-30x over its lifetime (write
> amplification). This eats SSD endurance and causes tail latency spikes during
> compaction storms — a common production surprise.

### Deep Dive: The B-Tree Write Path

B-Trees keep data sorted in fixed-size **pages** (4-16 KB) arranged as a balanced
tree. Every write must find the right page, then modify it *in place*.

```
  B-Tree Structure
  ════════════════
                      ┌─────────────────────┐
                      │     Root Page        │
                      │  [ 20 | 50 | 80 ]   │
                      └───┬──────┬──────┬───┘
              ┌───────────┘      │      └───────────┐
              ▼                  ▼                   ▼
       ┌────────────┐    ┌────────────┐      ┌────────────┐
       │ [5,10,15]  │    │ [25,31,42] │      │ [55,60,78] │  Internal
       └──┬──┬──┬───┘    └──┬──┬──┬───┘      └──┬──┬──┬───┘  Pages
          ▼  ▼  ▼           ▼  ▼  ▼              ▼  ▼  ▼
       ┌────────────────────────────────────────────────────┐
       │            Leaf Pages (actual row data)             │
       │  ~3-4 levels deep for millions of rows              │
       └────────────────────────────────────────────────────┘


  Write Path: INSERT user_42
  ══════════════════════════

  ① Find leaf    Root → Internal → Leaf Page 7   (tree walk, usually cached)
       │
       ▼
  ② WAL append   [...|page=7, key=42, val='Alice'|...]   (sequential — fast)
       │          This is the durability guarantee.
       ▼          If we crash, recovery replays the WAL.
  ③ Buffer pool   Page 7 marked DIRTY in RAM              (instant)
       │
       ▼
  ④ Flush         Page 7 written back to its               (random I/O — slow)
                  EXACT location on disk                    Deferred, but unavoidable.
       │
       ▼ (worst case: page is full)
  ⑤ Page split    Page splits into two halves              (2-3 more random writes)
                  + parent page updated                    Can cascade to root.
```

> Durability comes from the WAL (sequential — fast).
> Write cost comes from the flush (random — slow).
> WAL lets the engine say "done" quickly, then pay the random I/O lazily.

### Why B-Tree Reads Are Cheaper Than Writes

Both do an O(log n) tree walk. But the buffer pool makes reads nearly free:

- **Read key=42**: Root(RAM) → Internal(RAM) → Leaf(maybe disk) = 0-1 disk I/Os
- **Write key=42**: same tree walk + WAL append + flush dirty page + possible page split

Reads return what they found. Writes must *also* produce new I/O.
The buffer pool absorbs read cost — it can't absorb write-back cost.

### When to Pick Each

- **B-Tree** (MySQL, Postgres) — read-heavy OLTP, point lookups, range scans, strong transactions
- **LSM-Tree** (Cassandra, RocksDB) — write-heavy, high ingest, time-series, logs

> Pick LSM when writes dominate. Pick B-Tree when reads dominate and you
> need fast lookups with strong transactional guarantees.

Hardware decides I/O patterns. Next: what happens when data moves *between* machines?

---

## 3. Bandwidth, Throughput & Latency: The Pipe Problem

Your team says "the API is slow." Is it slow for *one* request (latency), or slow
for *all* requests together (throughput)? The fix is completely different.

Three terms that get confused constantly:

- **Latency** — time for one request (A → B). Governed by distance. You can't buy a shorter road.
- **Bandwidth** — max capacity of the channel. You *can* buy more lanes.
- **Throughput** — actual data delivered/sec. Always ≤ bandwidth (overhead eats the rest).

| Term       | What it measures             | Analogy              | Buyable?             |
| ---------- | ---------------------------- | -------------------- | -------------------- |
| Latency    | Time for first bit to arrive | Drive time (A → B)   | No — move closer     |
| Bandwidth  | Max capacity of the channel  | Number of lanes      | Yes — add lanes      |
| Throughput | Actual data delivered/sec    | Cars actually passing | Depends on bottleneck |

> **Boeing 747 thought experiment**: A 747 loaded with hard drives flying NYC → London
> carries ~300 PB in 8 hours ≈ 83 Tbps — that's 830x a 100 Gbps fiber link.
> *Terrible* latency (8 hrs for first byte), *massive* bandwidth.
> AWS Snowmobile is literally this — a 45-ft shipping container that trucks 100 PB to a data center.

### Diagnosing the Bottleneck

```
  LATENCY-BOUND                           BANDWIDTH-BOUND
  ─────────────                           ───────────────
  Single request slow even                Each request fast, but total
  under light load.                       throughput plateaus.

  Causes:                                 Causes:
  • Cross-region hops                     • Saturated NIC / disk
  • Deep microservice chains              • Serialization bottleneck
  • Lock contention                       • Single-threaded consumer

  Fixes:                                  Fixes:
  • CDN / caching / co-locate             • Scale out, add replicas
  • Fewer hops, async fan-out             • Compress, batch, parallelize
```

Real-world examples:
- **Latency**: Netflix Open Connect places video caches *inside* ISPs — 1 hop, not 20
- **Latency**: App + DB in same AZ cuts cross-AZ latency from ~1 ms to <0.1 ms
- **Bandwidth**: Kafka adds partitions so more consumers process in parallel
- **Bandwidth**: gRPC/protobuf sends 5-10x smaller payloads than JSON on the same pipe

> **Anti-pattern**: Throwing bandwidth at a latency problem.
> A 10x bigger pipe doesn't help when the pipe is 5,000 miles long.

> **Staff-level mistake**: Upgrading hardware before profiling. "Bigger DB" is
> a bandwidth fix. If the bottleneck is a cross-region hop, you've wasted money.

### Example: E-Commerce Product Page

```
  Situation: Product page takes 1.2s. Team wants to upgrade the DB.

  Browser ──► CDN ──► API Gateway ──► Product Service ──► DB (us-east-1)
                                            │
                                            ├──► Pricing Service (us-east-1)
                                            ├──► Inventory Service (eu-west-1) ◄── !
                                            └──► Review Service (us-east-1)

  Full breakdown of the 1.2s:
  ┌───────────────────────────────────────────────────────────────┐
  │ Browser/CDN: ~340ms │ Gateway: ~35ms │ Services: 265ms (seq) │
  │ Rendering/serialization overhead: ~560ms                      │
  └───────────────────────────────────────────────────────────────┘
  Service waterfall (sequential): DB 12ms + Pricing 15ms + Inventory 220ms + Review 18ms

  Bottleneck: Inventory at 220 ms — cross-region hop (us → eu).
  Bigger DB would save ~1 ms. Money wasted.

  Fix A: Co-locate Inventory to us-east-1 → 220→15 ms     = ~995 ms (17% faster)
  Fix B: Parallelize service calls → max(12,15,220,18)     = ~600 ms (50% faster)
  Fix C: Both → co-locate + parallelize                    = ~400 ms (67% faster)
```

> Before scaling, measure: is the system blocked on the *first byte* (latency)
> or *total bytes/sec* (bandwidth)? Solving the wrong one wastes money.

We've covered how data moves and how fast pipes carry it. But what happens when
requests start *queuing* for that pipe?

---

## 4. Little's Law: The Math of Queuing

Your on-call page fires at 3 AM — thread pool exhausted, but traffic hasn't changed.
What happened? Little's Law tells you exactly why.

A formula that governs **every system with a queue** — web servers, databases,
checkout lines:

```
  ╔═════════════════════════════════════════════════╗
  ║              L   =   λ   ×   W                  ║
  ║                                                 ║
  ║   L  = in-flight requests (concurrency)         ║
  ║   λ  = arrival rate  (requests / second)        ║
  ║   W  = processing time per request              ║
  ╚═════════════════════════════════════════════════╝
```

The server has a physical limit for L (thread pool, connection pool). If λ × W
exceeds that limit, a queue forms — and the death spiral begins.

### The Death Spiral

```
  HEALTHY: λ=100 req/s, W=50ms → L=5 (plenty of room)
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ► ► ► ► ►  ──►  [ ■ ■ ■ ■ ■ · · · · · · · · · · ]   server cap: 100
                    5 busy             95 idle


  WARNING: λ=100 req/s, W=500ms (DB slow!) → L=50
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ► ► ► ► ►  ──►  [ ■ ■ ■ ■ ■ ■ ■ ■ ■ ... ■ ■ · · ]   half full


  COLLAPSE: λ=100 req/s, W=2s (DB dying!) → L=200 > capacity!
  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ► ► overflow ──► QUEUE ──► W rises MORE ──► L grows ──► DEATH SPIRAL
  DB slow → L piles up → threads exhausted → upstream callers timeout
  → THEY queue too → TOTAL CASCADING COLLAPSE
```

### What You Can Do About It

| Lever              | Action                                   | Effect     |
| ------------------ | ---------------------------------------- | ---------- |
| Decrease W         | Optimize queries, add indexes, cache     | W ↓ → L ↓  |
| Increase L ceiling | More servers, bigger thread pools         | capacity ↑ |
| Decrease λ         | Rate limit, circuit break, shed load     | λ ↓ → L ↓  |

> If W is rising while λ stays constant, collapse is **mathematically inevitable**.
> You cannot escape Little's Law — only change its variables.

### Capacity Planning with Little's Law

Rearrange the formula for different questions:

- **"What response time can I expect?"** → `W = L / λ`
  Thread pool of 200, arrival rate 100 req/s → W = 200/100 = 2s max before queuing
- **"What throughput can I sustain?"** → `λ = L / W`
  Thread pool of 200, target W = 100ms → λ = 200/0.1 = 2,000 req/s max

> **Staff-level use**: Before any capacity review, compute `L_required = λ_peak × W_p99`.
> If L_required > your thread/connection pool, you *will* have an outage at peak.

Data has distance, hardware has I/O patterns, pipes have limits, queues have math.
The last constraint is the one most engineers forget: **cost**.

---

## 5. The Economics of Bits: Aligning Value with Cost

A new grad puts everything in Redis. A Staff engineer asks: "Is this data worth
$8/GB/month, or is $0.004/GB/month in Glacier good enough?"

In the cloud, economics IS physics. Every storage tier encodes a speed-vs-cost trade-off:

| Tier    | Cost/GB/mo | Access Pattern  | Use For                      |
| ------- | ---------- | --------------- | ---------------------------- |
| RAM     | $5-8       | >1000 reads/sec | sessions, auth tokens, cache |
| SSD     | $0.08      | 10-1000 reads/s | user profiles, OLTP          |
| S3      | $0.023     | <1 read/day     | old logs, receipts, backups  |
| Glacier | $0.004     | regulatory hold  | compliance archives          |

Key ratios: **RAM ~100x SSD, SSD ~3.5x S3, S3 ~6x Glacier**

> **The anti-pattern** — "Renting a penthouse to store cardboard boxes":
> 3-year-old receipt in Redis = $4,000/mo. Same data in Glacier = $2/mo.

> **Staff-level mistake**: Choosing storage tier based on current reads/sec without
> considering how access patterns change over time. Today's hot data is next year's
> cold data. Build lifecycle policies from day one.

---

## Cheat Sheet

```
  ┌──────────────────────────────────────────────────────────────────────┐
  │  FIRST PRINCIPLES — QUICK REFERENCE                                 │
  ├──────────────────────────────────────────────────────────────────────┤
  │                                                                      │
  │  1. LATENCY         Data has distance. Minimize it.                 │
  │                     L1=1ns  RAM=100ns  SSD=150μs  Net=0.5-150ms    │
  │                     Local var vs network call: ~1,000,000x          │
  │                                                                      │
  │  2. MECHANICAL      Sequential I/O >>> Random I/O.                  │
  │     SYMPATHY        HDD: 100-200x gap.  SSD: 5-20x gap.           │
  │                     LSM for writes (sequential). B-Tree for reads.  │
  │                                                                      │
  │  3. BANDWIDTH       Latency ≠ Bandwidth ≠ Throughput.               │
  │     & THROUGHPUT    Latency = can't buy.  Bandwidth = can buy.      │
  │                     Throughput = actual (always ≤ bandwidth).        │
  │                     Diagnose which one before scaling.              │
  │                                                                      │
  │  4. LITTLE'S LAW    L = λ × W                                       │
  │                     W rising + λ constant = collapse coming.        │
  │                     Fix W, raise L ceiling, or shed λ.              │
  │                                                                      │
  │  5. ECONOMICS       RAM ~100x SSD ~3.5x S3 ~6x Glacier.            │
  │                     Hot data in RAM, cold data in S3.               │
  │                     Misalignment = penthouse for cardboard boxes.   │
  │                                                                      │
  │  Design question for EVERY decision:                                │
  │    "What are the physics, and does the cost match the value?"       │
  │                                                                      │
  └──────────────────────────────────────────────────────────────────────┘
```

---

*Source: "System Design from First Principles" — FAANG Senior Engineer lecture series*
