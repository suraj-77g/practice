# Data Modeling Essentials

> Concise guide for system design interviews. Design a schema that works, then move on.

---

## Quick Decision: Which Database?

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         START HERE                                      │
│                             │                                           │
│                             ▼                                           │
│              Need ACID transactions / complex queries?                  │
│                      /              \                                   │
│                    YES               NO                                 │
│                    /                  \                                 │
│                   ▼                    ▼                                │
│           PostgreSQL             What's the pattern?                    │
│           (default)                    │                                │
│                          ┌─────────────┼─────────────┐                  │
│                          ▼             ▼             ▼                  │
│                    Simple K/V     Time-series    Schema changes         │
│                    lookups?       / Analytics?   frequently?            │
│                          │             │             │                  │
│                          ▼             ▼             ▼                  │
│                       Redis       Cassandra      MongoDB                │
│                    (+ SQL behind)                                       │
└─────────────────────────────────────────────────────────────────────────┘

TL;DR: Default to PostgreSQL. It handles 90% of cases.
       Even Facebook, Airbnb, Stripe use relational DBs at core.
```

---

## 1. Database Types

### Relational Databases (SQL) — Default Choice

```
Tables with fixed schema. Rows = entities, Columns = attributes.
Relationships via foreign keys. ACID guarantees.

┌─────────────────────────────────────────────────────────────────────────┐
│  USERS                                                                  │
├──────────┬────────────┬─────────────────────┬───────────────────────────┤
│ id (PK)  │ username   │ email               │ created_at                │
├──────────┼────────────┼─────────────────────┼───────────────────────────┤
│ 1        │ john_doe   │ john@example.com    │ 2024-01-01 10:00:00       │
│ 2        │ jane_doe   │ jane@example.com    │ 2024-01-01 10:05:00       │
└──────────┴────────────┴─────────────────────┴───────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  POSTS                                                                  │
├──────────┬──────────────────┬───────────────────┬───────────────────────┤
│ id (PK)  │ user_id (FK)     │ content           │ created_at            │
├──────────┼──────────────────┼───────────────────┼───────────────────────┤
│ 1        │ 1                │ Hello, world!     │ 2024-01-01 10:00:00   │
│ 2        │ 1                │ My first post     │ 2024-01-01 10:05:00   │
│ 3        │ 2                │ Another post      │ 2024-01-01 10:10:00   │
└──────────┴──────────────────┴───────────────────┴───────────────────────┘

Good for:
  ✓ Complex queries with JOINs
  ✓ Strong consistency (payments, inventory, bookings)
  ✓ Most CRUD applications
  ✓ When you need ACID guarantees

Scaling:
  Read replicas, sharding, connection pooling, caching
  (Facebook, Airbnb, Stripe all built on relational foundations)

Examples: PostgreSQL, MySQL, SQLite
```

### Document Databases (NoSQL)

```
JSON-like documents with flexible schema. No fixed columns.

{
  "_id": "507f1f77bcf86cd799439011",
  "username": "john_doe",
  "email": "john@example.com",
  "posts": [
    {"content": "Hello, world!", "created_at": "2024-01-01T10:00:00Z"},
    {"content": "My first post", "created_at": "2024-01-01T10:05:00Z"}
  ]
}

Good for:
  ✓ Rapidly changing schema
  ✓ Deeply nested data (avoid many JOINs)
  ✓ Records with different structures

Bad for:
  ✗ Complex queries across relationships
  ✗ System design interviews (requirements are well-scoped)

Examples: MongoDB, Firestore, CouchDB
```

### Key-Value Stores

```
Simple: key → value. Extremely fast lookups.

┌───────────────────────┬──────────────────────────────────────────┐
│ Key                   │ Value                                    │
├───────────────────────┼──────────────────────────────────────────┤
│ user:123              │ {"name": "John", "email": "j@x.com"}     │
│ session:abc123        │ {"user_id": 123, "expires": 1640995200}  │
│ cache:post:456        │ {"title": "...", "likes": 42}            │
└───────────────────────┴──────────────────────────────────────────┘

Good for:
  ✓ Caching (put in front of SQL)
  ✓ Session storage
  ✓ Feature flags
  ✓ Rate limiting counters

Pattern: SQL as source of truth + Redis cache for hot data

Examples: Redis, DynamoDB, Memcached
```

### Wide-Column Databases

**What Does "Wide Column" Mean?**
```
In SQL, every row has the SAME columns (fixed schema):

┌─────────┬──────────┬─────────┬───────────┐
│ user_id │ name     │ email   │ phone     │  ← Every row has these 4 columns
├─────────┼──────────┼─────────┼───────────┤
│ 1       │ John     │ j@x.com │ 555-1234  │
│ 2       │ Jane     │ j@y.com │ 555-5678  │
│ 3       │ Bob      │ b@z.com │ 555-9999  │
└─────────┴──────────┴─────────┴───────────┘

In Wide-Column, each row can have DIFFERENT columns:

┌─────────┬──────────┬─────────┬───────────┬───────────┬───────────┐
│ row_key │ col_1    │ col_2   │ col_3     │ col_4     │ col_N...  │
├─────────┼──────────┼─────────┼───────────┼───────────┼───────────┤
│ user:1  │ name:John│ email:..│ phone:... │           │           │
│ user:2  │ name:Jane│ email:..│           │           │           │  ← no phone
│ user:3  │ name:Bob │ twitter:│ linkedin: │ github:..│ blog:...  │  ← extra cols
└─────────┴──────────┴─────────┴───────────┴───────────┴───────────┘

"Wide" = rows can have millions of columns, and each row is different.
```

**How It Actually Works**
```
Data is organized into:
  1. Partition Key  → determines which node stores the data
  2. Clustering Key → determines sort order within partition
  3. Columns        → the actual data (can vary per row)

┌─────────────────────────────────────────────────────────────────────────┐
│  TABLE: user_activity                                                   │
│  Partition Key: user_id                                                 │
│  Clustering Key: timestamp (DESC)                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Partition: user_id = "user_123"                                        │
│  ┌───────────────────────┬────────────┬──────────────────────────────┐  │
│  │ timestamp (cluster)   │ event_type │ data                         │  │
│  ├───────────────────────┼────────────┼──────────────────────────────┤  │
│  │ 2024-01-15 10:05:00   │ click      │ {page: "/product/123"}       │  │
│  │ 2024-01-15 10:04:30   │ view       │ {page: "/home"}              │  │
│  │ 2024-01-15 10:03:00   │ login      │ {ip: "73.42.15.99"}          │  │
│  │ ...thousands more...  │            │                              │  │
│  └───────────────────────┴────────────┴──────────────────────────────┘  │
│                                                                         │
│  Partition: user_id = "user_456"                                        │
│  ┌───────────────────────┬────────────┬──────────────────────────────┐  │
│  │ 2024-01-15 09:00:00   │ purchase   │ {order_id: "ord_789"}        │  │
│  │ 2024-01-14 15:30:00   │ view       │ {page: "/cart"}              │  │
│  └───────────────────────┴────────────┴──────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

Key insight:
  - All data for user_123 is stored TOGETHER on one node
  - Sorted by timestamp → range queries are fast
  - Can have millions of rows per partition
```

**Why It's Fast for Writes**
```
SQL Write:                          Wide-Column Write:
  1. Find correct page               1. Append to commit log (sequential)
  2. Check constraints               2. Update memtable (in-memory)
  3. Update indexes                  3. Done! (flushed to disk later)
  4. Write to transaction log
  5. Update table                    No random I/O, no locks, no indexes
                                     to update immediately.

Result: 10-100x faster writes than SQL for append-heavy workloads
```

**Example: Time-Series Metrics**
```
Storing server metrics (CPU, memory, disk) every second:

CREATE TABLE server_metrics (
  server_id    TEXT,
  metric_date  DATE,
  timestamp    TIMESTAMP,
  metric_name  TEXT,
  value        DOUBLE,
  PRIMARY KEY ((server_id, metric_date), timestamp, metric_name)
);
            ↑                        ↑
     Partition Key              Clustering Key
  (server + date = 1 partition)  (sorted by time, then metric)

Query: "Get CPU metrics for server-1 on Jan 15"
  → Goes to ONE partition
  → Scans sorted range
  → Super fast

Data layout on disk:
┌─────────────────────────────────────────────────────────────────────────┐
│  Partition: (server-1, 2024-01-15)                                      │
│  ───────────────────────────────────────────────────────────────────    │
│  10:00:00 | cpu    | 45.2                                               │
│  10:00:00 | memory | 78.5                                               │
│  10:00:00 | disk   | 23.1                                               │
│  10:00:01 | cpu    | 46.8                                               │
│  10:00:01 | memory | 78.6                                               │
│  ... 86,400 seconds × 3 metrics = 259,200 rows per day per server       │
└─────────────────────────────────────────────────────────────────────────┘
```

**Example: Chat Messages**
```
CREATE TABLE messages (
  conversation_id  TEXT,
  sent_at          TIMESTAMP,
  sender_id        TEXT,
  content          TEXT,
  PRIMARY KEY (conversation_id, sent_at)
);

Query: "Get last 50 messages in conversation X"
  SELECT * FROM messages 
  WHERE conversation_id = 'conv_123'
  ORDER BY sent_at DESC
  LIMIT 50;

All messages for a conversation stored together, sorted by time.
No JOINs, no indexes to maintain, just sequential read.
```

**Real-World Examples**
```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Company         │ Use Case                                             │
├─────────────────┼──────────────────────────────────────────────────────┤
│ Netflix         │ Viewing history, billions of events/day              │
│                 │ Partition: user_id, Cluster: timestamp               │
│                 │                                                      │
│ Discord         │ Message storage, 150M+ messages/day                  │
│                 │ Partition: channel_id, Cluster: message_id           │
│                 │                                                      │
│ Instagram       │ Feed storage, user timelines                         │
│                 │ Partition: user_id, Cluster: post_timestamp          │
│                 │                                                      │
│ Uber            │ Trip history, driver locations                       │
│                 │ Partition: driver_id + date, Cluster: timestamp      │
│                 │                                                      │
│ Apple           │ 10+ petabytes in Cassandra for various services      │
│                 │                                                      │
│ Spotify         │ User activity, playlist data                         │
│                 │ Partition: user_id, Cluster: timestamp               │
└─────────────────┴──────────────────────────────────────────────────────┘
```

**Wide-Column vs SQL**
```
┌─────────────────────┬─────────────────────┬─────────────────────────────┐
│                     │ SQL (PostgreSQL)    │ Wide-Column (Cassandra)     │
├─────────────────────┼─────────────────────┼─────────────────────────────┤
│ Schema              │ Fixed               │ Flexible per row            │
│ Writes              │ Moderate            │ Extremely fast (append)     │
│ Reads               │ Flexible (any query)│ Must query by partition key │
│ JOINs               │ Yes                 │ No                          │
│ Transactions        │ Full ACID           │ Limited (per-partition)     │
│ Consistency         │ Strong              │ Tunable (eventual default)  │
│ Scaling writes      │ Hard (single master)│ Easy (multi-master)         │
│ Best for            │ Complex queries     │ Known access patterns       │
└─────────────────────┴─────────────────────┴─────────────────────────────┘
```

**When to Use**
```
✓ Time-series data (metrics, logs, events, IoT sensors)
✓ Massive write volumes (millions of writes/second)
✓ Data with natural partition key (user_id, device_id, conversation_id)
✓ Queries always include partition key

✗ Ad-hoc queries ("find all users who...")
✗ Complex JOINs across entities
✗ Strong consistency requirements
✗ Small datasets (overkill)

Examples: Cassandra, HBase, ScyllaDB, Google Bigtable
```

### Graph Databases

```
Nodes and edges. Optimized for traversing relationships.

    (User: John) ──FOLLOWS──► (User: Jane)
         │                        │
      POSTED                   POSTED
         │                        │
         ▼                        ▼
    (Post: Hello)           (Post: Hi!)
         │
      LIKED_BY
         │
         ▼
    (User: Bob)

When to use: Almost never in interviews.
  Even Facebook and LinkedIn use MySQL for core social graph.
  
Examples: Neo4j, Amazon Neptune
```

### Time Series Databases

**What Is Time Series Data?**
```
Data points indexed by time, arriving continuously.

Examples:
  - Server metrics: CPU 45%, Memory 78%, every second
  - Stock prices: $142.50 at 10:00:01, $142.52 at 10:00:02
  - IoT sensors: Temperature 72°F, Humidity 45%, every minute
  - User analytics: Page views, clicks, conversions per second
  - Application logs: Errors, requests, latency measurements

Characteristics:
  ✓ Append-only (rarely update old data)
  ✓ Time is always the primary dimension
  ✓ High write volume (millions of points/second)
  ✓ Queries are time-range based ("last hour", "past 7 days")
  ✓ Old data is often downsampled or deleted
```

**Problem It Solves**
```
Why not just use SQL or Wide-Column?

SQL Problem:
  - 1 million metrics × 1 sample/second = 86 billion rows/day
  - Table scans become impossible
  - Indexes grow huge
  - No built-in time-based aggregation

Wide-Column (Cassandra) helps with scale, but...
  - No native time-based functions (AVG per hour, percentiles)
  - No automatic data retention/downsampling
  - Manual partitioning by time

Time Series DB provides:
  ✓ Optimized storage for timestamp + value
  ✓ Built-in aggregation (AVG, MAX, PERCENTILE)
  ✓ Automatic data retention policies
  ✓ Downsampling (1-second → 1-minute → 1-hour)
  ✓ Compression designed for time-series patterns
```

**How Data Is Stored (and Why It Helps)**

```
┌─────────────────────────────────────────────────────────────────────────┐
│  SQL STORAGE vs TIME-SERIES STORAGE                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  SQL (row-based, scattered):                                            │
│                                                                         │
│    Page 1: [row 1, row 47, row 892, ...]   ← rows scattered by ID      │
│    Page 2: [row 3, row 15, row 201, ...]                                │
│    Page 3: [row 8, row 99, row 500, ...]                                │
│                                                                         │
│    Query "last 1 hour" → might touch EVERY page (random I/O)           │
│                                                                         │
│  ─────────────────────────────────────────────────────────────────────  │
│                                                                         │
│  Time-series (time-ordered blocks):                                     │
│                                                                         │
│    Block 1: [all data from 00:00-02:00, compressed together]           │
│    Block 2: [all data from 02:00-04:00, compressed together]           │
│    Block 3: [all data from 04:00-now, in memory]                       │
│                                                                         │
│    Query "last 1 hour" → read exactly 1 block (sequential I/O)         │
│                                                                         │
│  Key insight: Time-series queries are ALWAYS time-range based.         │
│  Store data by time → queries read contiguous chunks → fast.           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  COMPRESSION (10-20x smaller)                                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Timestamps: 10:00:01, 10:00:02, 10:00:03 → store deltas: +1, +1, +1   │
│  Values: 45.23, 45.24, 45.22 → store XOR diff (similar = tiny diff)    │
│                                                                         │
│  Result: 16 bytes/point → 1-2 bytes/point                              │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  DOWNSAMPLING (keep old data smaller)                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  60 one-second samples → 1 one-minute summary (avg/min/max)            │
│                                                                         │
│    0-7 days:    1-second resolution (debug recent issues)              │
│    7-30 days:   1-minute resolution (60x smaller)                      │
│    30-365 days: 1-hour resolution   (3600x smaller)                    │
│                                                                         │
│  Why keep max? avg(45, 46, 99, 45) = 58 hides the spike. max=99 keeps. │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Real-World Access Patterns**

```
┌─────────────────────────────────────────────────────────────────────────┐
│  PATTERN 1: Dashboard (Grafana showing server health)                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Query: "Average CPU per host, last 6 hours, 1-minute resolution"      │
│                                                                         │
│  avg(cpu_usage{env="prod"}) by (host) [6h:1m]                          │
│                                                                         │
│  How TSDB handles it:                                                   │
│    1. Find all series matching env="prod" (inverted index)             │
│    2. Read last 3 time blocks (each block = 2 hours)                   │
│    3. Aggregate to 1-minute buckets in memory                          │
│    4. Return 360 points per host                                        │
│                                                                         │
│  Why fast: 3 sequential block reads, not billions of row lookups       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  PATTERN 2: Alerting (PagerDuty trigger)                                │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Query: "Any host with CPU > 90% for last 5 minutes?"                  │
│                                                                         │
│  avg(cpu_usage) by (host) > 0.9 for 5m                                 │
│                                                                         │
│  How TSDB handles it:                                                   │
│    1. Every 15 seconds, read last 5 mins from current in-memory block  │
│    2. Compute avg per host                                              │
│    3. Fire alert if threshold exceeded                                  │
│                                                                         │
│  Why fast: Recent data in memory, no disk reads at all                 │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  PATTERN 3: Incident Investigation ("What happened at 3 AM?")          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Query: "All metrics for service=payments, 02:55-03:15, full detail"   │
│                                                                         │
│  {service="payments"}[02:55:00 - 03:15:00]                             │
│                                                                         │
│  How TSDB handles it:                                                   │
│    1. Find series with service="payments" (maybe 50 metrics)           │
│    2. Seek to 02:00-04:00 block (skip all other blocks)                │
│    3. Decompress only 02:55-03:15 range within block                   │
│    4. Return 1200 points (20 mins × 1/sec × 50 metrics)                │
│                                                                         │
│  Why fast: Direct seek to time range, decompress small window          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  PATTERN 4: Capacity Planning ("Show me last 90 days trend")           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Query: "Daily max memory usage across all prod hosts"                 │
│                                                                         │
│  max(max_over_time(memory_usage{env="prod"}[1d])) [90d]               │
│                                                                         │
│  How TSDB handles it:                                                   │
│    1. Use DOWNSAMPLED data (1-hour resolution, not 1-second)           │
│    2. Read 90 days × 24 hours = 2160 points per host                   │
│    3. Aggregate to daily max                                            │
│    4. Return 90 points                                                  │
│                                                                         │
│  Why fast: Downsampled = 3600x less data to read                       │
│  Without downsampling: 90 × 86400 = 7.7M points (slow!)                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  PATTERN 5: High-Volume Ingest (IoT / Telemetry)                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Scenario: 10,000 devices sending metrics every second                 │
│            = 10,000 writes/second = 864M points/day                    │
│                                                                         │
│  How TSDB handles it:                                                   │
│    1. All writes go to in-memory buffer (no disk I/O)                  │
│    2. Buffer flushed to disk every 2 hours as compressed block         │
│    3. No per-write index updates, no locks                             │
│                                                                         │
│  Why fast: Append-only + batch compression                             │
│  SQL would need: 10K inserts/sec × index updates = bottleneck          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**Labels & Cardinality Warning**

```
Labels identify unique time series:
  cpu_usage{host="server-1", region="us-east"} = 45.2

Query by any label: cpu_usage{region="us-east"} → inverted index lookup

⚠️  Never use high-cardinality values as labels (user_id, IP, request_id)
    100 hosts × 10 regions = 1,000 series (fine)
    + 1M user_ids = 1 BILLION series (database crashes)
```

**Data Model Example**
```
Prometheus/InfluxDB style:

┌─────────────────────────────────────────────────────────────────────────┐
│  METRIC NAME                    LABELS                    VALUE         │
├─────────────────────────────────────────────────────────────────────────┤
│  http_requests_total           {method="GET", path="/api"} @ 10:00:01   │
│                                                             1523        │
│                                                                         │
│  http_request_duration_seconds {method="POST", path="/login"}           │
│                                @ 10:00:01 = 0.234                       │
│                                @ 10:00:02 = 0.189                       │
│                                @ 10:00:03 = 0.201                       │
│                                                                         │
│  temperature                   {sensor="living-room", unit="F"}         │
│                                @ 10:00:00 = 72.1                        │
│                                @ 10:01:00 = 72.3                        │
└─────────────────────────────────────────────────────────────────────────┘

Queries:
  - rate(http_requests_total[5m])  → requests per second over last 5 min
  - avg_over_time(temperature[1h]) → average temp over last hour
  - histogram_quantile(0.99, http_request_duration) → p99 latency
```

**Query Examples (PromQL / Flux)**
```
PromQL (Prometheus):
──────────────────
# Average CPU over last 5 minutes
avg(rate(cpu_usage_seconds_total[5m])) by (host)

# 99th percentile latency
histogram_quantile(0.99, rate(http_request_duration_bucket[5m]))

# Alert: CPU > 80% for 5 minutes
avg(cpu_usage) by (host) > 0.8

InfluxDB Flux:
──────────────
from(bucket: "metrics")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "cpu")
  |> aggregateWindow(every: 5m, fn: mean)
```

**Real-World Examples**
```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Company         │ Use Case                                             │
├─────────────────┼──────────────────────────────────────────────────────┤
│ Uber            │ Real-time monitoring of millions of trips            │
│                 │ Uses M3 (built on top of Prometheus concepts)        │
│                 │                                                      │
│ Netflix         │ Atlas - 2+ billion time series, 2.5 PB of metrics    │
│                 │ Monitors all streaming infrastructure                │
│                 │                                                      │
│ Cloudflare      │ 25+ million requests/second, all tracked as metrics  │
│                 │ ClickHouse + custom time-series storage              │
│                 │                                                      │
│ Tesla           │ Telemetry from millions of vehicles                  │
│                 │ Battery temp, motor RPM, GPS, every second           │
│                 │                                                      │
│ Robinhood       │ Stock price tracking, trade execution metrics        │
│                 │ TimescaleDB (Postgres extension)                     │
│                 │                                                      │
│ Datadog         │ SaaS monitoring platform                             │
│                 │ Trillions of data points across customer infra       │
│                 │                                                      │
│ Grafana Cloud   │ Hosted Prometheus/Mimir for thousands of companies   │
└─────────────────┴──────────────────────────────────────────────────────┘
```

**Common Time Series Databases**
```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Database        │ Notes                                                │
├─────────────────┼──────────────────────────────────────────────────────┤
│ Prometheus      │ Pull-based, great for Kubernetes/container metrics   │
│                 │ Limited to single node, use Thanos/Mimir to scale    │
│                 │                                                      │
│ InfluxDB        │ Push-based, SQL-like query language (Flux)           │
│                 │ Good for IoT, application metrics                    │
│                 │                                                      │
│ TimescaleDB     │ PostgreSQL extension (SQL + time-series features)    │
│                 │ Good if you want SQL compatibility                   │
│                 │                                                      │
│ ClickHouse      │ Column-oriented, extremely fast analytics            │
│                 │ Used for logs + metrics + analytics                  │
│                 │                                                      │
│ QuestDB         │ High-performance, SQL interface                      │
│                 │ 1.4M writes/second on single node                    │
│                 │                                                      │
│ VictoriaMetrics │ Prometheus-compatible, better resource efficiency    │
│                 │ Drop-in replacement for Prometheus                   │
└─────────────────┴──────────────────────────────────────────────────────┘
```

**When to Use**
```
✓ Monitoring and observability (server metrics, APM)
✓ IoT sensor data (temperature, pressure, GPS)
✓ Financial data (stock prices, trading metrics)
✓ Real-time analytics dashboards
✓ Log aggregation with time-based queries
✓ Any data where time is the primary query dimension

✗ Transactional data (orders, users) → use SQL
✗ Frequent updates to old data → time series is append-optimized
✗ Complex JOINs across entities → use SQL
✗ Small datasets → overkill, just use PostgreSQL

Interview tip:
  "For metrics and monitoring, I'd use a time-series database like
   Prometheus or InfluxDB, since it handles high write volume and
   has built-in aggregation functions for dashboards."
```

### Quick Comparison

```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Type            │ Use When                                             │
├─────────────────┼──────────────────────────────────────────────────────┤
│ SQL (Postgres)  │ DEFAULT. Complex queries, ACID, most applications    │
│ Document        │ Rapidly changing schema, nested data                 │
│ Key-Value       │ Caching, sessions, simple lookups                    │
│ Wide-Column     │ Time-series, massive writes, analytics               │
│ Graph           │ Rarely. Complex relationship traversals              │
└─────────────────┴──────────────────────────────────────────────────────┘
```

---

## 2. Schema Design Process

### Step 1: Identify Core Entities

```
From requirements → What "things" exist in the system?

Twitter example:
  Entities: users, tweets, follows, likes, retweets

E-commerce example:
  Entities: users, products, orders, order_items, payments

Ticketmaster example:
  Entities: users, events, venues, tickets, bookings
```

### Step 2: Define Primary Keys

```
Every entity needs a unique identifier.

Rules:
  ✓ Use system-generated IDs (user_id, post_id)
  ✗ Don't use business data (email, SSN) — they change

Options:
  - Auto-increment: 1, 2, 3, ... (simple, but exposes count)
  - UUID: 550e8400-e29b-41d4-a716-446655440000 (random, no ordering)
  - ULID/Snowflake: Time-sortable unique IDs (best of both)

Interview tip: Just say "id will be our primary key" and move on.
```

### Step 3: Map Relationships

```
┌─────────────────────────────────────────────────────────────────────────┐
│  ONE-TO-MANY (1:N)                                                      │
│  ─────────────────                                                      │
│  A user has many posts. A post belongs to one user.                     │
│                                                                         │
│  users: id, username, email                                             │
│  posts: id, user_id (FK → users.id), content                            │
│                      ↑                                                  │
│              Foreign key creates the relationship                       │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  MANY-TO-MANY (N:M)                                                     │
│  ─────────────────                                                      │
│  Users follow many users. Users are followed by many users.             │
│                                                                         │
│  users: id, username                                                    │
│  follows: follower_id (FK), followee_id (FK)                            │
│           ↑                                                             │
│      Junction table / join table                                        │
│                                                                         │
│  Example: likes                                                         │
│  likes: user_id (FK), post_id (FK), created_at                          │
│         (composite primary key: user_id + post_id)                      │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  ONE-TO-ONE (1:1)                                                       │
│  ────────────────                                                       │
│  Rare. Often means tables should be merged.                             │
│                                                                         │
│  Example: user + user_profile                                           │
│  (Usually just put profile fields in users table)                       │
└─────────────────────────────────────────────────────────────────────────┘
```

### Step 4: Add Constraints

```
Enforce correctness at database level:

NOT NULL      → Field must have a value
UNIQUE        → No duplicates (email, username)
CHECK         → Custom validation (price > 0, age >= 18)
FOREIGN KEY   → Referential integrity (no orphaned records)

Example:
  CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT NOW()
  );

Trade-off: Constraints add write overhead but prevent bad data.
At extreme scale, some companies drop FKs and enforce in application.
```

---

## 3. Indexing

### What Is an Index?

```
Without index:                    With index:
  Find user by email?               Find user by email?
  → Scan ALL rows (O(n))            → B-tree lookup (O(log n))
  
  1M users = 1M comparisons         1M users = ~20 comparisons

Index = data structure (usually B-tree) for fast lookups.
Like a book's index: jump to page 149 instead of reading every page.
```

### Index for Your Access Patterns

```
Think: What queries will my API endpoints need?

GET /users/{id}           → Primary key (indexed by default)
GET /users?email=x        → Index on email
GET /posts?user_id=x      → Index on user_id
GET /posts?user_id=x&     → Composite index on (user_id, created_at)
    order_by=created_at

Example schema with indexes:
  posts:
    - id (PK, auto-indexed)
    - user_id (FK, INDEX)
    - content
    - created_at (INDEX)
    - INDEX (user_id, created_at)  ← composite for user's recent posts
```

### Index Trade-offs

```
Pros:
  ✓ Dramatically faster reads
  ✓ Essential for any column in WHERE, ORDER BY, JOIN

Cons:
  ✗ Slower writes (must update index on insert/update)
  ✗ Storage overhead
  ✗ Too many indexes = diminishing returns

Rule: Index columns you query by. Don't index everything.
```

### Common Index Types

```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Type            │ Use Case                                             │
├─────────────────┼──────────────────────────────────────────────────────┤
│ B-tree          │ Default. Equality, range queries, sorting            │
│ Hash            │ Equality only (=), faster than B-tree for exact match│
│ GIN             │ Full-text search, JSONB, arrays                      │
│ GiST            │ Geospatial data, nearest-neighbor                    │
└─────────────────┴──────────────────────────────────────────────────────┘
```

---

## 4. Normalization vs Denormalization

### Normalization (Start Here)

```
Store each piece of data in exactly ONE place.
Prevents update anomalies.

NORMALIZED:
┌──────────────────────────────────────────────────────────────────┐
│  users: id, username, email                                      │
│  posts: id, user_id (FK), content, created_at                    │
└──────────────────────────────────────────────────────────────────┘

To get post with username: JOIN users ON posts.user_id = users.id

Benefits:
  ✓ Single source of truth
  ✓ Update username in ONE place
  ✓ No inconsistent data
  ✓ Less storage
```

### Denormalization (Only When Needed)

```
Duplicate data for read performance.

DENORMALIZED:
┌──────────────────────────────────────────────────────────────────┐
│  posts: id, user_id, username, email, content, created_at       │
│                      ↑        ↑                                  │
│                 Duplicated from users table                      │
└──────────────────────────────────────────────────────────────────┘

No JOIN needed, but...

Problems:
  ✗ User changes username → update EVERY post they made
  ✗ Miss one update → inconsistent data
  ✗ More storage
```

### When to Denormalize

```
✓ Analytics/reporting (infrequent updates, heavy reads)
✓ Event logs / audit trails (capturing point-in-time snapshot)
✓ Pre-computed aggregations (like_count on posts table)
✓ Search indexes (Elasticsearch gets denormalized copy)

Better alternative: Cache
  - Keep source of truth normalized (SQL)
  - Put denormalized view in cache (Redis)
  - Best of both worlds
```

### Example: Like Counts

```
Option 1: Always count (normalized, slow reads)
  SELECT COUNT(*) FROM likes WHERE post_id = 123
  → Counts every time, accurate but slow at scale

Option 2: Denormalized counter (fast reads, eventual consistency)
  posts: id, content, like_count
  → Increment like_count when someone likes
  → Fast read, but count might drift if updates fail

Option 3: Cache (best of both)
  - Source of truth: likes table
  - Cache: Redis INCR for like counts
  - Periodic sync to fix drift
```

---

## 5. Sharding (Partitioning)

### When You Need It

```
Single database can't handle:
  - Data too large to fit on one machine
  - Write volume exceeds single node capacity
  - Need geographic distribution

Solution: Split data across multiple databases.
```

### Choosing a Shard Key

```
┌─────────────────────────────────────────────────────────────────────────┐
│  SHARD BY PRIMARY ACCESS PATTERN                                        │
│                                                                         │
│  Query: "Get all posts by user X"                                       │
│  Shard key: user_id                                                     │
│  Result: All of user's posts on same shard → fast                       │
│                                                                         │
│       Shard 0              Shard 1              Shard 2                 │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐             │
│  │ user_id 1-1M │     │ user_id 1M-2M│     │ user_id 2M-3M│             │
│  │ + their posts│     │ + their posts│     │ + their posts│             │
│  └──────────────┘     └──────────────┘     └──────────────┘             │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### Avoid Cross-Shard Queries

```
Bad: "Get posts from all users I follow"
  - Followers on different shards
  - Must query multiple shards + merge
  - Slow, complex, expensive

Solutions:
  1. Denormalize: Store feed per user (fan-out on write)
  2. Different shard key: Shard by time for timeline queries
  3. Hybrid: Hot data in one place, archive sharded differently
```

### Common Shard Key Patterns

```
┌─────────────────┬──────────────────────────────────────────────────────┐
│ Shard Key       │ Good For                                             │
├─────────────────┼──────────────────────────────────────────────────────┤
│ user_id         │ User-centric apps (social media, e-commerce)         │
│ tenant_id       │ Multi-tenant SaaS                                    │
│ time (date)     │ Time-series, logs, events                            │
│ geo (region)    │ Location-based apps with data locality               │
│ hash(id)        │ Even distribution when no natural key                │
└─────────────────┴──────────────────────────────────────────────────────┘

Warning: Shard key choice is often permanent. Think carefully.
```

---

## 6. Schema Examples

### Twitter-like App

```
┌─────────────────────────────────────────────────────────────────────────┐
│  users                                                                  │
│  ──────                                                                 │
│  id (PK)                                                                │
│  username (UNIQUE, INDEX)                                               │
│  email (UNIQUE)                                                         │
│  display_name                                                           │
│  bio                                                                    │
│  created_at                                                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  tweets                                                                 │
│  ──────                                                                 │
│  id (PK)                                                                │
│  user_id (FK → users.id, INDEX)                                         │
│  content                                                                │
│  created_at (INDEX)                                                     │
│  like_count (denormalized)                                              │
│  retweet_count (denormalized)                                           │
│  INDEX (user_id, created_at)  ← user's recent tweets                    │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  follows                                                                │
│  ───────                                                                │
│  follower_id (FK → users.id)                                            │
│  followee_id (FK → users.id)                                            │
│  created_at                                                             │
│  PRIMARY KEY (follower_id, followee_id)                                 │
│  INDEX (followee_id)  ← "who follows me"                                │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  likes                                                                  │
│  ─────                                                                  │
│  user_id (FK → users.id)                                                │
│  tweet_id (FK → tweets.id)                                              │
│  created_at                                                             │
│  PRIMARY KEY (user_id, tweet_id)                                        │
│  INDEX (tweet_id)  ← "who liked this tweet"                             │
└─────────────────────────────────────────────────────────────────────────┘
```

### E-commerce App

```
┌─────────────────────────────────────────────────────────────────────────┐
│  users                                                                  │
│  ──────                                                                 │
│  id (PK)                                                                │
│  email (UNIQUE)                                                         │
│  password_hash                                                          │
│  created_at                                                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  products                                                               │
│  ────────                                                               │
│  id (PK)                                                                │
│  name                                                                   │
│  description                                                            │
│  price (CHECK price > 0)                                                │
│  inventory_count                                                        │
│  category_id (FK, INDEX)                                                │
│  created_at                                                             │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  orders                                                                 │
│  ──────                                                                 │
│  id (PK)                                                                │
│  user_id (FK → users.id, INDEX)                                         │
│  status (pending, paid, shipped, delivered)                             │
│  total_amount                                                           │
│  created_at (INDEX)                                                     │
│  INDEX (user_id, created_at)                                            │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  order_items                                                            │
│  ───────────                                                            │
│  id (PK)                                                                │
│  order_id (FK → orders.id, INDEX)                                       │
│  product_id (FK → products.id)                                          │
│  quantity                                                               │
│  unit_price (snapshot at time of order)                                 │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│  payments                                                               │
│  ────────                                                               │
│  id (PK)                                                                │
│  order_id (FK → orders.id, UNIQUE)  ← one payment per order             │
│  amount                                                                 │
│  status (pending, completed, failed)                                    │
│  payment_method                                                         │
│  idempotency_key (UNIQUE)  ← prevent double charges                     │
│  created_at                                                             │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 7. Interview Checklist

### Data Modeling Steps

```
□ 1. Choose database type (default: PostgreSQL)
□ 2. List core entities from requirements
□ 3. Define primary key for each entity
□ 4. Map relationships (1:N, N:M) with foreign keys
□ 5. Add constraints (NOT NULL, UNIQUE, CHECK)
□ 6. Identify indexes based on query patterns
□ 7. Consider denormalization only if needed
□ 8. Discuss sharding if data is massive
```

### Common Pitfalls

```
✗ Choosing exotic DB to show off (stick to PostgreSQL)
✗ Over-normalizing (sometimes denormalization is right)
✗ Under-indexing (every WHERE/ORDER BY/JOIN column needs index)
✗ Cross-shard queries (shard by access pattern)
✗ Forgetting idempotency for payments/orders
✗ Spending too much time (5-10 min max on schema)
```

### Key Factors to Consider

```
1. DATA VOLUME
   - How much data? Fits on one machine?
   - Determines sharding strategy

2. ACCESS PATTERNS (most important!)
   - How will data be queried?
   - Drives index and shard key choices

3. CONSISTENCY REQUIREMENTS
   - Strong (payments, inventory) → SQL with ACID
   - Eventual (likes, views) → can denormalize/cache
```

### Interview Phrasing

```
Good:
  "Since we query posts by user frequently, I'll add an index on user_id"
  "Payments need strong consistency, so I'll keep them in PostgreSQL"
  "Like counts can be eventually consistent, so I'll cache in Redis"

Shows you're reasoning from requirements, not memorizing patterns.
```
