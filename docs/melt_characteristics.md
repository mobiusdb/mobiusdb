# MELT data characteristics (deep research summary)

## Metrics
- **High cardinality dimensions:** labels/tags (host, service, region, tenant, build) can explode cardinality when users add custom tags.
- **Regular cadence:** data typically arrives at fixed intervals (seconds to minutes), which enables predictable segmenting and compaction windows.
- **Aggregation-friendly:** queries often roll up by time windows and tag sets (p50/p95, avg, sum, rate).
- **Downsampling pressure:** long retention often requires multiple resolutions (raw → 1m → 1h) to keep storage and query costs bounded.

## Events
- **Sparse but bursty:** events are irregular and often arrive in bursts during deployments, incidents, or user actions.
- **Schema-flexible payloads:** events frequently include semi-structured data with optional or evolving fields.
- **Correlation-centric:** events are frequently joined with metrics/logs/traces via entity IDs, service names, or deployment markers.

## Logs
- **High volume and variance:** logs can dwarf other signals during outages, with high variance in rate and message size.
- **Unstructured to semi-structured:** message bodies may be plain text, JSON, or key-value formats; parsing is often late-bound.
- **Search-first access:** queries rely on time range + full-text / regex / field filters.
- **Retention tiers:** hot vs warm vs cold storage is common due to volume.

## Traces
- **Graph-shaped data:** traces are trees/graphs of spans with parent-child relationships.
- **High fan-out:** a single trace can include many spans across services.
- **Latency-sensitive queries:** trace reconstruction requires ordered span retrieval by trace ID with low latency.
- **Sampling effects:** traces may be sampled, causing gaps; storage needs to mark sampling decisions and completeness.

## Shared characteristics across MELT
- **Time-centric ingestion:** all signals are time-stamped and usually queried by time ranges.
- **Append-only workload:** new data arrives continuously; updates are rare and usually corrections or late arrivals.
- **Late data tolerance:** out-of-order arrivals are common; ingest must handle skew and reordering.
- **Compression opportunities:** repeated labels, keys, and message prefixes provide strong compression potential.
- **Entity correlations:** linking signals across shared identifiers is critical for observability use cases.
