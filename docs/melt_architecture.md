# MobiusDB MELT-first architecture proposal

## Ingestion & write path
- **Append-only segment store:** immutable segments per time window with write-ahead logs for durability.
- **Multi-signal ingest API:** common envelope containing timestamp, signal type, tenant, service, and payload.
- **Out-of-order buffer:** short-term ingest buffer to re-order within a configurable lateness window.
- **Routing by time + signal:** partition by (signal type, time bucket, tenant) to isolate compaction and queries.

## Storage layout
- **Segmented log files:** compacted segments with per-segment metadata (min/max time, signal type, tag dictionary).
- **Dictionary + columnar encoding:** for structured fields (tags, JSON keys) to reduce size and improve filtering.
- **Value blobs for logs/events:** keep raw message payloads alongside parsed fields.
- **Index sidecars:** per-segment indexes for time, trace ID, and full-text tokens.

## Query layer
- **Time-range pruning:** coarse indexes to skip segments outside query window.
- **Signal-specific query paths:**
  - Metrics: rollups, downsampling, tag filters.
  - Logs: text search + field filtering.
  - Traces: trace ID lookup and span graph reconstruction.
  - Events: schema-flexible filters and joins with deployments/incidents.
- **Cross-signal correlation:** build a shared entity index (service, host, trace ID) to join across signals.

## Compaction & retention
- **Tiered compaction:** merge older segments with dictionary deduplication and tombstone cleanup.
- **Downsampling pipelines:** configurable aggregation tiers for metrics and event rollups.
- **Retention policies:** per-tenant and per-signal retention, with cold storage export options.

## Operations & scalability
- **Single-host focus:** preserve simple operational footprint with bounded complexity.
- **CPU/IO-aware scheduling:** compaction and indexing should be backpressure-aware.
- **Observability of the store:** internal metrics, log sampling, and trace hooks to dogfood MELT.
