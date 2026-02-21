# Apache Cassandra Architecture

> Originally developed at Facebook for inbox search and open-sourced in 2008. Combines
> Amazon Dynamo's distributed design with Google Bigtable's data model.

---

## 1. Design Philosophy

| Principle                  | Description                                                            |
| -------------------------- | ---------------------------------------------------------------------- |
| **Decentralization**       | Every node is equal — no master, no leader election, no SPOF           |
| **AP over C (CAP)**        | Favors availability + partition tolerance; consistency is tunable       |
| **Linear Scalability**     | Add nodes = linear increase in throughput/storage, zero downtime       |
| **Write Optimization**     | Append-only commit log + in-memory memtable — avoids random I/O       |

```
  CAP Theorem — Cassandra's Position

         Consistency (C)
              /\
             /  \
            /    \
           / CP   \
          /  zone  \
         /----------\
        /            \
       /   Cassandra  \
      /    defaults    \
     /     here (AP)    \
    /____________________\
  Availability (A) --- Partition Tolerance (P)

  * Cassandra lives on the AP edge by default
  * Tunable consistency lets you slide toward CP per query
```

---

## 2. Cluster Topology and the Ring

Every node owns a position on a logical **token ring**. The full token range
(`-2^63` to `2^63 - 1` with Murmur3Partitioner) is divided among nodes.

```
                     Token Ring (RF = 3)

                        Node A
                      (0 - 255)
                     .---------. 
                   /             \
                 /                 \
         Node F  |                 |  Node B
      (1281-1535)|                 |(256 - 511)
                 |     Cluster     |
                 |                 |
         Node E  |                 |  Node C
      (1024-1280)|                 |(512 - 767)
                  \               /
                    \           /
                      '-------'
                       Node D
                     (768-1023)

  Data with token = 300  -->  Primary: Node B
                              Replica: Node C, Node D  (next 2 on ring)
```

### Virtual Nodes (Vnodes)

Instead of one big range per node, each node owns **many small ranges** (default 256).

```
  Without Vnodes                    With Vnodes (num_tokens = 4 shown)
  +-----------+                     +-----------+
  |  Node A   |  owns [0, 333]     |  Node A   |  owns [0,50] [200,250]
  |  Node B   |  owns [334, 666]   |           |        [400,450] [700,750]
  |  Node C   |  owns [667, 999]   |  Node B   |  owns [51,100] [251,300]
  +-----------+                     |           |        [451,500] [751,800]
                                    |  Node C   |  owns [101,199] [301,399]
  Adding Node D requires            |           |        [501,699] [801,999]
  streaming ~33% of data            +-----------+
  from ONE node
                                    Adding Node D streams small slices
                                    from ALL nodes — faster, balanced
```

---

## 3. Data Partitioning

```
                       Partitioning Flow

  +------------------+      Murmur3       +---------------+
  |  Partition Key   | ----- Hash ------> |  Token Value  |
  |  (e.g. user_id)  |     Function       |  (e.g. 4729) |
  +------------------+                    +-------+-------+
                                                  |
                                                  v
                                    +----------------------------+
                                    |  Token Ring lookup:        |
                                    |  Token 4729 falls in       |
                                    |  Node C's range            |
                                    |  --> Primary replica = C   |
                                    |  --> Next replicas = D, E  |
                                    +----------------------------+
```

### Primary Key Structure

```
  PRIMARY KEY ( (partition_key), clustering_col1, clustering_col2 )
               |_______________|  |________________________________|
                     |                          |
              Determines WHICH            Determines HOW data
              node stores data            is sorted WITHIN
              (hashed to token)           the partition on disk

  Example:
  PRIMARY KEY ( (sensor_id), reading_time )

  All readings for sensor_id = "temp-42" live on the SAME node,
  sorted by reading_time  -->  efficient range scans
```

---

## 4. Replication

```
  Replication Factor = 3

  Write to partition key "user:alice"  (token = 400)

          Node A          Node B          Node C          Node D
         [0-255]        [256-511]       [512-767]       [768-1023]
            |               |               |               |
            |          +----+----+          |               |
            |          | PRIMARY |          |               |
            |          | REPLICA |          |               |
            |          +---------+          |               |
            |               |               |               |
            |               +---> Replica 2 +               |
            |               |               |               |
            |               +---> --------- +-> Replica 3   |
            |               |               |               |

  Token 400 falls in Node B's range.
  Replicas go to next nodes on ring: C and D.
```

### Replication Strategies

```
  SimpleStrategy (single DC)          NetworkTopologyStrategy (multi-DC)
  +-------------------------+         +----------------------------------+
  | Place replicas on next  |         | Per-DC replication factor:       |
  | N-1 nodes on the ring   |         |   dc1: 3,  dc2: 2               |
  +-------------------------+         |                                  |
                                      | Within each DC, place replicas  |
  Fine for dev/test.                  | in DIFFERENT racks for fault     |
  Not for production.                 | tolerance.                       |
                                      +----------------------------------+

  NetworkTopologyStrategy with Rack Awareness:

       DC1 (RF=3)                          DC2 (RF=2)
  +------------------+               +------------------+
  | Rack A  | Rack B |               | Rack A  | Rack B |
  |---------|--------|               |---------|--------|
  | Node 1  | Node 3 |               | Node 5  | Node 6 |
  | Node 2  | Node 4 |               |         |        |
  +------------------+               +------------------+
   Replica    Replica                  Replica    Replica
   on Rack A  on Rack B               on Rack A  on Rack B
   + 1 more
```

---

## 5. Gossip Protocol

Every second, each node picks 1-3 random peers and exchanges state.

```
  Gossip Exchange (Epidemic Protocol)

  Time T=0:  Node A learns "Node E is down"

  T=1:   A --gossip--> B    "Hey, E is down (version 7)"
         B: "I have E version 5, yours is newer. Updated!"
         B --reply-->  A    "BTW, D has new tokens (version 12)"
         A: "I have D version 10, updating!"

  T=2:   A --gossip--> F
         B --gossip--> C    Information spreads exponentially

  T=3:   C --gossip--> D
         F --gossip--> E    Entire cluster converges in seconds

  +------------------------------------------------------+
  | Gossip State per Node:                               |
  |   - Status (ALIVE / BOOTSTRAP / LEAVING / DOWN)      |
  |   - Token ownership                                  |
  |   - Schema version                                   |
  |   - Datacenter & rack                                |
  |   - Load statistics                                  |
  +------------------------------------------------------+
```

### Phi Accrual Failure Detector

```
  Fixed Timeout (traditional)         Phi Accrual (Cassandra)
  +---------------------------+       +----------------------------------+
  | "No heartbeat in 5s?      |       | Maintain sliding window of       |
  |  Node is DOWN."           |       | heartbeat inter-arrival times.   |
  |                           |       |                                  |
  | Problem: High-latency     |       | Compute phi (statistical         |
  | networks cause false      |       | likelihood of failure).          |
  | positives.                |       |                                  |
  +---------------------------+       | phi > 8 (default) --> mark DOWN  |
                                      |                                  |
                                      | Adapts to network conditions.    |
                                      | Fewer false positives.           |
                                      +----------------------------------+

  phi value:    1     2     4      8       12
                |     |     |      |        |
  Confidence:  low   ...   med   default   very high
  that node         threshold -->  |
  has failed                    (configurable)
```

---

## 6. The Write Path

```
  Client Write Request
        |
        v
  +============================================+
  | Coordinator Node                           |
  | (any node the client connects to)          |
  +============================================+
        |
        |  Forward to replica nodes
        v
  +---------------------------------------------+
  |              REPLICA NODE                    |
  |                                              |
  |  Step 1: COMMIT LOG (append-only on disk)    |
  |  +----------------------------------------+ |
  |  | ... | write N-1 | >>> write N <<<       | |
  |  +----------------------------------------+ |
  |  Sequential I/O --> fast, durable            |
  |                                              |
  |  Step 2: MEMTABLE (in-memory, sorted)        |
  |  +----------------------------------------+ |
  |  |  key1 -> val  |  key2 -> val  | ...     | |
  |  |  (ConcurrentSkipListMap)                | |
  |  +----------------------------------------+ |
  |  Insert into sorted structure --> fast       |
  |                                              |
  |  --> ACK sent to coordinator                 |
  |      (write complete!)                       |
  |                                              |
  |  Step 3: FLUSH (async, when threshold hit)   |
  |  Memtable ---------> SSTable on disk         |
  |  (mutable, memory)   (immutable, disk)       |
  +---------------------------------------------+

  Write = Commit Log + Memtable = SUCCESS
  No need to wait for SSTable flush!
```

### SSTable Components

```
  SSTable on Disk (immutable once written)
  +----------------------------------------------------+
  |                                                    |
  |  +----------------+   Data file: actual rows       |
  |  |   Data File    |   sorted by partition key      |
  |  +----------------+   then clustering columns      |
  |                                                    |
  |  +----------------+   Maps partition keys to       |
  |  | Partition Index |   offsets in data file         |
  |  +----------------+                                |
  |                                                    |
  |  +----------------+   Sampled subset of partition   |
  |  | Summary Index  |   index for faster lookups     |
  |  +----------------+                                |
  |                                                    |
  |  +----------------+   Probabilistic: "is this      |
  |  | Bloom Filter   |   partition in this SSTable?"   |
  |  +----------------+   False positives OK, no        |
  |                        false negatives              |
  |  +----------------+                                |
  |  | Compression    |   Locate compressed blocks     |
  |  | Offsets        |   for random access             |
  |  +----------------+                                |
  |                                                    |
  |  +----------------+                                |
  |  | Statistics     |   Min/max tokens, row counts,  |
  |  | Metadata       |   timestamps, etc.              |
  |  +----------------+                                |
  +----------------------------------------------------+
```

---

## 7. The Read Path

```
  Client Read Request (partition key = "user:bob")
        |
        v
  +============================================+
  | Coordinator Node                           |
  +============================================+
        |
        v
  +---------------------------------------------+
  |              REPLICA NODE                    |
  |                                              |
  |  +-- Step 1: Check MEMTABLE --------+       |
  |  |   Data in memory? Include it.    |       |
  |  +----------------------------------+       |
  |         |                                    |
  |         v                                    |
  |  +-- Step 2: BLOOM FILTER per SSTable --+   |
  |  |                                      |   |
  |  |  SSTable-1 Bloom: "user:bob" ? NO ---+--> SKIP (no disk read)
  |  |  SSTable-2 Bloom: "user:bob" ? YES --+--> continue
  |  |  SSTable-3 Bloom: "user:bob" ? YES --+--> continue
  |  +--------------------------------------+   |
  |         |                                    |
  |         v                                    |
  |  +-- Step 3: SUMMARY + INDEX LOOKUP ----+   |
  |  |  Summary narrows range in index.     |   |
  |  |  Index gives exact offset in data    |   |
  |  |  file.                               |   |
  |  +--------------------------------------+   |
  |         |                                    |
  |         v                                    |
  |  +-- Step 4: READ DATA + MERGE --------+   |
  |  |  Read from SSTable-2 data file       |   |
  |  |  Read from SSTable-3 data file       |   |
  |  |  Merge with memtable data            |   |
  |  |  Resolve conflicts by TIMESTAMP      |   |
  |  |  (latest write wins)                 |   |
  |  +--------------------------------------+   |
  |         |                                    |
  |         v                                    |
  |      Result                                  |
  +---------------------------------------------+
```

### Caching Layers

```
  Read Request
       |
       v
  +-----------+     hit
  | Row Cache | ---------> Return (entire partition cached)
  +-----------+
       | miss
       v
  +-----------+     hit
  | Key Cache | ---------> Jump directly to SSTable data offset
  +-----------+             (skip summary + index lookup)
       | miss
       v
  Full lookup:
  Summary -> Index -> Data File
```

---

## 8. Compaction

SSTables are immutable. Updates/deletes create new SSTables and tombstones.
Compaction merges them, discards obsolete data, and reclaims space.

```
  Why Compaction Is Needed:

  Time -->
  Flush 1:  SSTable-1  [ user:A v1, user:C v1 ]
  Flush 2:  SSTable-2  [ user:A v2, user:D v1 ]    <-- user:A updated
  Flush 3:  SSTable-3  [ user:C DEL, user:E v1 ]   <-- user:C deleted (tombstone)

  Read for user:A must check ALL 3 SSTables!

  After Compaction:
  SSTable-4  [ user:A v2, user:D v1, user:E v1 ]
  (user:A v1 discarded, user:C tombstone garbage-collected)
```

### Compaction Strategies

```
  STCS (Size-Tiered)                  LCS (Leveled)
  +--------------------------+        +-----------------------------+
  | Group SSTables by size.  |        | Organize into levels.      |
  | Merge when enough        |        | L0: fresh from memtable    |
  | similar-sized accumulate.|        | L1: 10x size of L0         |
  |                          |        | L2: 10x size of L1 ...     |
  | + Write-optimized        |        |                             |
  | - 2x disk space needed   |        | Non-overlapping ranges     |
  | - Variable read latency  |        | within each level.         |
  +--------------------------+        |                             |
                                      | + Consistent read latency  |
                                      | + Bounded space overhead   |
                                      | - Higher write amplification|
                                      +-----------------------------+

  STCS Visualization:            LCS Visualization:

  [4MB][4MB][4MB][4MB]           L0: [s][s][s][s]  (small, overlapping)
         |                            \  merge  /
         v                       L1: [----][----][----]  (non-overlapping)
  [------16MB------]                       |  merge
         +                       L2: [----------][----------][--------]
  [16MB][16MB][16MB][16MB]
         |
         v
  [--------64MB--------]


  TWCS (Time-Window)                  UCS (Unified, Cassandra 5.0)
  +--------------------------+        +-----------------------------+
  | Group SSTables by time   |        | Single adaptive strategy.  |
  | window.                  |        | Auto-tunes based on         |
  | Within each window: STCS.|        | workload patterns.          |
  | Closed windows are NEVER |        | Replaces need to choose     |
  | compacted again.         |        | between STCS/LCS/TWCS.      |
  |                          |        +-----------------------------+
  | + Ideal for time-series  |
  | + No rewriting TTL data  |
  +--------------------------+

  TWCS Visualization:

  Window 1 (00:00-01:00):  [ss][ss] -> [merged] --> DONE (never touched again)
  Window 2 (01:00-02:00):  [ss][ss] -> [merged] --> DONE
  Window 3 (02:00-now):    [s][s][s]  (active, still compacting via STCS)
```

---

## 9. Consistency Model

Cassandra provides **tunable consistency** per query.

```
  Consistency Spectrum (RF = 3)

  Weak                                                         Strong
  <---------------------------------------------------------------->
  ANY    ONE    TWO   THREE   QUORUM   LOCAL_QUORUM   ALL

  ANY:     At least 1 node (even hinted handoff). Weakest.
  ONE:     1 replica acknowledges.
  QUORUM:  floor(RF/2) + 1 = 2 replicas (for RF=3).
  ALL:     All 3 replicas must acknowledge. Strongest but least available.
```

### Achieving Strong Consistency

```
  Formula:   W + R > RF   -->  Strong (linearizable) consistency

  Example with RF = 3:

  +-------------------+-----+-----+-------+------------------+
  | Config            |  W  |  R  | W+R   | Strong?          |
  +-------------------+-----+-----+-------+------------------+
  | ONE / ONE         |  1  |  1  |   2   | NO  (2 <= 3)     |
  | QUORUM / ONE      |  2  |  1  |   3   | NO  (3 <= 3)     |
  | QUORUM / QUORUM   |  2  |  2  |   4   | YES (4 > 3)  <-- |
  | ALL / ONE          |  3  |  1  |   4   | YES (4 > 3)      |
  +-------------------+-----+-----+-------+------------------+

  Most common production config: QUORUM reads + QUORUM writes with RF=3
```

### Consistency Levels Visualized

```
  Write with QUORUM (RF=3, need 2 ACKs)

  Client --> Coordinator
                |
                +--> Node A (replica 1)  --> ACK  \
                |                                   }--> 2 ACKs = SUCCESS
                +--> Node B (replica 2)  --> ACK  /
                |
                +--> Node C (replica 3)  --> (slow, but write still succeeds)

  Read with QUORUM (need 2 responses)

  Client --> Coordinator
                |
                +--> Node A  --> data (ts=100)  \
                |                                 }--> pick latest ts --> return
                +--> Node B  --> data (ts=105)  /
                |
                +--> Node C  --> (not contacted or too slow)
```

---

## 10. Coordination and Request Routing

```
  Token-Aware Routing (Modern Drivers)

  Without token-aware:                 With token-aware:
  +--------+                           +--------+
  | Client |                           | Client |
  +---+----+                           +---+----+
      |                                    |
      v                                    |  Driver knows token map:
  +--------+    forward    +--------+      |  "token 400 => Node B"
  | Node A | -----------> | Node B |      |
  | (coord)|              | (owner)|      +--------->  +--------+
  +--------+              +--------+                   | Node B |
                                                       | (owner)|
  Extra hop!                                           +--------+
                                           Direct! No extra hop.
```

### Speculative Retry

```
  Normal:                              With Speculative Retry:

  Coord --> Replica A (slow...)        Coord --> Replica A (slow...)
            wait...wait...wait                   |
            Replica A responds                   +-- timeout threshold hit
            Total: 200ms                         +--> Replica B (fast!)
                                                 Replica B responds
                                                 Total: 15ms

  Significantly reduces P99 tail latency.
```

---

## 11. Anti-Entropy and Repair

### Read Repair

```
  Read at QUORUM (RF=3):

  Coordinator asks Node A and Node B:

  Node A:  user:alice = { name: "Alice", age: 30 }   ts=100
  Node B:  user:alice = { name: "Alice", age: 31 }   ts=150  <-- newer

  Coordinator returns Node B's data (higher timestamp).

  Background: Coordinator sends Node B's data to Node A:
  "Hey Node A, update user:alice to age=31, ts=150"

  Now all replicas are consistent!
```

### Hinted Handoff

```
  Normal Write (RF=3):               Hinted Handoff:

  Coord --> Node A  ACK              Coord --> Node A  ACK
  Coord --> Node B  ACK              Coord --> Node B  ACK
  Coord --> Node C  ACK              Coord --> Node C  UNREACHABLE!
                                              |
                                     Coord stores HINT locally:
                                     +-----------------------------+
                                     | Hint for Node C:            |
                                     | key=user:alice, val=...,    |
                                     | ts=150                      |
                                     +-----------------------------+
                                              |
                                     (Node C comes back online)
                                              |
                                     Coord replays hint --> Node C
                                     Node C is caught up!

  Hints expire after 3 hours (configurable).
```

### Anti-Entropy Repair (Merkle Trees)

```
  nodetool repair

  Node A                              Node B
  +------------------+                +------------------+
  | Build Merkle Tree|                | Build Merkle Tree|
  |                  |                |                  |
  |      [root]      |                |      [root]      |
  |      /    \      |                |      /    \      |
  |   [H1]   [H2]   |  -- compare -> |   [H1]   [H2']  |
  |   / \    / \     |               |   / \    / \     |
  | [a] [b][c] [d]  |                | [a] [b][c'] [d] |
  +------------------+                +------------------+

  H2 != H2'  -->  Subtree differs!
  Only stream data in [c'] range (not entire dataset).

  Incremental Repair:
  - Marks SSTables as "repaired" after validation
  - Future repairs only process UNREPAIRED SSTables
  - Much faster and less I/O intensive
```

---

## 12. Data Modeling

```
  Relational vs. Cassandra Approach

  Relational (normalize, then JOIN):

  Users             Orders              Products
  +----+------+     +----+-----+----+   +----+---------+
  | id | name |     | id | uid | pid|   | id | name    |
  +----+------+     +----+-----+----+   +----+---------+
  | 1  | Alice|     | 10 |  1  | 50 |   | 50 | Widget  |
  +----+------+     +----+-----+----+   +----+---------+

  SELECT * FROM orders JOIN users JOIN products ...


  Cassandra (denormalize, one table per query):

  Query: "Get all orders for a user, sorted by date"

  orders_by_user
  +----------+------------+--------+------------+---------+
  | user_id  | order_date | ord_id | product    | amount  |
  | (PK)     | (CK DESC)  |        |            |         |
  +----------+------------+--------+------------+---------+
  | alice    | 2026-02-17 | 10     | Widget     | 29.99   |
  | alice    | 2026-02-10 | 8      | Gadget     | 49.99   |
  | bob      | 2026-02-15 | 9      | Widget     | 29.99   |
  +----------+------------+--------+------------+---------+

  No JOINs. Single partition scan. Fast.
```

### Partition Sizing Guidelines

```
  +---------------------------------------+
  |  Partition Size Guidelines            |
  |                                       |
  |  Target:  < 100 MB per partition      |
  |           < 100,000 rows              |
  |                                       |
  |  Too Large:                           |
  |  +----------------------------------+ |
  |  | sensor_id = "temp-1"             | |
  |  | 5 years of readings              | |
  |  | 500 MB, 10M rows     BAD!       | |
  |  +----------------------------------+ |
  |                                       |
  |  Fix: Add time bucket to PK:         |
  |  PRIMARY KEY ((sensor_id, month),     |
  |               reading_time)           |
  |                                       |
  |  +------------------+                 |
  |  | temp-1 | 2026-01 |  ~8 MB   GOOD  |
  |  | temp-1 | 2026-02 |  ~8 MB   GOOD  |
  |  +------------------+                 |
  +---------------------------------------+
```

---

## 13. Storage Engine Internals

### SSTable Compression

```
  SSTable Data File (compressed with LZ4)

  +--------+--------+--------+--------+--------+
  | Chunk 1| Chunk 2| Chunk 3| Chunk 4| Chunk 5|   (16 KB each, default)
  +--------+--------+--------+--------+--------+

  Compression Offsets File:
  +-------+-------+-------+-------+-------+
  |  0    | 4012  | 7890  | 11920 | 15800 |   byte offsets
  +-------+-------+-------+-------+-------+

  Random read for partition in Chunk 3:
  --> Offset table says Chunk 3 starts at byte 7890
  --> Decompress ONLY Chunk 3 (not entire file)
  --> Read partition data
```

### Bloom Filter Tuning

```
  bloom_filter_fp_chance settings:

  +-------------------+----------+--------+--------------------------+
  | fp_chance         | Memory   | False  | Use Case                 |
  |                   | Usage    | Pos.   |                          |
  +-------------------+----------+--------+--------------------------+
  | 0.1  (default)    | Low      | 10%    | Write-heavy, few reads   |
  | 0.01              | Medium   | 1%     | Read-heavy tables        |
  | 0.001             | High     | 0.1%   | Critical read perf       |
  +-------------------+----------+--------+--------------------------+

  Lower fp_chance = more memory, fewer wasted disk reads
```

---

## 14. Multi-Datacenter Architecture

```
  Active-Active Multi-DC Deployment

  +------ DC1 (us-east) ------+        +------ DC2 (eu-west) ------+
  |  RF = 3                   |        |  RF = 2                   |
  |                           |        |                           |
  |  +-----+  +-----+        |  async |        +-----+  +-----+  |
  |  |Node1|  |Node2|        | <----> |        |Node5|  |Node6|  |
  |  +-----+  +-----+        |  repl. |        +-----+  +-----+  |
  |       +-----+             |        |                           |
  |       |Node3|             |        |                           |
  |       +-----+             |        |                           |
  |                           |        |                           |
  |  App servers (US users)   |        |  App servers (EU users)   |
  |  Use LOCAL_QUORUM         |        |  Use LOCAL_QUORUM         |
  +---------------------------+        +---------------------------+

  - Each DC serves reads/writes independently (active-active)
  - Cross-DC replication is asynchronous
  - LOCAL_QUORUM ensures low latency (no cross-DC wait)
  - If DC1 goes down, DC2 continues serving traffic

  Deployment Patterns:
  +----------------------------+----------------------------------------+
  | Pattern                    | Description                            |
  +----------------------------+----------------------------------------+
  | Active-Active              | Both DCs serve live traffic             |
  | Active-Passive             | DC2 is standby for disaster recovery   |
  | Analytics DC               | Dedicated DC for analytics workloads   |
  | Hybrid Cloud               | On-prem DC + cloud DC                  |
  +----------------------------+----------------------------------------+
```

---

## 15. Security Architecture

```
  Security Layers

  +-------------------------------------------------------+
  |                    CLIENT                              |
  +-------------------------------------------------------+
         |                                    ^
         | TLS/SSL (client-to-node)           | TLS/SSL
         v                                    |
  +-------------------------------------------------------+
  |                CASSANDRA NODE                          |
  |                                                        |
  |  1. AUTHENTICATION                                     |
  |     +----------------------------------------------+   |
  |     | PasswordAuthenticator (default)              |   |
  |     | Credentials stored in system_auth keyspace   |   |
  |     | Pluggable (LDAP, Kerberos, custom)           |   |
  |     +----------------------------------------------+   |
  |                                                        |
  |  2. AUTHORIZATION (RBAC)                               |
  |     +----------------------------------------------+   |
  |     | GRANT SELECT ON keyspace.table TO role;      |   |
  |     | Granularity: keyspace / table / function     |   |
  |     +----------------------------------------------+   |
  |                                                        |
  |  3. ENCRYPTION                                         |
  |     Client-to-Node: TLS for app traffic                |
  |     Node-to-Node:   TLS for gossip & streaming         |
  +-------------------------------------------------------+
         |                         |
         | TLS (node-to-node)      | TLS (node-to-node)
         v                         v
  +--------------+          +--------------+
  | Other Nodes  |          | Other Nodes  |
  +--------------+          +--------------+
```

---

## 16. Performance Characteristics

```
  Write vs Read Performance Profile

  WRITES:                              READS:
  +----------------------------+       +----------------------------+
  | Commit log (sequential)    |       | Depends on:                |
  | + Memtable (in-memory)     |       |  - # SSTables per partition|
  | = Sub-millisecond          |       |  - Cache hit rates         |
  |                            |       |  - Consistency level       |
  | Consistent & predictable   |       |  - Partition size          |
  +----------------------------+       |                            |
                                       | Well-tuned: 1-5ms (CL=ONE)|
                                       | Variable by workload       |
                                       +----------------------------+

  Operational Health Checklist:
  +----+----------------------------------------------+
  | #  | Monitor                                      |
  +----+----------------------------------------------+
  | 1  | Compaction throughput & pending tasks         |
  | 2  | Repair schedule (within gc_grace_seconds)     |
  | 3  | JVM garbage collection pauses                |
  | 4  | Partition sizes (avoid hotspots)              |
  | 5  | Read/write latencies (P50, P99, P999)        |
  +----+----------------------------------------------+
```

---

## 17. Evolution and Modern Developments

```
  Cassandra Version Timeline

  2008    2021          2022          2023-2024
  |       |             |             |
  v       v             v             v
  Open    4.0           4.1           5.0
  Source   |             |             |
          |             |             +-- Storage-Attached Indexes (SAI)
          |             |             +-- Unified Compaction Strategy
          |             |             +-- Vector search (AI/ML)
          |             |             +-- Dynamic data masking
          |             |             +-- Trie-based index format
          |             |
          |             +-- Pluggable memtable implementations
          |             +-- Improved guardrails
          |
          +-- Stability: extensive fuzz testing
          +-- Removed experimental features
```

---

## Full Architecture Summary

```
  +====================================================================+
  |                    CASSANDRA ARCHITECTURE OVERVIEW                  |
  +====================================================================+
  |                                                                    |
  |  CLIENT (token-aware driver)                                       |
  |    |                                                               |
  |    v                                                               |
  |  COORDINATOR NODE  (any node on the ring)                          |
  |    |                                                               |
  |    +---> Determine replicas via token ring + replication strategy   |
  |    +---> Forward to replica nodes                                  |
  |    +---> Collect responses per consistency level                   |
  |    +---> Return result to client                                   |
  |                                                                    |
  |  REPLICA NODE (write path):                                        |
  |    Commit Log --> Memtable --> [flush] --> SSTable (immutable)      |
  |                                                                    |
  |  REPLICA NODE (read path):                                         |
  |    Memtable + Bloom Filter --> Index --> SSTable --> Merge by ts    |
  |                                                                    |
  |  BACKGROUND PROCESSES:                                             |
  |    - Compaction (merge SSTables, discard tombstones)               |
  |    - Gossip (cluster state, failure detection)                     |
  |    - Hinted handoff (temporary failure recovery)                   |
  |    - Read repair (opportunistic consistency fix)                   |
  |    - Anti-entropy repair (Merkle tree-based full repair)           |
  |                                                                    |
  |  CLUSTER-WIDE:                                                     |
  |    - Peer-to-peer (no master)                                      |
  |    - Virtual nodes for balanced distribution                       |
  |    - Multi-DC replication with rack awareness                      |
  |    - Tunable consistency (ANY --> ALL per query)                   |
  +====================================================================+
```

---

*Source: [Apache Cassandra Architecture — A Comprehensive Guide](https://techie007.substack.com/p/apache-cassandra-architecture-a-comprehensive)*
