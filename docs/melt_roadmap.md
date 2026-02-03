# MobiusDB MELT development roadmap

## Phase 1: Foundation (MVP)
- Define the MELT envelope schema and ingestion API.
- Implement append-only segments with per-segment metadata.
- Build time-range pruning and basic tag filtering.
- Add durability via WAL and crash recovery.

## Phase 2: Signal specialization
- Metrics: implement rollup queries and basic downsampling.
- Logs: implement token index + field filters for fast search.
- Traces: add trace ID index + span reconstruction.
- Events: schema-flexible filtering with JSON key extraction.

## Phase 3: Compaction & retention
- Implement tiered compaction and index rebuilding.
- Add configurable retention policies by tenant and signal type.
- Introduce storage tiers (hot/warm/cold) and export hooks.

## Phase 4: Correlation & analytics
- Build a shared entity index across signals (trace IDs, service names, hosts).
- Add cross-signal query primitives (e.g., logs for a trace, metrics around an event).
- Introduce anomaly detection hooks or alerting integrations.

## Phase 5: Performance & operability
- Benchmark ingestion/query throughput and optimize hot paths.
- Improve compression ratios with adaptive dictionaries.
- Add operator tooling (segment inspection, repair, reindex).
