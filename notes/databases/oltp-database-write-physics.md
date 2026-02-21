# OLTP Databases: A Database Is Just a Write Choice

> Your company runs five transactional databases. Different logos, different query
> languages, different pitch decks. But strip away the marketing — most of them
> rewrite data the exact same way. You're paying five times the operational cost
> for permutations of the same physics.

*Based on [Neeraj Koul's article](https://www.linkedin.com/pulse/database-just-write-choice-industry-has-addiction-neeraj-koul-g6vhc/)*

---

## 1. The First-Order Identity: How Bytes Hit Disk

Not SQL vs NoSQL. Not document vs relational. Not monolith vs distributed.
Those are second-order choices. The first-order identity of any OLTP database
is a single question:

> **When data changes, do you rewrite it in place, or append a new version
> and reconcile later?**

Every OLTP database in production today reduces to one of two storage philosophies:

```
  ┌─────────────────────────────────────────────────────────────────┐
  │                TWO WRITE PHILOSOPHIES                            │
  ├────────────────────────────┬────────────────────────────────────┤
  │  Model 1: MUTATE IN PLACE │  Model 2: APPEND AND RECONCILE    │
  │  (B-Tree world)            │  (LSM world)                      │
  │                            │                                    │
  │  PostgreSQL                │  RocksDB                           │
  │  MySQL                     │  Cassandra                         │
  │  Oracle                    │  TiDB (storage layer)              │
  │                            │                                    │
  │  Find the page → rewrite   │  Write to memory → flush sequentially│
  │  it in its exact location  │  → compact in background           │
  └────────────────────────────┴────────────────────────────────────┘
```

Everything else — query language, data model, scaling topology — is decoration
on top of this single axis.

---

## 2. Model 1: Mutate in Place (B-Tree)

Data lives in fixed-size pages. A B+Tree maps keys to pages. Updates find the
page and modify it *where it sits*.

**How it works physically:**
- B+Tree maps keys → fixed-size pages (4-16 KB)
- Updates modify existing pages in place
- WAL (write-ahead log) ensures durability before the page flush
- Checkpoints flush dirty pages from buffer pool to disk
- Vacuum / autovacuum reclaims dead row versions (MVCC overhead)

**The hidden cost on SSDs:**

Flash memory cannot overwrite — it must erase first, and erase happens at
*block* granularity (much larger than a page). A small logical update triggers
a much larger physical rewrite.

```
  Logical update:   change 100 bytes in a 4 KB page
      │
      ▼
  Database:         rewrites the full 4 KB page            (DB write amplification)
      │
      ▼
  SSD internally:   erases a 256 KB block, rewrites it     (SSD write amplification)
      │
      ▼
  Actual I/O:       DB amplification × SSD amplification   (the multiplication
                                                            most people ignore)
```

**Bottom line:** Reads are predictable and range-friendly. But every update
becomes a page rewrite, and the amplification compounds on SSDs.

---

## 3. Model 2: Append and Reconcile (LSM-Tree)

No in-place updates. Writes go to memory first, flush sequentially to immutable
files, and background compaction merges them.

**How it works physically:**
- Writes land in a MemTable (sorted, in-memory)
- When full, flushed as an immutable SSTable — **sequential write** (fast)
- SSTables organized in levels
- Background compaction merges levels, drops old versions, reclaims space

**The trade-off:**

Sequential writes align beautifully with SSD physics — no erase-before-write
penalty. But compaction rewrites data repeatedly across its lifetime.

> LSM doesn't eliminate write amplification — it *schedules* it.
> You trade random rewrite cost for structured, predictable rewrite cost.

**Read penalty:** A key might exist in multiple levels. Reads may check several
SSTables before finding the current version (bloom filters help, but don't eliminate this).

---

## 4. Everything Else Is a Second-Order Choice

After the write model, databases diverge through composable decisions. These
matter operationally, but they're *layered on top* of the write identity.

### Cache Ownership

| Strategy             | Who controls memory?      | Examples               |
| -------------------- | ------------------------- | ---------------------- |
| OS page cache        | Kernel manages caching    | PostgreSQL, Kafka      |
| Custom buffer pool   | Database manages eviction | MySQL (InnoDB), Oracle |

- OS cache: simpler, less code, but less deterministic control
- Custom pool: more predictable memory behavior, but you own the policy

### Consensus Layer (Distributed OLTP)

- **CockroachDB** — Raft
- **TiDB** — Raft
- **Google Spanner** — Paxos lineage

Consensus adds network round trips, replicated WAL, and tail latency amplification.
Storage stalls propagate cluster-wide.

> Consensus is layered *on top of* write physics. It does not replace it.
> Inside each replica, the same B-Tree or LSM decision holds.

### Scaling Model

| Topology                  | Examples        | Key trade-off                      |
| ------------------------- | --------------- | ---------------------------------- |
| Ring / consistent hashing | Cassandra       | Simple partitioning, weaker txns   |
| Sharded clusters          | MongoDB         | Flexible, cross-shard txns costly  |
| Globally coordinated SQL  | CockroachDB     | Strong consistency, higher latency |

Inside each shard or partition, the same write model applies.

### Concurrency Model

Thread-per-connection, worker pools, event loops, shard-per-core coroutines —
these affect CPU scheduling and context switching. They do **not** change how
data is rewritten on disk.

---

## 5. Cloud Cost: Where Write Physics Becomes Money

Cloud providers bill across dimensions: storage (GB-month), provisioned IOPS,
throughput, and replication bandwidth. Your write model determines which
dimension dominates your bill.

```
  B-Tree Cost Profile                     LSM Cost Profile
  ─────────────────                       ────────────────
  Dominant cost: IOPS                     Dominant cost: throughput + disk

  • Random page writes → high IOPS        • Sequential flushes → low IOPS
  • Checkpoint bursts create spikes        • Compaction consumes bandwidth
  • SSD internal GC adds hidden I/O        • Temporary disk overhead grows
  • Need headroom for flush storms         • Read amplification during merges

  Teams overprovision IOPS to              Cost shifts to sustained throughput
  stabilize P99. You're buying             and extra disk capacity.
  immunity against flush storms.
```

> This is not taste. It is billing math.
> If IOPS pricing hurts more → LSM aligns better.
> If storage cost and read latency dominate → B-Tree may be leaner.

---

## 6. The Lean OLTP Stack

Most OLTP systems don't need five transactional databases. A lean setup:

- **One B-Tree system** — read-heavy OLTP, strong transactions (Postgres, MySQL)
- **One LSM system** — sustained write-heavy paths (Cassandra, RocksDB)
- **One inverted index** — full-text search (Elasticsearch)
- **One cache** — hot path acceleration (Redis, Memcached)

Adding more systems with *identical write physics* multiplies operational surface
area, tuning dimensions, failure modes, and cognitive load — without increasing
fundamental capability.

> **Staff-level smell**: If you're running both Postgres *and* MySQL *and*
> CockroachDB, ask what you're gaining. All three are B-Tree engines.
> You've tripled your ops burden for the same write identity.

---

## 7. Decision Framework

Before adopting another database, ask:

| Question                                           | Points toward       |
| -------------------------------------------------- | ------------------- |
| Is the workload write-heavy or read-heavy?         | LSM vs B-Tree       |
| Are writes bursty or sustained?                    | B-Tree vs LSM       |
| Do we suffer more from IOPS limits or storage cost? | LSM vs B-Tree       |
| Are range scans dominant or key lookups?            | B-Tree vs LSM/Hash  |
| Do we want to manage vacuum cycles or compaction?   | B-Tree vs LSM       |

> If the answer maps to a write model you already run,
> you likely don't need another database.

---

## Other Indexes: Important but Not Foundational

OLTP systems use many index types beyond B-Tree/LSM:

- **Hash indexes** — O(1) equality lookups
- **Bitmap indexes** — low-cardinality filters
- **GIN / inverted indexes** — full-text search
- **GiST / R-Trees** — spatial queries
- **Adaptive radix trees** — in-memory acceleration

These optimize *access paths*. They sit on top of either page mutation or log
structuring. They do not redefine the primary write path.

---

## Cheat Sheet

```
  ┌──────────────────────────────────────────────────────────────────────┐
  │  OLTP DATABASE IDENTITY — QUICK REFERENCE                          │
  ├──────────────────────────────────────────────────────────────────────┤
  │                                                                      │
  │  FIRST-ORDER    How does the database rewrite data on disk?         │
  │  QUESTION       Mutate in place (B-Tree) or append (LSM)?          │
  │                                                                      │
  │  B-TREE         Predictable reads, range-friendly.                  │
  │  (Postgres,     Page rewrites × SSD amplification = hidden cost.   │
  │   MySQL)        Cloud cost dominated by IOPS.                      │
  │                                                                      │
  │  LSM            Fast sequential writes, high ingest.                │
  │  (Cassandra,    Compaction = scheduled amplification.              │
  │   RocksDB)      Cloud cost dominated by throughput + disk.         │
  │                                                                      │
  │  SECOND-ORDER   Cache ownership, consensus, scaling topology,      │
  │  CHOICES        concurrency model — layered on top, not foundational│
  │                                                                      │
  │  LEAN STACK     One B-Tree + one LSM + one search + one cache      │
  │                 covers most OLTP needs.                             │
  │                                                                      │
  │  KEY INSIGHT    SQL vs NoSQL is marketing. B-Tree vs LSM is physics.│
  │                                                                      │
  └──────────────────────────────────────────────────────────────────────┘
```

---

*Source: [Neeraj Koul — "Database Is Just A Write Choice"](https://www.linkedin.com/pulse/database-just-write-choice-industry-has-addiction-neeraj-koul-g6vhc/) (Feb 2026)*
