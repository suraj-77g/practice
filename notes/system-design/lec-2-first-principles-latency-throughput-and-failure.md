# System Design First Principles: Latency, Throughput & Failure at Scale

> Lecture 1 covered the physics of individual components — latency, I/O patterns,
> bandwidth, queuing, and cost. Now: what happens when you combine them at scale,
> and how do you measure what's actually happening?

---

## 1. The Myth of the Average

Your dashboard says "avg = 100 ms." Your VP says "looks great." Meanwhile, 100K users
per day are waiting 15 seconds. The mean hides your worst users behind a comfortable number.

```
  100 requests to your API:

   97 requests  ──►  80-120 ms   (happy)
    2 requests  ──►  500 ms      (annoyed)
    1 request   ──►  15,000 ms   (support ticket)

  Mean: (97×100 + 2×500 + 1×15000) / 100 = 257 ms

  Dashboard:  "257 ms — looks fine!"
  Reality:    1 in 100 users waits 15 seconds.
              At 10M req/day = 100,000 angry users.
```

Think of it like income: 99 people earn $50K, 1 earns $1B. Average = $10M — says
everyone's a multi-millionaire. Median = $50K — tells the real story. Same with latency.

> Never trust the mean. Always ask: *"What's happening to the slowest 1%?"*

---

## 2. P50 vs P99: Finding the Tail

Percentiles give you a distribution-aware view instead of a single misleading number.

```
  Sort all request latencies fastest → slowest:

  |█████████████████████████|████████████████████████|█████████|██|
  0%                       50%                      95%      99%  100%
                            ▲                        ▲        ▲
                           P50                      P95      P99
                        (typical)              (bad day)  (suffering)
```

- **P50** = the typical user. If this is bad, *everything* is broken.
- **P95** = the "bad day" user. Start worrying here.
- **P99** = the worst 1%. At scale, that 1% is millions of people.

> As seniority grows, focus shifts from P50 to P99. The median user is fine.
> The tail is where systems break, users churn, and SLAs fail.

> **Staff-level signal**: When P99 is 100x worse than P50, you have a bimodal
> distribution — two different code paths or data shapes, not just "slow requests."

Percentiles tell you *who* is suffering. Next: *how many* are suffering.

---

## 3. Scale: When Rare Events Become Constant

A 1% failure rate sounds negligible. But **probability × volume = certainty**.

| Requests/day  | P99 failures/day | Feels like                     |
| ------------- | ---------------- | ------------------------------ |
| 1,000         | 10               | barely noticeable              |
| 100,000       | 1,000            | support tickets                |
| 10,000,000    | 100,000          | constant complaints            |
| 1,000,000,000 | 10,000,000       | permanent outage for a country |

> At 1B req/day, P99 fires 10M times. It's not "rare" — it's a permanent feature.
> FAANG engineers treat P99 as a hard requirement, not a nice-to-have.

Individual services might look fine. But what happens when a single user request
touches *many* of them?

---

## 4. The Microservice Trap: Tail Amplification

Each microservice may be 99% fast on its own. But one user request that fans out
across many services **multiplies** the chance of hitting a tail.

```
  Homepage fans out to 100 microservices. Each is 99% fast.

  P(ALL 100 fast) = 0.99^100 = 36.6%

  Fan-out     P(all fast)    P(at least one slow)
  ────────────────────────────────────────────────
       1        99.0%              1.0%
      10        90.4%              9.6%
      50        60.5%             39.5%
     100        36.6%             63.4%   ◄── majority broken!
     500         0.7%             99.3%
```

At fan-out 100, **63.4% of users see a slow page** — even though every individual
service is "99% reliable."

> Individual service reliability means nothing in a fan-out architecture.
> The system's tail is the product of every service's tail.

> **Staff-level insight**: This is why Google and Amazon obsess over P99.9 per service,
> not P99. When you fan out to 100 services, you need each one to be 99.9% to keep
> the user-facing P99 at 90.5%.

So each request is a gamble across services. But there's another amplifier: the system
itself starts fighting back as load rises.

---

## 5. The Queuing Delay Explosion

Throughput and latency are independent — until the system nears capacity. Then
queuing kicks in and latency **explodes** non-linearly.

This is Little's Law (`L = λ × W` from Lecture 1) in action: as utilization rises,
W grows non-linearly, and L explodes past capacity.

### The Knee of the Curve

```
  Latency
  (P99)
    │
    │                                           ╱ ← 100%: DISASTER
    │                                        ╱
    │                                     ╱
    │                                  ╱ ← knee ~70-80%
    │                              .╱
    │                          .·
    │                     .·
    │              . . ·          ← flat zone: throughput rises,
    │  . .  . . .                   latency stable
    └────────────────────────────────────────────── Utilization %
    0%    20%    40%    60%    80%   100%
                               ▲
                     FAANG auto-scales here (60-70%)
                     30% headroom = insurance against spikes
```

Below ~70%, adding load increases throughput with stable latency. Past the knee,
one hiccup triggers a shockwave — P99 goes from seconds to minutes.

> Running at 100% isn't "efficient" — it's a ticking bomb.
> 30% idle is the cheapest insurance you'll buy.

> **Staff-level rule**: Set auto-scaling triggers at 60-70% saturation,
> not 80-90%. By 80%, you're already past the knee and one spike away from an outage.

Queuing tells us *when* systems break. But there's a hard ceiling even parallelism
can't break.

---

## 6. Amdahl's Law: The Serial Bottleneck Ceiling

You tripled your fleet, but throughput only went up 40%. Your VP asks why.
The answer: Amdahl's Law.

No matter how many machines you add, the **serial fraction** (non-parallelizable
work) sets an absolute ceiling on speedup.

```
  Max Speedup = 1 / S    (S = serial fraction, as N → ∞)

  Serial %     Max Speedup       Example
  ───────────────────────────────────────────────
    1%            100x            parallel batch job
    5%             20x            single DB lock on writes
   10%             10x            coordinator / sequencer
   25%              4x            single-threaded leader
   50%              2x            mostly serial pipeline
```

### The House-Building Analogy

```
  Serial (must happen in order — no amount of workers helps):

  ┌──────────┐   ┌──────────┐   ┌──────────┐
  │Foundation│──►│  Framing  │──►│   Roof   │
  └──────────┘   └──────────┘   └──────────┘

  Parallel (can happen simultaneously):

  ┌──────────┐
  │Electrical│──►─╮
  ├──────────┤    │
  │ Plumbing │──►─┤── all at once
  ├──────────┤    │
  │ Painting │──►─╯
  └──────────┘

  Foundation→Framing→Roof is the serial fraction.
  It determines MINIMUM build time regardless of team size.
```

### The Whack-a-Mole Reality

Performance tuning is a chain: fixing one bottleneck reveals the next.

```
  Fix slow algorithm ──► now blocked by disk I/O
  Fix disk I/O       ──► now blocked by network
  Fix network        ──► now blocked by lock contention
  Fix lock           ──► now blocked by serialization
```

> Senior engineers don't just add parallelism. They hunt the serial fraction —
> the single lock, the one coordinator — because *that* is the system's ceiling.

> **Gustafson's counterpoint**: Amdahl assumes fixed problem size. In practice,
> as you get more machines you solve *bigger* problems — the parallel portion grows,
> and the serial fraction shrinks as a percentage. `Scaled Speedup = N - S × (N - 1)`.
> This is why "just shard it" often works better than the math suggests.

> **Staff-level mistake**: "Just add more servers" without identifying the serial
> bottleneck. If your write path goes through a single-leader DB, 50 more read
> replicas won't help writes at all.

Now we can detect problems (percentiles), understand amplification (fan-out),
predict collapse (queuing), and find ceilings (Amdahl). How do we tie it all
together for monitoring?

---

## 7. The Four Golden Signals of SRE

Google's SRE handbook distills monitoring into four signals. Each catches problems
the others miss. You need all four.

- **Latency** — how long requests take (P50, P95, P99). P99 spike + flat P50 = tail problem, not global.
- **Traffic** — requests/sec hitting the system (λ). A *drop* can be worse than a spike — users giving up.
- **Errors** — failure rate relative to traffic. Watch the *rate*, not count:
  100 errors at 10K traffic = 1% (normal). 100 errors at 2K traffic = 5% (crisis — count is flat!).
- **Saturation** — how full the most constrained resource is. The **leading indicator** —
  by the time latency spikes, saturation already crossed the knee. >70-80% = explosion imminent.

### Diagnosis Flow

```
  Latency P99 spiking?
      │
      ├── Traffic stable?
      │       YES ──► Check SATURATION
      │                   ├── near 80%?  YES → at the knee, scale out
      │                   └── low?       → downstream dependency slow
      │       NO (traffic surging) ──► λ spike, scale out or shed load
      │
      ├── Error rate climbing? ──► failing component, not just slow
      │
      └── Traffic dropping + errors stable? ──► users giving up
```

### Real Incident Timeline

```
  11:02 AM  Saturation on DB crosses 80%                (LEADING indicator)
  11:04 AM  Latency P99 spikes 200ms → 5s               (queuing starts)
  11:05 AM  Error rate jumps 0.1% → 8%                   (timeouts cascade)
  11:06 AM  Traffic drops 40%                             (users giving up)

  Resolution: Scale DB read replicas.
  Saturation warned 4 minutes before user impact.
```

> Latency without traffic is meaningless. Errors without saturation miss the
> "why." Saturation is the only signal that warns you *before* things break.

> **Staff-level mistake**: Monitoring only error *count* instead of error *rate*.
> Count stays flat during a traffic drop — making a 5x worse situation look "stable."

All four signals tell you what's happening. But can you *fix* tail latency
without making servers faster?

---

## 8. Hedged Requests: Beating Tail Latency with Statistics

A Staff-level technique from Google's "The Tail at Scale" paper. Instead of hoping
every server is fast, **race two replicas** and take whichever responds first.

```
  Without hedging                     With hedging
  ════════════════                    ═══════════════

  Client ──► Shard A                  Client ──► Shard A (primary)
                │                                  │
             waiting...                     wait until P95 threshold
                │                                  │ no reply?
                │                                  ├──► Shard B (hedge)
             responds at                           │       │
             2,000 ms (P99)                        │    responds 50 ms
                │                                  ▼
                ▼                            take FIRST: 50 ms
          Response: 2,000 ms                 cancel the other
```

**The math**: P(both replicas slow) = 0.01 × 0.01 = 0.0001. That's a **100x
improvement** in tail probability for ~5% extra load (hedge only fires when
primary misses P95).

### When NOT to Hedge

- **Non-idempotent operations** — can't safely send a payment or write to two replicas
- **High base load** — if already at 90% utilization, the extra 5% pushes past the knee
- **Correlated slowness** — if both replicas share a dependency (same DB, GC storm),
  hedging assumes independence that doesn't exist

> Hedged requests don't make servers faster. They make slowness **irrelevant**
> by exploiting the fact that two machines are almost never slow at the same time.

---

## The Big Picture

Every concept in these two lectures connects into a single flow:

```
  Request arrives
      │
      ▼
  Queuing ──────────── Little's Law: L = λ × W
      │                 Saturation → knee of the curve
      ▼
  Processing ────────── Latency hierarchy (data has distance)
      │                 Mechanical sympathy (sequential vs random I/O)
      │                 Amdahl's Law (serial fraction = ceiling)
      ▼
  Response ──────────── Measure with percentiles (P50/P99, not mean)
      │                 Four Golden Signals (latency, traffic, errors, saturation)
      ▼
  At Scale ──────────── Tail amplification (fan-out × P99 = system P99)
      │                 Hedged requests (race replicas to beat the tail)
      ▼
  Economics ─────────── Match storage tier to access pattern
                        Hot → RAM, Warm → SSD, Cold → S3, Frozen → Glacier
```

> For every design decision, ask two questions:
> *"What are the physics?"* and *"Does the cost match the value?"*

---

## Cheat Sheet

```
  ┌──────────────────────────────────────────────────────────────────────┐
  │  LATENCY, THROUGHPUT & FAILURE AT SCALE — QUICK REFERENCE           │
  ├──────────────────────────────────────────────────────────────────────┤
  │                                                                      │
  │  1. AVERAGES LIE    Always use percentiles. P99 > P50 for SRE.     │
  │                     At 1B req/day, P99 fires 10M times daily.       │
  │                                                                      │
  │  2. TAIL            Fan-out amplifies tails.                         │
  │     AMPLIFICATION   100 services × 99% = 36.6% all-fast chance.    │
  │                     Individual reliability ≠ system reliability.     │
  │                                                                      │
  │  3. QUEUING         Latency flat until ~70% utilization.             │
  │     EXPLOSION       Past the knee → seconds become minutes.         │
  │                     60-70% ceiling. 30% idle = insurance.           │
  │                                                                      │
  │  4. AMDAHL'S LAW    Max speedup = 1/S  (S = serial fraction).      │
  │                     5% serial = 20x ceiling, infinite cores.        │
  │                     Hunt the serial bottleneck, not more parallelism.│
  │                                                                      │
  │  5. FOUR GOLDEN     Latency (percentiles), Traffic (λ),             │
  │     SIGNALS         Errors (rate not count), Saturation (leading).  │
  │                     Saturation >80% = explosion imminent.           │
  │                                                                      │
  │  6. HEDGED          Race two replicas, take first response.          │
  │     REQUESTS        P(both slow) = 0.01² = 0.0001.                 │
  │                     100x tail improvement for ~5% extra load.       │
  │                                                                      │
  └──────────────────────────────────────────────────────────────────────┘
```

---

*Source: "System Design from First Principles — Part 2" — FAANG Senior Engineer lecture series*
