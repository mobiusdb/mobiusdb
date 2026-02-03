# Bitcask research notes

## What Bitcask is
- **Design goal:** a simple, high-throughput key-value store optimized for fast writes and reads on spinning disks or SSDs.
- **Core idea:** append-only log files for all writes. Each write is stored as a new record; updates are not in-place.
- **In-memory keydir:** an in-memory hash map ("keydir") maps each key to its latest value location (file ID, offset, size, timestamp). This enables O(1) lookups after a single disk seek.
- **Log-structured storage:** the log is segmented into data files. Old data files become immutable once they reach a size threshold.
- **Compaction ("merge") process:** periodically scans older data files, keeps only the latest entry per key, and rewrites them into new compacted files. This reclaims space and removes obsolete versions (tombstones or overwritten values).
- **Crash recovery:** on restart, the keydir can be rebuilt by scanning data files (or loaded from a hint file if present) so the store can recover without a full index on disk.

## Strengths
- **High write throughput:** append-only writes avoid random I/O and are friendly to disks and SSDs.
- **Fast reads:** a single disk seek per read (find location via keydir → fetch value).
- **Simplicity:** a small, consistent storage model (log-structured files + in-memory index).

## Limitations / trade-offs
- **Memory-bound keydir:** the in-memory index must hold every key, so the data set size is limited by available RAM for keys.
- **No range scans:** the hash index does not support ordered traversal; range queries are inefficient.
- **Compaction cost:** merge operations can be I/O intensive; requires careful scheduling to avoid write stalls.
- **Large values:** value size inflation can increase compaction overhead and disk usage.

## Where Bitcask fits well
- **Write-heavy workloads** where most accesses are point lookups and updates.
- **Workloads with predictable key sizes** so the keydir memory footprint is manageable.
- **Simple KV stores** without heavy query requirements.

## Common Bitcask-style implementations
- **Riak Bitcask:** the original implementation in Erlang.
- **Other KV stores** that use log-structured hash tables or similar append-only designs.

## Alternatives to Bitcask

### 1) LSM-tree (Log-Structured Merge Tree)
- **Examples:** LevelDB, RocksDB, Pebble.
- **How it works:** writes go to a memtable and a WAL; flushed to sorted SSTables on disk. Compaction merges SSTables over time.
- **Trade-offs vs Bitcask:**
  - Better range scans and ordered iteration.
  - Higher read amplification without careful tuning.
  - Requires more complex compaction strategies, but scales beyond RAM-limited keydirs.

### 2) B-tree / B+tree stores
- **Examples:** Berkeley DB, SQLite (for indexes), LMDB.
- **How it works:** balanced trees on disk support ordered keys and efficient range queries.
- **Trade-offs vs Bitcask:**
  - Handles range scans well.
  - Random writes can be slower than append-only logs (though modern variants mitigate this).
  - More complex concurrency control for high write throughput.

### 3) Log-structured hash table variants
- **Examples:** FASTER (hybrid log), various academic LSH designs.
- **How it works:** retains the append-only log concept but optimizes indexing, caching, and compaction.
- **Trade-offs vs Bitcask:**
  - Often better performance at scale or with SSDs.
  - More complex implementation and tuning.

### 4) KV stores with in-memory index + on-disk log (Bitcask-like)
- **Examples:** Badger (LSM + value log), Redis AOF (append-only file for persistence).
- **How it works:** maintain a memory index, store values in a log, and periodically compact.
- **Trade-offs vs Bitcask:**
  - Similar strengths but often add richer features (transactions, MVCC, range scans).

## When to choose alternatives
- **Need range scans or ordered queries:** prefer B-tree/B+tree or LSM-tree.
- **Need to scale beyond RAM for keys:** prefer LSM-tree or a disk-resident index.
- **Need multi-tenant or analytical workloads:** prefer stores with richer query engines or columnar designs.
