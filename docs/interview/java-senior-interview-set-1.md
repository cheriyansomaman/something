# Senior Java Developer Interview — Set 1 (10+ Years Experience)

Each question includes **answer hints** — the key points a strong senior candidate should cover.

---

## 1. Core Java

**Q1. Explain the Java Memory Model (JMM). Why do `volatile` and `synchronized` exist if we have atomic classes?**
> **Hints:** Happens-before relationships; visibility vs. atomicity vs. ordering; `volatile` guarantees visibility + ordering but not compound atomicity (e.g., `count++` is unsafe); `synchronized` gives mutual exclusion + visibility; atomics use CAS (lock-free) — good for counters, but CAS can livelock under high contention; instruction reordering by JIT/CPU and how JMM constrains it.

**Q2. How does `HashMap` work internally in Java 8+? What changed from Java 7?**
> **Hints:** Array of buckets, `hashCode()` spread via `hash ^ (hash >>> 16)`, index = `(n-1) & hash`; collision handling by linked list, converting to red-black tree when a bin exceeds 8 entries (and table ≥ 64); treeify threshold reverts at 6; resize doubling and rehash split (`lo`/`hi` lists); Java 7 head-insertion could cause infinite loop under concurrent modification, Java 8 tail insertion; why keys should be immutable.

**Q3. What are the contracts of `equals()` and `hashCode()`? What breaks if you violate them?**
> **Hints:** Reflexive, symmetric, transitive, consistent, non-null; equal objects must have equal hash codes (not vice versa); violating it breaks `HashMap`/`HashSet` lookups (object "lost" in wrong bucket); mutating a key after insertion; pitfalls with inheritance and symmetry (`instanceof` vs `getClass()`); records generate these correctly.

**Q4. Explain strong, soft, weak, and phantom references. Where would you actually use each?**
> **Hints:** Strong = default; soft = memory-sensitive caches (cleared before OOME); weak = canonical maps / `WeakHashMap`, listeners to avoid leaks; phantom = post-mortem cleanup with `ReferenceQueue`, replacement for `finalize()`; also mention `Cleaner` (Java 9+) and why `finalize()` is deprecated.

**Q5. What's new and significant in Java 11 → 21 that you've used in production?**
> **Hints:** `var`, HTTP Client (11); records, sealed classes, pattern matching for `instanceof`/`switch` (14–21); text blocks; virtual threads (21, Project Loom) — cheap blocking, thread-per-request revival; ZGC/Shenandoah low-pause collectors; `Optional` improvements; candidate should tie features to real use cases, not just list them.

**Q6. How does class loading work? Explain a `NoClassDefFoundError` vs `ClassNotFoundException` you've debugged.**
> **Hints:** Bootstrap → platform → application loaders, parent delegation; `ClassNotFoundException` = checked, thrown on explicit load (`Class.forName`); `NoClassDefFoundError` = class was present at compile time but missing/failed at runtime (often a static initializer failure — look for `ExceptionInInitializerError` first occurrence); classloader leaks in app servers; fat-jar shading conflicts.

**Q7. Explain generics type erasure and its practical consequences.**
> **Hints:** Type parameters erased to bounds at runtime; no `new T()`, no `T.class`, no overloading on `List<String>` vs `List<Integer>`; bridge methods; heap pollution and `@SafeVarargs`; wildcard PECS rule (Producer-Extends, Consumer-Super); how frameworks recover types via `TypeReference`/reflection on fields.

---

## 2. Collections & Data Structures

**Q1. Compare `ArrayList` vs `LinkedList` — when is `LinkedList` actually faster?**
> **Hints:** Almost never in practice: cache locality dominates; `ArrayList` O(1) amortized append, O(n) middle insert but memcpy is fast; `LinkedList` O(1) insert only if you already hold the node (iterator); `ArrayDeque` beats `LinkedList` for queues/stacks.

**Q2. How does `ConcurrentHashMap` achieve thread safety without locking the whole map?**
> **Hints:** Java 8: CAS on empty bins, `synchronized` on the first node of a bin (per-bin locking), no segments anymore; `size()` via `CounterCell` striping; weakly consistent iterators; `compute`/`merge` atomicity per key; never lock inside `compute` on another key (deadlock risk); why `null` keys/values are forbidden.

**Q3. Design choice: `TreeMap` vs `HashMap` vs `LinkedHashMap` — give production use cases for each.**
> **Hints:** `TreeMap`: sorted iteration, range queries (`headMap`, `ceilingKey`) — e.g., time-bucketed data, rate limiter windows; `LinkedHashMap`: insertion/access order — LRU cache via `removeEldestEntry`; `HashMap`: default O(1); complexity comparison O(log n) vs O(1).

**Q4. What are fail-fast vs fail-safe iterators?**
> **Hints:** `modCount` check → `ConcurrentModificationException` (best-effort, not guaranteed); `CopyOnWriteArrayList` snapshot iteration — safe but stale, write cost O(n), good for read-mostly listener lists; `ConcurrentHashMap` weakly consistent.

**Q5. How would you implement an LRU cache? What does a production-grade one (Caffeine) do differently?**
> **Hints:** `LinkedHashMap` with access order + `removeEldestEntry`, or HashMap + doubly linked list for O(1); thread safety concerns; Caffeine: Window-TinyLFU admission policy (frequency sketch), async eviction, expiry wheels — better hit rates than plain LRU under scan patterns.

---

## 3. Streams & Functional Java

**Q1. Explain lazy evaluation in streams. When does a stream actually do work?**
> **Hints:** Intermediate ops (map/filter) build a pipeline, terminal ops (collect/forEach/reduce) trigger execution; short-circuiting (`findFirst`, `limit`, `anyMatch`); one-pass fusion — elements flow through the whole pipeline one at a time, not stage by stage; streams are single-use.

**Q2. `map` vs `flatMap` — explain with a real example.**
> **Hints:** `map`: 1→1 transform; `flatMap`: 1→N flattening (`List<Order>` → stream of all `LineItem`s); `Optional.flatMap` for chaining optional-returning methods; `flatMap` for nested collections and for merging streams.

**Q3. When are parallel streams a bad idea?**
> **Hints:** Shared common ForkJoinPool — blocking I/O in a parallel stream starves everything else in the JVM; small datasets (splitting overhead > gains); ordered/stateful ops (`limit`, `sorted`) reduce parallelism; non-associative reductions give wrong results; side effects; sizing: N × Q model (elements × cost per element); prefer explicit executor or virtual threads for I/O.

**Q4. Write/explain a `Collectors.groupingBy` with downstream collectors.**
> **Hints:** `groupingBy(Order::getCustomer, mapping(Order::getTotal, reducing(BigDecimal.ZERO, BigDecimal::add)))`; downstream: `counting()`, `mapping()`, `toMap` vs `groupingBy`; `toMap` merge function to handle duplicate keys; `partitioningBy` for boolean split.

**Q5. What is the difference between `reduce` and `collect`? Why does `collect` exist?**
> **Hints:** `reduce` = immutable accumulation (creates new value each step — O(n²) for string concat); `collect` = mutable reduction (supplier/accumulator/combiner), efficient for containers; combiner needed for parallel; `Collector` characteristics (CONCURRENT, UNORDERED, IDENTITY_FINISH).

**Q6. Checked exceptions inside lambdas — how do you handle them cleanly?**
> **Hints:** Functional interfaces don't declare checked exceptions; options: wrap in unchecked, helper `ThrowingFunction` wrapper, extract to method with try/catch, or collect into `Either`/result objects; discuss sneaky-throws trade-offs.

---

## 4. Concurrency & Multithreading

**Q1. Explain the executor framework. How do you size a thread pool?**
> **Hints:** `ThreadPoolExecutor` anatomy: core/max size, queue, rejection policy — and the gotcha that max size only kicks in when the queue is *full* (unbounded queue → max never used); CPU-bound: ~cores; I/O-bound: cores × (1 + wait/compute); Little's Law; bounded queues + `CallerRunsPolicy` for backpressure; virtual threads change the calculus for I/O-bound work.

**Q2. What is `CompletableFuture`? Compare `thenApply` vs `thenCompose` vs `thenCombine`.**
> **Hints:** `thenApply` = map; `thenCompose` = flatMap (avoids `CompletableFuture<CompletableFuture<T>>`); `thenCombine` = zip two independent futures; `*Async` variants and which executor runs the continuation (common FJP default — danger for blocking work); exception handling: `exceptionally`, `handle`, `whenComplete`; timeouts with `orTimeout`/`completeOnTimeout` (9+).

**Q3. Describe a deadlock you've encountered. How do you detect and prevent deadlocks?**
> **Hints:** Four Coffman conditions; lock ordering as prevention; `tryLock` with timeout; detection: thread dump (`jstack`) shows "Found one Java-level deadlock", JMX `ThreadMXBean.findDeadlockedThreads`; also discuss livelock and lock convoys; real-world: DB deadlocks, nested `synchronized` on beans.

**Q4. Explain virtual threads (Java 21). What changes and what doesn't?**
> **Hints:** M:N scheduling on carrier threads; blocking is cheap — park/unpark instead of OS block; thread-per-request without pools; pinning issues with `synchronized` blocks (pre-24) and native calls; don't pool virtual threads; `ThreadLocal` still works but scoped values preferred; doesn't speed up CPU-bound work; structured concurrency (`StructuredTaskScope`).

**Q5. What is a race condition vs a data race? Give an example of check-then-act.**
> **Hints:** Data race = unsynchronized conflicting access (JMM concept); race condition = correctness depends on timing (can occur even with atomic ops); check-then-act: `if (!map.containsKey(k)) map.put(k, v)` → use `putIfAbsent`/`computeIfAbsent`; lazy init → holder idiom or `enum` singleton; double-checked locking needs `volatile`.

**Q6. How do `CountDownLatch`, `CyclicBarrier`, `Semaphore`, and `Phaser` differ?**
> **Hints:** Latch: one-shot, threads wait for N events (service startup gating); barrier: reusable, threads wait for each other (iterative parallel algorithms); semaphore: permits, resource limiting/rate limiting; phaser: dynamic party registration, multi-phase; real use cases for each.

---

## 5. JVM Internals, GC & Performance

**Q1. Walk through JVM memory areas. What lives where?**
> **Hints:** Heap (young: eden + survivors, old gen), Metaspace (class metadata, native memory since 8), thread stacks, code cache (JIT), direct buffers; String pool in heap; common OOM flavors: heap, Metaspace, direct memory, "unable to create native thread".

**Q2. Compare G1, ZGC, and Parallel GC. How do you choose?**
> **Hints:** Parallel: max throughput, tolerates long pauses (batch); G1: default, region-based, pause-target driven (`MaxGCPauseMillis`), mixed collections; ZGC: sub-millisecond pauses, colored pointers + load barriers, concurrent everything, generational ZGC in 21; choose by latency SLO vs throughput vs heap size; observability: GC logs (`-Xlog:gc*`), allocation rate, promotion rate.

**Q3. A service's p99 latency spikes every few minutes. How do you investigate?**
> **Hints:** Correlate with GC logs (stop-the-world pauses, old-gen collections); safepoint statistics; JIT deoptimization storms; check allocation rate → premature promotion; tools: JFR (Java Flight Recorder), async-profiler flame graphs; also non-JVM causes — connection pool exhaustion, noisy neighbor, cron jobs.

**Q4. What is JIT compilation? What are C1/C2, tiered compilation, and deoptimization?**
> **Hints:** Interpreter → C1 (client, fast compile) → C2 (server, aggressive opt) tiers; profile-guided: inlining, escape analysis (scalar replacement — objects that never hit the heap), lock elision, devirtualization; deopt when speculation fails (new class loaded breaks monomorphic call site); warmup implications for benchmarks — JMH.

**Q5. How do you diagnose a memory leak in production?**
> **Hints:** Symptoms: old gen sawtooth trending up, full GCs reclaiming less each time; capture heap dump (`jmap`, `-XX:+HeapDumpOnOutOfMemoryError`), analyze with Eclipse MAT (dominator tree, path to GC roots); usual suspects: static collections, unbounded caches, `ThreadLocal` in pools, listener registration, classloader leaks on redeploy; JFR old-object sampling for low overhead.

---

## 6. Spring Boot & Spring Framework

**Q1. How does Spring Boot auto-configuration actually work?**
> **Hints:** `@EnableAutoConfiguration` → `AutoConfiguration.imports` (formerly `spring.factories`); `@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`; user beans win via `@ConditionalOnMissingBean`; ordering (`@AutoConfigureAfter`); debugging with `--debug` condition evaluation report; creating your own starter.

**Q2. Explain bean scopes and the pitfall of injecting a prototype into a singleton.**
> **Hints:** Singleton (default), prototype, request/session; prototype injected once at singleton creation — fix with `ObjectProvider`, `@Lookup`, or scoped proxy; also thread-safety implications of singletons holding state.

**Q3. How does `@Transactional` work under the hood? List the classic ways it silently fails.**
> **Hints:** AOP proxy (JDK dynamic or CGLIB) wraps the bean; failures: self-invocation (call bypasses proxy), non-public methods, checked exceptions don't roll back by default (`rollbackFor`), `@Transactional` on the wrong layer, catching the exception yourself, calling from constructor; propagation levels (`REQUIRED`, `REQUIRES_NEW`, `NESTED`) and isolation; transaction + `@Async` = different thread, no tx.

**Q4. Constructor vs field injection — why does the community insist on constructor injection?**
> **Hints:** Immutability/`final` fields, fails fast on missing deps, testable without Spring, makes bloated classes visible (too many params = smell), no reflection hacks; `@RequiredArgsConstructor` with Lombok; circular dependency detection at startup.

**Q5. How do you handle configuration across environments in Spring Boot?**
> **Hints:** Profiles (`application-{profile}.yml`), property precedence order (command line > env vars > profile files > defaults), `@ConfigurationProperties` (typed, validated with `@Validated`) vs `@Value`; secrets: never in git — Vault/AWS Secrets Manager/K8s secrets; Spring Cloud Config; `spring.config.import`.

**Q6. What's the difference between Spring MVC and Spring WebFlux? When would you actually pick WebFlux?**
> **Hints:** Servlet thread-per-request vs event-loop (Netty, Reactor); WebFlux for high-concurrency I/O-bound streaming (SSE, proxying); costs: harder debugging, blocking calls poison the event loop, ecosystem (JDBC is blocking → R2DBC); with virtual threads, MVC covers most scalability needs now — a nuanced senior answer acknowledges this.

**Q7. How do you make a Spring Boot service production-ready?**
> **Hints:** Actuator (health with liveness/readiness groups, metrics, info), Micrometer → Prometheus, structured logging with correlation IDs, graceful shutdown (`server.shutdown=graceful`), connection pool tuning (Hikari), timeouts on all outbound calls, `@ControllerAdvice` global error handling, API docs (springdoc), container image via buildpacks/jib, JVM flags for containers.

---

## 7. Microservices & Distributed Systems

**Q1. How do you decide service boundaries? What goes wrong when you get them wrong?**
> **Hints:** DDD bounded contexts, business capability alignment, team topology (Conway's law); wrong boundaries → distributed monolith: chatty synchronous chains, lockstep deployments, shared databases; start with a modular monolith and extract; "don't share databases" rule.

**Q2. Explain the saga pattern. Choreography vs orchestration?**
> **Hints:** Long-running distributed transaction as sequence of local transactions + compensating actions; choreography: events, loose coupling, but flow is implicit and hard to trace; orchestration: central coordinator (e.g., Temporal, Camunda, or hand-rolled state machine), explicit flow, single point to monitor; compensation is not rollback (semantic undo); idempotency essential.

**Q3. What is a circuit breaker and how does it differ from a retry? How do they interact badly?**
> **Hints:** CB states: closed → open (failure threshold) → half-open probe; prevents hammering a dying dependency, fails fast; retries amplify load (retry storms) — always pair with exponential backoff + jitter and a retry budget; retries only for idempotent operations; Resilience4j; bulkheads to isolate pools; timeout hierarchy (caller timeout > sum of callee budget).

**Q4. How do you version and evolve APIs/events without breaking consumers?**
> **Hints:** Additive-only changes (tolerant reader), never repurpose fields; URL vs header versioning trade-offs; consumer-driven contract tests (Pact); for events: schema registry with compatibility rules (backward/forward); expand-contract (parallel change) pattern for breaking changes; deprecation policy and sunset headers.

**Q5. Explain distributed tracing. How does context propagation work?**
> **Hints:** Trace ID + span ID propagated via headers (W3C `traceparent`, B3); OpenTelemetry SDK/agent, sampling strategies (head vs tail); spans exported to Jaeger/Tempo/Zipkin; propagation across async boundaries and Kafka (headers); logs enriched with trace ID for pivoting; what tracing shows that metrics don't (critical path of one slow request).

**Q6. What is idempotency and how do you implement it for a payment endpoint?**
> **Hints:** Same request N times = same effect once; client-supplied idempotency key; server stores key → result (with TTL) in a store with atomic insert (unique constraint / `SETNX`); return stored response on replay; scope keys per operation; at-least-once delivery everywhere makes this mandatory; distinguish idempotency from deduplication windows.

---

## 8. REST API Design

**Q1. What makes an API RESTful beyond "JSON over HTTP"? Where do you pragmatically deviate?**
> **Hints:** Resources + uniform interface, statelessness, cacheability, HATEOAS (rarely used in practice — say so); Richardson maturity model; pragmatic deviations: RPC-style actions (`/orders/123/cancel`), search endpoints with POST for large queries; consistency matters more than purity.

**Q2. Design pagination for a large collection endpoint. Offset vs cursor?**
> **Hints:** Offset/limit: simple, but O(n) skip cost and page drift under concurrent writes; cursor/keyset: `WHERE (created_at, id) < (?, ?) ORDER BY created_at DESC, id DESC LIMIT n` — stable, index-friendly, no random access; opaque cursor tokens; return `next` links; total counts are expensive — make them optional.

**Q3. Explain HTTP status code choices: 400 vs 422, 401 vs 403, 409, 429.**
> **Hints:** 400 malformed syntax vs 422 semantically invalid; 401 unauthenticated (who are you) vs 403 unauthorized (you can't do that); 409 state conflict (duplicate, version mismatch); 429 with `Retry-After`; 502/503/504 distinctions for gateways; error body standard: RFC 7807 / RFC 9457 problem+json.

**Q4. How do you secure a REST API?**
> **Hints:** OAuth2/OIDC, JWT validation (signature, `exp`, `aud`, `iss`), short-lived access + refresh tokens; scopes vs fine-grained authz (policy engine); mTLS for service-to-service; rate limiting per client; input validation, output encoding; never leak internals in errors; OWASP API Top 10 (BOLA #1 — object-level authorization checks).

**Q5. How do you handle optimistic concurrency over HTTP?**
> **Hints:** `ETag` + `If-Match` → 412 Precondition Failed on mismatch; maps to a version column in DB; alternative `If-Unmodified-Since`; when to prefer optimistic (low contention) vs pessimistic locking; lost-update problem demonstration.

---

## 9. Kafka & Messaging

**Q1. Explain Kafka's core architecture: topics, partitions, consumer groups, offsets.**
> **Hints:** Partition = ordered append-only log, unit of parallelism; ordering guaranteed only within a partition (key → partition via hash); consumer group: each partition owned by exactly one consumer in the group; offsets committed to `__consumer_offsets`; more consumers than partitions = idle consumers; replication: leader/followers, ISR, `acks=all` + `min.insync.replicas=2` for durability.

**Q2. How do you achieve exactly-once semantics — and what does it really mean?**
> **Hints:** Idempotent producer (`enable.idempotence`, sequence numbers dedupe broker-side); transactions (`transactional.id`, atomic produce+offset commit) — EOS within Kafka Streams read-process-write; to external systems it's effectively at-least-once + idempotent consumer (or transactional outbox); "exactly-once delivery" to arbitrary sinks is a myth — exactly-once *processing effect* is achievable.

**Q3. What is consumer rebalancing? Why is it disruptive and how do you mitigate it?**
> **Hints:** Triggers: member join/leave/crash (missed heartbeats), subscription/partition changes; stop-the-world with eager protocol vs incremental cooperative rebalancing (`CooperativeStickyAssignor`); static membership (`group.instance.id`) for rolling restarts; `max.poll.interval.ms` exceeded during slow processing → spurious rebalance loops; tune poll batch size or move work off the poll thread carefully.

**Q4. How do you handle poison messages and retries in Kafka consumers?**
> **Hints:** Can't skip-and-continue naively without losing the message; patterns: retry topics with backoff tiers (`orders-retry-5m`, `orders-retry-1h`), dead letter topic with original headers (topic/partition/offset/error) for forensics; blocking retry vs non-blocking ordering trade-off (retry topic breaks per-key ordering — is that acceptable?); Spring Kafka `@RetryableTopic`, `DefaultErrorHandler`.

**Q5. Explain the transactional outbox pattern. What problem does it solve?**
> **Hints:** Dual-write problem: DB commit + Kafka publish can't be atomic; write event to outbox table in the same local transaction, relay publishes (polling or CDC via Debezium); at-least-once → consumers must be idempotent; alternatives: listen-to-yourself, CDC on domain tables; why 2PC/XA is avoided.

**Q6. How do you monitor Kafka health from the application side?**
> **Hints:** Consumer lag (records-lag-max, Burrow, exported to Prometheus) — lag trend matters more than absolute value; under-replicated partitions; producer metrics: batch size, compression, buffer exhaustion, request latency; rebalance frequency; end-to-end latency probes (heartbeat topic).

---

## 10. RDBMS & SQL

**Q1. Explain database indexes. Why can adding an index make things slower? What is a covering index?**
> **Hints:** B+tree structure, logarithmic lookup; write amplification (every index updated on insert/update), optimizer choosing a bad index, index bloat; covering index: all queried columns in the index → index-only scan, no table access; composite index column order (leftmost prefix rule); selectivity; when full scan beats index (low selectivity).

**Q2. Walk through transaction isolation levels and the anomalies each prevents.**
> **Hints:** Read uncommitted (dirty reads), read committed (non-repeatable reads possible), repeatable read (phantoms possible — though InnoDB gap locks mostly prevent), serializable; MVCC: readers don't block writers; snapshot isolation and write skew (the classic on-call doctors example); Postgres default = read committed, real serializable via SSI; know your DB's actual behavior, not just the ANSI table.

**Q3. A query that was fast is suddenly slow in production. Walk through your diagnosis.**
> **Hints:** `EXPLAIN (ANALYZE, BUFFERS)` — compare plan to expectation; stale statistics → `ANALYZE`; plan flip after data growth (index scan → seq scan or bad join order); parameter sniffing / generic plans; missing index after schema change; lock waits (check `pg_locks` / `pg_stat_activity`, blocking chains); bloat from dead tuples, autovacuum falling behind; connection pool saturation masquerading as slow queries.

**Q4. How do you avoid and debug the N+1 query problem with JPA/Hibernate?**
> **Hints:** Lazy loading in a loop → N extra queries; detect: SQL logging, Hibernate statistics, datasource-proxy; fixes: `JOIN FETCH`, `@EntityGraph`, batch fetching (`default_batch_fetch_size`), DTO projections for read paths; caution: fetch join + pagination = in-memory paging (HHH000104); `OpenSessionInView` controversy.

**Q5. Design a schema change (add NOT NULL column) on a 500M-row table with zero downtime.**
> **Hints:** Expand-contract: add nullable column → backfill in batches (throttled, keyed ranges) → validate → add NOT NULL constraint (Postgres: `NOT VALID` then `VALIDATE CONSTRAINT` to avoid long lock; or default-valued column is metadata-only in PG11+); lock awareness (`ACCESS EXCLUSIVE` duration); deploy code tolerant of both states; tools: gh-ost/pt-osc for MySQL, Flyway/Liquibase orchestration.

**Q6. SQL exercise (verbal): given `orders(id, customer_id, amount, created_at)`, find each customer's second-highest order amount.**
> **Hints:** Window function: `SELECT * FROM (SELECT customer_id, amount, DENSE_RANK() OVER (PARTITION BY customer_id ORDER BY amount DESC) rnk FROM orders) t WHERE rnk = 2`; discuss `RANK` vs `DENSE_RANK` vs `ROW_NUMBER` tie behavior; non-window alternative with correlated subquery and why it's worse.

---

## 11. CI/CD & DevOps

**Q1. Describe the CI/CD pipeline you'd design for a Java microservice.**
> **Hints:** Stages: compile → unit tests → static analysis (SpotBugs/Sonar, dependency CVE scan) → build image (multi-stage Dockerfile or jib, layer caching) → integration tests (Testcontainers) → publish (immutable tag = git SHA) → deploy to staging → smoke/contract tests → progressive prod rollout; fast feedback (<10 min to first signal); trunk-based development with feature flags; artifact promoted, never rebuilt per environment.

**Q2. Blue-green vs canary vs rolling deployments — trade-offs?**
> **Hints:** Rolling: default in K8s, gradual, but two versions coexist (N-1 compatibility required); blue-green: instant cutover and rollback, doubles capacity cost, DB migrations still shared; canary: percentage-based with automated analysis (error rate, latency SLOs) before promotion — needs good metrics; all require backward-compatible DB changes (expand-contract).

**Q3. How do you keep builds fast and reliable as the codebase grows?**
> **Hints:** Gradle build cache + configuration cache, parallel test execution, test splitting/sharding across agents; flaky test policy: quarantine + fix, retries hide rot; incremental builds; module boundaries so only affected modules build; Docker layer caching; measuring: pipeline analytics, p95 duration.

**Q4. How do secrets and configuration flow through your pipeline securely?**
> **Hints:** Secrets never in git or image layers; injected at deploy time (Vault agent, external-secrets operator, cloud secret managers); OIDC federation for pipeline → cloud auth (no long-lived keys); least-privilege service accounts; image signing/provenance (cosign, SLSA); scanning images for secrets and CVEs.

**Q5. A deploy went out and error rates spiked. Walk me through what happens next.**
> **Hints:** Automated rollback triggers vs manual decision; roll back first, diagnose later (mitigate > root cause during incident); feature flag kill-switch as faster path; verify rollback actually restored health; DB migration complicates rollback (why forward-compatible migrations matter); postmortem feeds pipeline gates (add canary metric, missing alert).

---

## 12. Agile & Ways of Working

**Q1. As a senior engineer, how do you handle a sprint that's clearly going to miss its commitment?**
> **Hints:** Raise early — transparency over heroics; re-scope with PO (split stories, cut scope not quality), swarm on highest-value item; look for systemic causes at retro (chronic overcommitment, unplanned work ratio, unclear acceptance criteria); avoid normalizing overtime.

**Q2. How do you handle technical debt with a product owner who only wants features?**
> **Hints:** Translate debt into business language (velocity drag, incident risk, onboarding cost — with data); debt budget (e.g., ~20% capacity); attach refactoring to feature work in the same area (boy-scout rule); make it visible in the backlog, not invisible padding; distinguish deliberate vs reckless debt (Fowler quadrant).

**Q3. What does "definition of done" mean to you, and what belongs in it for a backend team?**
> **Hints:** Code reviewed, tests written and green (unit + integration), security/static checks pass, docs/runbook updated, feature flagged, deployed to staging, observability in place (metrics/alerts for the new path), PO acceptance; DoD vs acceptance criteria distinction.

**Q4. How do you run effective code reviews on a team with mixed seniority?**
> **Hints:** Review for correctness, design, and knowledge-sharing — not style (automate style with formatters/linters); small PRs; comment kindness and "nit:" convention; author-driven walkthroughs for big changes; avoid gatekeeping bottlenecks (review SLAs); mentoring through reviews rather than rewriting juniors' code.

---

## 13. Incident Management & Production Support

**Q1. You're on call. An alert fires: p99 latency tripled on the orders service. First 10 minutes?**
> **Hints:** Acknowledge, assess blast radius/severity, check "what changed" first (deploys, config, feature flags, dependency deploys, traffic shift); dashboards: RED metrics (rate, errors, duration), saturation (CPU, GC, pools, DB connections); mitigate fast (rollback, flag off, scale out) before root-causing; communicate status early; escalate if customer-impacting beyond threshold — don't hero solo.

**Q2. Explain SLI, SLO, and error budget. How do they change team behavior?**
> **Hints:** SLI = measured indicator (success rate, latency under X ms); SLO = target (99.9% over 30 days); error budget = 1 − SLO, spent by incidents and risky changes; budget exhausted → freeze features, focus reliability; stops both over-engineering (five 9s nobody needs) and reliability neglect; alert on burn rate, not point-in-time.

**Q3. What makes a good postmortem? What kills the process?**
> **Hints:** Blameless (systems and incentives, not individuals), timeline of events + detection/mitigation timing (MTTD/MTTR), contributing factors over single "root cause" (avoid stopping at "human error"), concrete tracked action items with owners; killed by: blame culture, action items that never get done, template theater; share learnings org-wide.

**Q4. A customer reports intermittent 500s but your dashboards look green. How do you proceed?**
> **Hints:** Averages hide tail pain — check per-customer/per-endpoint slices; get specifics (request IDs, timestamps, region); search logs/traces by correlation ID; check edge layers your metrics miss (CDN, LB, API gateway 5xx that never reach the app); client-side retries masking failures; sampling gaps in tracing; add a synthetic probe replicating their call pattern; keep the customer informed with findings.

**Q5. How do you make a service you own easier to support at 3 AM?**
> **Hints:** Runbooks linked from alerts (alert → hypothesis → command to run); actionable alerts only (every page must be actionable — cull noisy ones); structured logs with correlation IDs; admin/ops endpoints (drain, replay, cache flush) with audit; dashboards organized by user journey; feature flags as kill switches; game days / chaos drills to test the above.

---

## 14. Live Coding Questions (12)

Ask the candidate to code in an IDE or shared editor. Hints describe the expected approach and follow-ups.

**LC1. Group and aggregate with Streams.**
Given `List<Employee>` (name, department, salary), produce a `Map<String, Double>` of department → average salary, then the highest-paid employee per department.
> **Hints:** `Collectors.groupingBy(Employee::getDept, averagingDouble(...))`; second part: `groupingBy(dept, collectingAndThen(maxBy(comparingDouble(...)), Optional::get))`; follow-up: sort result by average descending → `LinkedHashMap` via `toMap` with merge + supplier.

**LC2. First non-repeating character in a string.**
> **Hints:** Two passes with `LinkedHashMap<Character, Integer>` counting, then first entry with count 1 — O(n); stream version: `chars()` boxing pitfalls; follow-up: Unicode/supplementary characters, single-pass alternatives.

**LC3. Implement a thread-safe bounded blocking queue (put/take) without using `java.util.concurrent` queues.**
> **Hints:** `ReentrantLock` + two `Condition`s (notFull/notEmpty), or `synchronized` + `wait/notifyAll`; must use `while` loops around wait (spurious wakeups); follow-up: fairness, timeout variants, why two conditions beat notifyAll.

**LC4. Find the top K frequent elements in a large list.**
> **Hints:** Count with `HashMap`, then min-heap of size K (O(n log k)) or bucket sort by frequency (O(n)); streams version with `groupingBy(counting())` + sorted + limit; discuss why full sort is O(n log n) and when it's fine anyway.

**LC5. Implement a rate limiter (token bucket) callable from multiple threads.**
> **Hints:** State: tokens + lastRefill timestamp; refill lazily on `tryAcquire` based on elapsed time; synchronize or use atomics with CAS loop; follow-up: sliding window log vs fixed window burst problem, distributed version (Redis + Lua).

**LC6. Given a list of intervals, merge all overlapping intervals.**
> **Hints:** Sort by start; iterate, extend current if `next.start <= current.end`, else emit; O(n log n); edge cases: touching intervals, single element, unsorted input; follow-up: interval insertion into already-merged list.

**LC7. Write a method that reads a large CSV (millions of rows) and returns the top 10 customers by total spend — without loading the whole file into memory.**
> **Hints:** `Files.lines()` (streaming, try-with-resources), accumulate into `Map<String, BigDecimal>` (bounded by customer cardinality, not rows), then top 10 via heap or stream sort; `BigDecimal` for money; malformed-line handling; follow-up: what if customer cardinality also doesn't fit → external sort / partial aggregation.

**LC8. Implement `retry(Supplier<T> action, int maxAttempts, Duration backoff)` with exponential backoff.**
> **Hints:** Loop with try/catch, rethrow after max attempts (preserve last exception, add suppressed), `backoff * 2^attempt` + jitter, `Thread.sleep` and handling `InterruptedException` correctly (restore interrupt flag); follow-up: which exceptions are retryable, async version with `CompletableFuture` and scheduled executor.

**LC9. Detect if a `LinkedList`-style structure has a cycle; return the node where the cycle begins.**
> **Hints:** Floyd's tortoise/hare O(1) space; after meeting, reset one pointer to head, advance both one step → meeting point is cycle start (be ready to sketch why); alternative `HashSet` of visited nodes O(n) space.

**LC10. Fix this broken code (present snippet): a `SimpleDateFormat` shared as a static field across threads, and a `HashMap` written by multiple threads.**
> **Hints:** `SimpleDateFormat` is not thread-safe → `DateTimeFormatter` (immutable) or `ThreadLocal`; `HashMap` concurrent writes → lost updates/corruption → `ConcurrentHashMap`; candidate should explain *why* (internal state mutation), not just swap classes.

**LC11. Implement a simplified `@Cacheable`: a generic memoize wrapper `Function<K,V> memoize(Function<K,V> f)` that is thread-safe and computes each key at most once.**
> **Hints:** `ConcurrentHashMap.computeIfAbsent` — but recursive computation deadlock caveat; caching futures (`CompletableFuture`) to prevent duplicate concurrent computation (cache stampede); follow-up: eviction/TTL, soft references, error caching policy (don't cache failures).

**LC12. SQL live exercise: given tables `employees(id, name, dept_id, salary)` and `departments(id, name)`, write queries for (a) departments with more than 5 employees, (b) employees earning above their department average, (c) delete duplicate employee rows keeping lowest id.**
> **Hints:** (a) `GROUP BY dept_id HAVING COUNT(*) > 5` with join for name; (b) correlated subquery or window `AVG(salary) OVER (PARTITION BY dept_id)` in subselect; (c) self-join delete or `ROW_NUMBER() OVER (PARTITION BY name, dept_id ...)` CTE delete where rn > 1; discuss `WHERE` vs `HAVING`.

---
