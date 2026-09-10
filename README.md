# PageVault

A virtual memory management simulator demonstrating page replacement algorithms — FIFO, LRU, Optimal, and Clock — as interchangeable Strategy implementations, with Observer-based fault tracking and statistics. Built with clean OOAD in Java, deliberately distinct in scope from a scheduling-focused OS project (kthread): this one is entirely about memory management.

## What this is

When a process references more virtual memory than fits in physical frames, something has to be evicted to make room — *which* page gets evicted is the entire subject of this project. PageVault implements four classic replacement algorithms as swappable `Strategy` objects behind a single interface, so the memory manager itself never knows or cares which algorithm is deciding evictions. Page fault events are broadcast via `Observer`, letting a fault logger and a statistics collector react independently without the core engine knowing they exist.

## Design patterns

- **Strategy** (`PageReplacementStrategy`) — FIFO, LRU, Optimal, and Clock are fully interchangeable; swapping the algorithm a `MemoryManager` uses is a one-line constructor change, not a rewrite.
- **Observer** (`PageFaultObserver`) — `FaultLogger` (full event history) and `StatisticsCollector` (aggregate counts) both listen to the same fault stream independently.

## The four algorithms

| Algorithm | Data structure | Real-world relevance |
|---|---|---|
| **FIFO** | Queue | Simple, cheap, but ignores recency entirely — can evict a heavily-used page just because it loaded first |
| **LRU** | Timestamp comparison (O(n) scan here; a doubly-linked-list + hashmap gives O(1) in production) | Exploits recency directly — evicts whatever hasn't been touched in the longest time |
| **Optimal (Belady's)** | Full-sequence lookahead | Theoretically ideal — evicts whatever won't be needed for the longest time (or never again). Impossible for a real OS to implement (requires knowing the future), used here purely as a best-case baseline |
| **Clock** | Circular buffer + reference bits | Approximates LRU's behavior at far lower bookkeeping cost — the algorithm real operating systems actually favor, since true per-access LRU tracking is expensive at OS scale |

## Core components

| Layer | What it does |
|---|---|
| `core/` | `Page`, `Frame`, `MemoryReference`, `PageFaultEvent` — the domain model |
| `algorithms/` | The four `PageReplacementStrategy` implementations |
| `observer/` | `FaultLogger`, `StatisticsCollector` |
| `engine/` | `MemoryManager` — orchestrates references, faults, and eviction, algorithm-agnostic |
| `io/` | `ReferenceStringGenerator` — produces random, sequential, and locality-of-reference workloads |

## Tech stack

- Java 17, Maven
- JUnit 5
- GitHub Actions CI

## Project structure

```text
pagevault/
├── src/main/java/com/pagevault/
│ ├── core/
│ ├── algorithms/
│ ├── observer/
│ ├── engine/
│ ├── io/
│ └── Main.java
└── src/test/java/com/pagevault/
├── algorithms/
└── engine/
```


## Running it

```bash
mvn clean compile   # build
mvn test             # run the test suite (13 tests)
mvn exec:java         # run the demo comparison across three workloads
```

## Demo results and what they actually show

`Main.java` runs all four algorithms against three distinct workloads (4 physical frames, 20-page address space):

**Random access** (no exploitable pattern): all causal algorithms perform similarly (~23-25% hit ratio), since there's no locality for LRU/Clock to exploit over FIFO. Optimal nearly doubles the hit ratio (46%), showing the real cost of not knowing the future.

**Sequential access**: every causal algorithm hits 0% — this is the classic "sequential flooding" worst case for any recency-based cache, well-documented in OS literature. Even Optimal only achieves 15%, since a strictly increasing, rarely-repeating sequence offers little to exploit even with perfect foresight.

**Locality-of-reference (bursty, working set of 6 pages in 4 frames)**: LRU shows a real, measurable edge over FIFO and Clock (81 vs. 83 vs. 85 faults out of 200) — a modest but genuine margin, reflecting that this working set only moderately exceeds available frames. Optimal's clear lead here (62 faults) demonstrates there's meaningful headroom no causal algorithm can close, precisely because no real algorithm can see the future.

**The honest takeaway**: algorithm choice matters *in proportion to* how much memory pressure actually exists. Under light contention or workloads with no locality, the choice barely matters. Under sustained, moderate-to-heavy locality-based pressure — which is what real running programs typically look like — LRU/Clock's recency-tracking earns its keep over FIFO's naive ordering.

## Test coverage

13 tests across all four algorithms and the memory manager's core hit/fault/eviction logic — all passing.

```bash
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```


## License

MIT
