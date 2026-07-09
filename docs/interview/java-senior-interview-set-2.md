# Senior Java Developer Interview — Set 2 (10+ Years Experience)

Alternate set — no overlap with Set 1. Each question includes **answer hints**.

---

## 1. Core Java

**Q1. What is immutability and how do you design a truly immutable class? Why does it matter?**
> **Hints:** `final` class (or sealed), `final` fields, no setters, defensive copies of mutable inputs/outputs (arrays, dates, collections — or `List.copyOf`), don't leak `this` in constructor; benefits: thread safety without locks, safe map keys, safe caching/sharing; records as the modern shortcut and their limits (shallow immutability).

**Q2. Explain `String` interning and why `String` is immutable. What is the cost of careless string concatenation?**
> **Hints:** String pool, `intern()`, compile-time constant folding; immutability enables pooling, hashCode caching, security (class names, file paths); concat in loops → O(n²) — `StringBuilder`; JEP 280 `invokedynamic` concat for single expressions; compact strings (Latin-1 byte[] since 9).

**Q3. Compare checked vs unchecked exceptions. What is your production error-handling philosophy?**
> **Hints:** Checked = recoverable contract, but ergonomics poor with lambdas/streams — modern trend toward unchecked + documented; wrap-and-rethrow with context, never swallow, catch at boundaries (controller advice, message handler), preserve cause chain; `try-with-resources` and suppressed exceptions; don't use exceptions for control flow (cost of fillInStackTrace).

**Q4. What are sealed classes and pattern matching? How do they change domain modeling?**
> **Hints:** `sealed interface Shape permits Circle, Square` — closed hierarchies; exhaustive `switch` pattern matching (compiler flags missing cases — new subtype breaks the build, not production); records + sealed = algebraic data types; replaces visitor pattern boilerplate; guarded patterns (`case Circle c when c.radius() > 10`), record deconstruction.

**Q5. How does reflection work and what are its costs? Where do frameworks use it and what's replacing it?**
> **Hints:** `Class`/`Method`/`Field` metadata access, `setAccessible` and module restrictions (strong encapsulation since 16); costs: no JIT inlining, boxing, security bypass; used by Spring DI, Jackson, JPA; replacements: `MethodHandles`/`VarHandle`, compile-time annotation processing, AOT/native-image forcing reflection config (GraalVM), Spring AOT.

**Q6. Explain `Comparable` vs `Comparator`. What subtle bugs occur in comparator implementations?**
> **Hints:** Natural order vs external strategy; `Integer.compare(a, b)` not `a - b` (overflow!); inconsistent-with-equals comparators break `TreeMap` contracts; transitivity violations → `IllegalArgumentException: Comparison method violates its general contract` in TimSort; composing: `Comparator.comparing(...).thenComparing(...)`, `nullsFirst`, `reversed()`.

**Q7. What happens, step by step, when you run `java -jar app.jar`?**
> **Hints:** JVM launch, parse manifest `Main-Class`, classloading + verification + initialization order (static blocks), interpreter starts, JIT warms hot paths; JVM ergonomics picking heap/GC defaults from container limits (`UseContainerSupport`); shutdown hooks on SIGTERM; good question to gauge depth — let the candidate go as deep as they can.

---

## 2. Collections & Data Structures

**Q1. What guarantees do `Set` implementations give about iteration order? When did relying on it burn you?**
> **Hints:** `HashSet` none (and order can change across JVM runs/resizes), `LinkedHashSet` insertion order, `TreeSet` sorted; classic bug: tests passing by accidental iteration order; `Map.of()`/`Set.of()` randomize iteration order per JVM run deliberately (`SALT`) to expose such bugs.

**Q2. How does `PriorityQueue` work? Why is iterating it in order a bug?**
> **Hints:** Binary heap in array; only `peek`/`poll` respect order — iterator traverses array layout, not sorted order; O(log n) insert/poll, O(n) contains/remove(Object); not thread-safe (`PriorityBlockingQueue`); use cases: top-K, scheduling, Dijkstra.

**Q3. Explain `equals`/`hashCode` requirements for keys in hash-based vs tree-based collections.**
> **Hints:** Hash-based need consistent `hashCode`/`equals`; tree-based use `compareTo`/comparator *instead of* equals — a `TreeSet` treats compare==0 as duplicate even if `!equals` (e.g., `BigDecimal("1.0")` vs `("1.00")` in `TreeSet` vs `HashSet`); "consistent with equals" clause in `Comparable` javadoc.

**Q4. When would you pick immutable collections (`List.of`, Guava) vs `Collections.unmodifiableList`?**
> **Hints:** `unmodifiableList` is a *view* — underlying list can still change; `List.of`/`copyOf` truly immutable, null-hostile, memory-efficient (field-based small sizes); defensive copying at API boundaries; structural sharing in persistent collections (Vavr) for functional style.

**Q5. Estimate memory: `HashMap<Long, Long>` with 1M entries. How would you shrink it?**
> **Hints:** Entry ~32B + boxed Longs ~24B each + table refs → roughly 80–100MB vs 16MB of raw longs; compressed oops; shrink: primitive collections (Eclipse Collections, fastutil `Long2LongOpenHashMap`), arrays if keys dense; measuring with JOL; this tests mechanical sympathy, exact numbers matter less than the reasoning.

---

## 3. Streams & Functional Java

**Q1. What are stateless vs stateful intermediate operations? Why does it matter?**
> **Hints:** Stateless: `map`, `filter` — process elements independently; stateful: `sorted` (full buffering), `distinct` (seen-set), `limit`/`skip` (ordering constraint); stateful ops are barriers for parallelism and memory; `unordered()` can unlock performance for `distinct`/`limit` in parallel.

**Q2. `Optional` — intended use, and the anti-patterns you reject in code review.**
> **Hints:** Designed for return types only; anti-patterns: `Optional` fields/parameters (serialization, ergonomics), `opt.get()` without check, `isPresent()+get()` instead of `map`/`orElseGet`, `Optional.of(nullable)` NPE, wrapping collections (return empty collection instead); `orElse` vs `orElseGet` eager-evaluation gotcha; `orElseThrow` as self-documenting get.

**Q3. Implement "find the 3 most recent orders per customer" with streams. Then argue when NOT to use streams.**
> **Hints:** `groupingBy(customer, collectingAndThen(toList(), l -> l.stream().sorted(byDateDesc).limit(3).toList()))` or sort first then group with bounded accumulator; when not: complex flows read worse than loops, checked exceptions, debugging (breakpoints in lambdas), performance-critical hot paths with boxing; "streams for transformation pipelines, loops for algorithms".

**Q4. How does `Collectors.toMap` bite people in production?**
> **Hints:** `IllegalStateException: Duplicate key` without merge function — and the message shows *values* not the key (pre-fix versions); NPE on null values (HashMap.merge restriction); no order guarantee → pass `LinkedHashMap::new` supplier; always ask "can keys collide?" before using two-arg version.

**Q5. What is a `Spliterator`? When have you (or would you) write a custom one?**
> **Hints:** Splittable iterator: `tryAdvance`, `trySplit`, characteristics (SIZED, ORDERED, IMMUTABLE) feeding optimizations; custom sources: paginated API as a stream, chunked file reading; `StreamSupport.stream(spliterator, parallel)`; characteristics wrong → subtle bugs.

**Q6. Explain method reference kinds and one ambiguity gotcha.**
> **Hints:** Static `Integer::parseInt`, bound `str::length`, unbound `String::length` (receiver becomes first param), constructor `ArrayList::new`; unbound vs static ambiguity when both match; readability judgment — method refs not always clearer than lambdas.

---

## 4. Concurrency & Multithreading

**Q1. `synchronized` vs `ReentrantLock` vs `StampedLock` — when does each earn its complexity?**
> **Hints:** `synchronized`: simplest, JIT-optimized (biased locking history, lock coarsening), works with virtual threads post-24; `ReentrantLock`: tryLock/timeout, interruptible, fairness, multiple conditions; `StampedLock`: optimistic reads (validate stamp) for read-heavy — but non-reentrant and no conditions, easy to misuse; `ReadWriteLock` writer starvation; measure before choosing exotic locks.

**Q2. What is `ThreadLocal`? Describe a leak scenario and the virtual-thread-era alternative.**
> **Hints:** Per-thread storage — transaction contexts, MDC, non-thread-safe formatters; leak: pooled threads never die → stale values persist, classloader leaks on redeploy (value holds ref to webapp classes) → always `remove()` in finally; with millions of virtual threads, per-thread copies get expensive → `ScopedValue` (immutable, bounded lifetime, cheap inheritance).

**Q3. How does `ForkJoinPool` work? What is work stealing?**
> **Hints:** Per-worker deques: own tasks LIFO (cache-friendly), steal FIFO from others' tails (largest chunks); recursive divide-and-conquer (`RecursiveTask`); commonPool sizing (cores − 1) and why blocking in it is dangerous (`ManagedBlocker` as escape hatch); used by parallel streams and `CompletableFuture` defaults.

**Q4. Explain producer-consumer. Compare implementations: `wait/notify`, `BlockingQueue`, and Kafka as the distributed version.**
> **Hints:** In-JVM: `ArrayBlockingQueue`/`LinkedBlockingQueue` (bounded! backpressure), poison-pill or interrupt-based shutdown; `wait/notify` version to test fundamentals (while-loop guard); `SynchronousQueue` handoff; scaling out: same pattern becomes message broker — persistence, consumer groups, redelivery replacing in-memory guarantees.

**Q5. What is false sharing? How would you detect and fix it?**
> **Hints:** Independent variables on the same cache line → cores invalidate each other's caches despite no logical sharing; symptom: parallel code scaling worse than expected; detect: perf counters (cache misses), JMH with/without padding; fix: `@Contended` (with `-XX:-RestrictContended`), padding fields; `LongAdder`'s striped cells as the canonical design response.

**Q6. Design a concurrent in-memory cache with TTL expiry. What are the design decisions?**
> **Hints:** `ConcurrentHashMap` + timestamped entries; expiry: lazy-on-read vs background sweeper vs timer wheel; eviction policy separate from expiry; stampede prevention (compute-through with future caching); memory bound (size vs weight); or the honest senior answer — "use Caffeine, here's what it does for me and why hand-rolling is a bug farm" — then whiteboard the internals anyway.

---

## 5. JVM Internals, GC & Performance

**Q1. Explain generational GC and the weak generational hypothesis. What is premature promotion?**
> **Hints:** Most objects die young → cheap eden collection (copying, cost proportional to survivors); survivor spaces, tenuring threshold; premature promotion: allocation bursts / undersized young gen push short-lived objects to old gen → expensive old collections, fragmentation; humongous objects in G1 bypassing young gen; tune by observing tenuring distribution.

**Q2. Your container gets OOMKilled but the heap looks fine. What's eating memory?**
> **Hints:** JVM ≠ heap: Metaspace, thread stacks (1MB × threads), direct/native buffers (Netty!), code cache, GC overhead, glibc arena fragmentation (`MALLOC_ARENA_MAX`, jemalloc); Native Memory Tracking (`-XX:NativeMemoryTracking=summary` + `jcmd VM.native_memory`); set `MaxRAMPercentage` leaving headroom; mmap'd files count toward RSS.

**Q3. How would you benchmark whether implementation A is faster than B? Why is a `main()` with `System.nanoTime` wrong?**
> **Hints:** JIT warmup, dead-code elimination (result unused → optimized away), constant folding, OSR quirks, GC interference; JMH: warmup/measurement iterations, forks, `Blackhole`, `@State`; measure allocation (`gc.alloc.rate`) too; microbenchmark vs production profile mismatch — profile first (async-profiler), benchmark second.

**Q4. What are safepoints? How can they cause mysterious pauses that aren't GC?**
> **Hints:** Global points where all threads park so VM ops run (GC, deopt, thread dumps, biased lock revocation); time-to-safepoint: a thread in a big counted loop or long JNI call delays everyone (`-XX:+PrintSafepointStatistics` / `-Xlog:safepoint`); huge array copies; symptoms: pauses with no GC activity; fixes: loop strap reduction awareness, identifying the vm op.

**Q5. Explain escape analysis and why "allocation is cheap" is mostly true — until it isn't.**
> **Hints:** TLAB bump-the-pointer allocation (~10 instructions); escape analysis → scalar replacement (no allocation at all) when object doesn't escape; breaks with polymorphism/debug flags/large methods failing inlining; allocation *rate* drives GC frequency — cheap individually, expensive in aggregate; measuring allocation pressure with JFR.

---

## 6. Spring Boot & Spring Framework

**Q1. Walk through the Spring bean lifecycle from definition to destruction.**
> **Hints:** Definition scan → `BeanFactoryPostProcessor` (e.g., property placeholders) → instantiation → dependency injection → `Aware` callbacks → `BeanPostProcessor` before-init → `@PostConstruct`/`InitializingBean` → post-init (proxies created here — AOP!) → in service → `@PreDestroy`; why `@Transactional` self-invocation fails clicks once you know proxying happens in post-processing; circular dependency resolution via early references.

**Q2. How do you test a Spring Boot service properly? Slice tests vs `@SpringBootTest` vs Testcontainers.**
> **Hints:** Pyramid: plain JUnit for domain logic (no Spring), slices (`@WebMvcTest` + `MockMvc`, `@DataJpaTest`) for adapters, `@SpringBootTest` sparingly (context caching to keep suite fast — count your distinct contexts), Testcontainers for real DB/Kafka (`@ServiceConnection` in Boot 3.1+); `@MockBean` context-cache pollution; contract tests for API compatibility; avoid H2-vs-Postgres dialect lies.

**Q3. Explain Spring AOP. What can't it do, and when do you need AspectJ?**
> **Hints:** Proxy-based: only public external method calls on Spring beans; can't advise self-invocation, private/final methods, constructors, field access; JDK proxy (interfaces) vs CGLIB (subclass); AspectJ weaving (compile/load-time) removes those limits at complexity cost; real uses: transactions, security, metrics, retry — and debugging when the proxy surprises you.

**Q4. How do you implement resilient outbound HTTP calls from a Spring Boot service?**
> **Hints:** `RestClient`/`WebClient` (RestTemplate maintenance mode); explicit connect/read timeouts (never infinite defaults), connection pool sizing; Resilience4j annotations: `@Retry`, `@CircuitBreaker`, `@Bulkhead`, `@RateLimiter` — and their order in the decorator chain; per-dependency thread isolation; fallbacks that degrade gracefully; propagating deadlines; testing with WireMock fault injection.

**Q5. What's your approach to database migrations in a Spring Boot app with multiple instances deploying?**
> **Hints:** Flyway/Liquibase versioned migrations in the artifact, run on startup with locking (or as a separate pre-deploy job — better for K8s rolling deploys); never `ddl-auto=update` in prod (validate only); backward-compatible migrations (expand-contract) because old and new pods run simultaneously; repeatable vs versioned scripts, checksums, out-of-order policy.

**Q6. Spring Security: walk through what happens to an incoming request with a JWT.**
> **Hints:** Filter chain order; `BearerTokenAuthenticationFilter` → `JwtDecoder` (signature via JWKS, exp/iss/aud validation) → `JwtAuthenticationConverter` (claims → authorities) → `SecurityContext`; method security `@PreAuthorize`; stateless (`SessionCreationPolicy.STATELESS`); common misconfig: permitAll ordering, CSRF for stateless APIs, leaking stack traces in 401 handlers.

**Q7. What changed in Spring Boot 3 that required real migration work?**
> **Hints:** Jakarta namespace (`javax.*` → `jakarta.*` — every import, plus library compatibility), Java 17 baseline, Spring Security 6 (lambda DSL, `authorizeHttpRequests`), trailing-slash matching change, Micrometer tracing replacing Sleuth, AOT/native support, actuator endpoint changes; how you'd run such a migration across 30 services (OpenRewrite recipes, one canary service first).

---

## 7. Microservices & Distributed Systems

**Q1. Explain CAP theorem and what it actually means for a system you've built. What is PACELC?**
> **Hints:** Partition tolerance is not optional in distributed systems — real choice is C vs A *during partitions*; PACELC adds the else-case: latency vs consistency trade-off even without partitions; map to real systems: Kafka configs, Postgres sync vs async replication, DynamoDB/Cassandra tunable consistency; per-operation choices (read-your-writes for the user's own data, eventual for feeds).

**Q2. How do services discover and talk to each other in your architecture? Compare client-side discovery, server-side LB, and service mesh.**
> **Hints:** Eureka/Consul client-side (client picks instance, needs smart client per language) vs K8s Services + kube-proxy/DNS (server-side, simple) vs mesh (Istio/Linkerd sidecars or ambient: mTLS, retries, traffic shifting, observability moved to platform layer); mesh cost: latency hop, operational complexity; gRPC client-side LB gotcha with L4 K8s services (long-lived HTTP/2 pins to one pod).

**Q3. What is eventual consistency? Design "email uniqueness" across a distributed user service.**
> **Hints:** Uniqueness is a global invariant — hard eventually-consistent; options: single writer/DB constraint as source of truth (simplest — keep uniqueness in one service's ACID boundary), reserve-then-confirm saga, or accept-then-compensate (rare-collision apology); read-your-writes vs monotonic reads session guarantees; this question tests whether they reach for the simple consistent core instead of cargo-cult distribution.

**Q4. How do you prevent cascading failures? Explain bulkheads, load shedding, and backpressure end to end.**
> **Hints:** One slow dependency exhausts caller threads → caller dies → its callers die; bulkheads: separate pools/semaphores per dependency; timeouts strictly less than caller's budget (deadline propagation); load shedding: reject early (429/503) at admission when saturated — queue depth limits, adaptive concurrency (gradient/Vegas style); backpressure through async chains; brownout: degrade optional features first; test with chaos engineering.

**Q5. Synchronous REST vs async messaging between services — how do you choose per interaction?**
> **Hints:** Sync for query/immediate-response UX paths, async for state-change propagation, decoupled lifecycles, burst absorption; temporal coupling and availability math of sync chains (0.99⁵ ≈ 0.95); async costs: eventual consistency in UX, harder debugging, duplicate delivery; commands vs events distinction; hybrid: sync accept + async process with status endpoint (202 + Location).

**Q6. What is the strangler fig pattern? Describe decomposing a monolith you've worked on.**
> **Hints:** Route traffic through a facade, peel capabilities incrementally, old and new coexist; data is the hard part: shared DB interim → change-data-capture sync → cut over ownership; anti-corruption layer; seams: start with leaf capabilities, low coupling/high value; measure progress, avoid the multi-year "big bang in disguise"; when to stop (modular monolith may be the right end state).

---

## 8. REST API Design

**Q1. Explain HTTP method semantics: idempotency and safety. Why does PUT vs PATCH vs POST matter operationally?**
> **Hints:** Safe: GET/HEAD (no side effects — crawlers, prefetch); idempotent: PUT/DELETE (retry-safe — infra can auto-retry); POST neither → needs idempotency keys; PATCH partial update (JSON Merge Patch vs JSON Patch, null-handling ambiguity); operational impact: proxies/gateways retry idempotent methods by default.

**Q2. Design the API for a long-running operation (report generation taking minutes).**
> **Hints:** `POST /reports` → 202 Accepted + `Location: /operations/{id}`; polling with status resource (state machine: pending/running/succeeded/failed + result link); alternatives: webhooks (with retries + signature), SSE/WebSocket for push; expiry of results; idempotency key on submit; don't hold HTTP connections open across minutes.

**Q3. How do you design consistent error responses across a fleet of services?**
> **Hints:** RFC 9457 problem+json: `type`, `title`, `status`, `detail`, `instance` + extensions (correlation ID, field errors array); stable machine-readable error codes (clients switch on code, not message); never leak stack traces/SQL; shared library or gateway normalization; document errors in OpenAPI as first-class citizens.

**Q4. What are the caching mechanisms in HTTP and where do they apply to APIs?**
> **Hints:** `Cache-Control` (max-age, private/public, no-store), `ETag`/`If-None-Match` → 304 saves bandwidth (still hits server) vs freshness (saves the round trip); `Vary` header pitfalls (Authorization); CDN caching for anonymous GETs; app-level caching when HTTP caching doesn't fit (per-user data); cache invalidation strategy honesty.

**Q5. Review this endpoint (verbal): `GET /api/getUserOrders?uid=123&del=true` that deletes cancelled orders and returns the rest. What's wrong?**
> **Hints:** GET with side effects (violates safety — prefetchers will delete data!), verb in URL, ambiguous flag params, no versioning, sequential numeric IDs (enumeration/BOLA risk), no pagination; redesign: `GET /users/{id}/orders?status=active` + separate `DELETE`; tests whether they *spot* problems unprompted — a senior should list most within a minute.

---

## 9. Kafka & Messaging

**Q1. How does a Kafka producer decide where and when a record is sent? Which configs shape throughput vs latency vs durability?**
> **Hints:** Partitioner (key hash / sticky for null keys), `batch.size` + `linger.ms` (throughput vs latency dial), `compression.type` (zstd/lz4), `acks` (0/1/all), `retries` + `delivery.timeout.ms`, `max.in.flight.requests` vs ordering (safe with idempotence ≤5), `buffer.memory` and what happens when it fills (send blocks); walk through a tuning scenario.

**Q2. Compare Kafka with RabbitMQ (or another broker). When is Kafka the wrong choice?**
> **Hints:** Kafka: replayable log, consumer-controlled position, horizontal throughput, retention-based; Rabbit: smart broker routing (topic/headers exchanges), per-message ack, priorities, TTL/delay, lower latency at small scale, simpler ops; Kafka wrong for: task queues needing per-message routing/completion, request-reply, small-scale where its ops burden dominates; also mention cloud-native options (SQS/PubSub) honestly.

**Q3. Explain log compaction. Design a use case where a compacted topic replaces a database read.**
> **Hints:** Retain latest value per key (tombstones delete); use: changelog/table semantics — e.g., product catalog cache: services bootstrap state by replaying compacted topic into a local store (Kafka Streams KTable/GlobalKTable); compaction is lazy — readers still see duplicates, must upsert; `min.compaction.lag`, tombstone retention (`delete.retention.ms`) gotchas.

**Q4. A consumer group's lag is growing steadily. Diagnose end to end.**
> **Hints:** Is it production spike or consumption slowdown (compare produce rate vs consume rate)? Consumer side: slow downstream call per record (batch it, parallelize within partition carefully), GC pauses, rebalance loops (`max.poll.interval.ms`), one hot partition (key skew — check per-partition lag); fixes: scale consumers up to partition count, increase `max.poll.records` with async processing, fix skewed key; when to add partitions (breaks key ordering mapping!).

**Q5. How does Kafka Streams do stateful processing? What happens on instance failure?**
> **Hints:** State stores (RocksDB) local per task, backed by changelog topics; failure → task migrates, state restored by replaying changelog (standby replicas to cut restore time); repartitioning on `groupBy` key changes; windowing (tumbling/hopping/session) + grace period for late events; exactly-once v2; interactive queries; compare with ksqlDB/Flink briefly.

**Q6. How do you decide the number of partitions for a new topic? What's hard to change later?**
> **Hints:** Target throughput / per-consumer throughput, plus headroom; ceiling of parallelism = partition count; too many: broker memory/file handles, longer failovers, latency; can only increase, never decrease — and increasing remaps keys (breaks "same key → same partition" history); over-provision moderately (e.g., 2–3× expected consumers); per-key ordering requirement drives keying strategy first.

---

## 10. RDBMS & SQL

**Q1. Explain JOIN types and how the database physically executes them (nested loop, hash, merge).**
> **Hints:** INNER/LEFT/FULL/CROSS semantics, semi-joins (`EXISTS`); physical: nested loop (small driving set + index on inner), hash join (large unsorted sets, memory), merge join (both sorted/indexed); why the optimizer's choice flips with cardinality estimates; LEFT JOIN + WHERE-on-right-table accidentally becoming INNER.

**Q2. What is MVCC? What are dead tuples and why does autovacuum matter (Postgres)?**
> **Hints:** Writers create row versions; readers see snapshot (xmin/xmax visibility) — readers never block writers; UPDATE = insert new version + mark old dead; dead tuples bloat tables/indexes until vacuumed; autovacuum tuning (scale factors on big tables), long-running transactions holding back cleanup (`xmin` horizon) → bloat + wraparound risk; HOT updates; MySQL InnoDB contrast (undo logs, purge).

**Q3. Design the schema for a multi-tenant SaaS: shared tables vs schema-per-tenant vs DB-per-tenant.**
> **Hints:** Shared + `tenant_id` column: cheapest, needs rigorous scoping (composite indexes leading with tenant_id, row-level security as backstop), noisy neighbors; schema-per-tenant: isolation vs migration fan-out pain; DB-per-tenant: max isolation/compliance, ops automation required; hybrid (big tenants isolated); the answer should weigh tenant count, size skew, compliance.

**Q4. Explain pessimistic vs optimistic locking in JPA/SQL. Implement inventory decrement safely.**
> **Hints:** Optimistic: `@Version` → `OptimisticLockException`, retry loop — good for low contention; pessimistic: `SELECT ... FOR UPDATE` (`@Lock(PESSIMISTIC_WRITE)`), holds row lock; best for counters: atomic conditional update `UPDATE inventory SET qty = qty - 1 WHERE id = ? AND qty > 0` + check rows affected — no read-modify-write race at all; `FOR UPDATE SKIP LOCKED` for job-queue pattern.

**Q5. When do you denormalize? What replaces JOINs at scale?**
> **Hints:** Read-path hot spots, avoiding N-way joins on every request; techniques: summary/rollup tables, materialized views (refresh strategy!), duplicated columns maintained by triggers/app events, CQRS read models; costs: write complexity, drift risk (reconciliation jobs); measure first — Postgres joins fine at surprising scale with right indexes.

**Q6. SQL exercise (verbal): `events(user_id, event_type, created_at)` — find users whose first event was 'signup' and who had ≥3 events within 7 days of signup.**
> **Hints:** Window: `FIRST_VALUE(event_type) OVER (PARTITION BY user_id ORDER BY created_at)` or `ROW_NUMBER` CTE to get first event; then join/filter events in `created_at < signup_time + interval '7 days'` grouped `HAVING COUNT(*) >= 3`; probe understanding of window vs group, and index to support it (`user_id, created_at`).

---

## 11. CI/CD & DevOps

**Q1. Trunk-based development vs GitFlow — what do you run and why?**
> **Hints:** Trunk-based: short-lived branches (<1–2 days), feature flags decouple deploy from release, enables true CI (integration daily, merge hell eliminated); GitFlow suits versioned/appliance software, adds ceremony and long-lived divergence for SaaS; DORA research correlation with performance; migration path and prerequisites (test suite trust, flags, review discipline).

**Q2. What are feature flags operationally? What debt do they create?**
> **Hints:** Release flags (decouple deploy/release), ops kill switches, experiments, permission flags — different lifecycles; runtime evaluation (LaunchDarkly/Unleash/homegrown), targeting, default-safe on config service outage; debt: dead flags multiply code paths (2ⁿ combinations untested) → expiry dates, cleanup tickets in DoD, flag inventory reviews; testing strategy: both states in CI for active flags.

**Q3. How do you test infrastructure and deployment itself, not just application code?**
> **Hints:** IaC (Terraform plan review, policy-as-code via OPA/Sentinel, `terraform validate` + tflint), K8s manifests (kubeconform, kyverno/gatekeeper policies), Helm chart tests; ephemeral preview environments per PR; smoke tests post-deploy as pipeline gates; DR/game-day exercises; rollback rehearsal — "your deploy is only as tested as your rollback".

**Q4. Explain observability-driven deployment: how does the pipeline know a release is bad?**
> **Hints:** Canary analysis: compare error rate/latency/saturation between canary and baseline (Kayenta-style automated judgment or Argo Rollouts analysis templates), statistical significance vs eyeballing; SLO burn-rate gates; synthetic probes post-deploy; deployment markers in dashboards/tracing; auto-rollback thresholds and their false-positive tuning; "you can't canary what you don't measure".

**Q5. A build passes locally but fails in CI (or vice versa). What are the usual suspects and the systemic fix?**
> **Hints:** Environment drift: JDK version, locale/timezone/encoding, filesystem case sensitivity, network access, `~/.m2`/`~/.gradle` local state, snapshot dependencies, test ordering/parallelism differences, hidden dependence on machine cores; systemic: hermetic builds (containerized toolchain, locked dependency versions, checksum verification), reproducible builds, `./gradlew` wrapper enforcement, CI runs on clean workspace.

---

## 12. Agile & Ways of Working

**Q1. Story pointing keeps sparking arguments and estimates are always wrong. What do you change?**
> **Hints:** Points measure relative complexity/uncertainty, not hours — arguments often signal unclear requirements (fix refinement, not the scale); options: right-size stories and just count them (#NoEstimates flow metrics — cycle time, throughput), Monte Carlo forecasting from historicals; stop using points as performance metrics (Goodhart's law); estimation is for conversation, forecasts are for data.

**Q2. As the senior on the team, how do you technically onboard and grow mid-level engineers?**
> **Hints:** Pairing/mobbing on real work over documentation dumps; graduated ownership (bug → feature → epic → on-call with backup); design review culture where they present, you ask questions; delegate visibly and let them own outcomes (including safe failures); ADRs so decisions are learnable; measure: bus factor, review distribution, who answers questions in channels.

**Q3. Product wants a feature in 4 weeks; your honest estimate is 8. Walk through the conversation.**
> **Hints:** Never silently pad or silently accept; decompose and show the math, offer scope options (MVP slice in 4, full in 8 — cut scope not quality), surface risks/assumptions, agree checkpoints for early warning; the anti-pattern answers: heroics/burnout, quality shortcuts that surface as incidents, or sandbagging that erodes trust; document the trade-off decision and owner.

**Q4. What agile ceremonies actually earn their time for an experienced team, and what would you cut?**
> **Hints:** Look for outcome-orientation: retros with tracked experiments (cut if action items never happen — fix the follow-through, not the meeting), standups as blocker-surfacing (async when co-located in timezone, cut status-theater), refinement as just-in-time not batch; planning lightweight with a ready backlog; demos to real stakeholders; the meta-point: rituals serve feedback loops — keep the loop, adapt the ritual.

---

## 13. Incident Management & Production Support

**Q1. Define severity levels and walk through incident roles. Why does a "commander" who doesn't debug matter?**
> **Hints:** SEV1 (customer-facing outage, all hands) → SEV3/4 (degraded, business-hours); roles: incident commander (coordination, decisions, comms cadence — deliberately not hands-on-keyboard), ops lead (investigates), comms lead (stakeholders/status page), scribe (timeline for postmortem); IC prevents the classic failure mode: everyone debugging, nobody deciding, stakeholders in the dark; handoffs for long incidents.

**Q2. Design the observability for a new payment service before it ships. What exactly do you instrument?**
> **Hints:** Golden signals per endpoint (rate, errors, duration histograms — p50/p95/p99, not averages) + business metrics (payments initiated/succeeded/failed by reason — business metrics often alert before infra ones); structured logs with correlation + payment IDs (PII-scrubbed); traces across gateway→service→PSP; dashboards by user journey; alerts: SLO burn rate + a few leading indicators (queue depth, PSP latency); runbook per alert *before* launch.

**Q3. Tell me about a root cause analysis where the first "root cause" turned out to be wrong or incomplete.**
> **Hints:** Looking for: 5-whys beyond the surface (bug → why not caught → why testable gap → why incentive/process gap), distinguishing trigger vs cause vs contributing factors, resisting single-cause narratives in complex systems; honesty about their own wrong first hypothesis; follow-through: what systemic change resulted and did it stick; red flag: stories that end at "developer made a mistake, we told them to be careful".

**Q4. Your service depends on a flaky third-party API you can't fix. How do you support this sustainably?**
> **Hints:** Defensive: timeouts, retries with budget, circuit breaker, cached/stale-if-error responses, queue-and-retry for writes, graceful degradation in UX; separate *their* failures in your SLO math (and dashboards) so your error budget isn't consumed by them invisibly; synthetic monitoring of their endpoints to alert before customers; vendor escalation path + status page subscription; document the failure modes in the runbook so on-call doesn't rediscover monthly.

**Q5. How do you reduce on-call load for a team that's getting paged nightly?**
> **Hints:** Audit pages: actionable? urgent? → delete/downgrade non-actionable alerts (ticket vs page), fix top recurring causes (Pareto — track "pages per cause"), auto-remediation for known-safe responses (restart, failover), improve alert thresholds (burn rate vs static), invest in the top toil items each sprint with explicit capacity; track pager metrics over time and make them visible to leadership; sustainable rotations (follow-the-sun, minimum team size).

---

## 14. Live Coding Questions (12)

**LC1. Flatten and deduplicate: given `List<List<Integer>>`, return sorted distinct values — then do it without streams.**
> **Hints:** `list.stream().flatMap(List::stream).distinct().sorted().toList()`; loop version with `TreeSet` (dedupe + sort in one structure); discuss complexity and which reads better; nulls in inner lists?

**LC2. Implement `groupAnagrams(List<String> words)` → groups of words that are anagrams.**
> **Hints:** Key = sorted chars of word (or char-count array as string key); `groupingBy` with that key function; complexity O(n·k log k) vs counting O(n·k); Unicode caveats; follow-up: case/whitespace normalization.

**LC3. Producer-consumer with `wait`/`notifyAll` from scratch (fixed-size buffer, multiple producers and consumers).**
> **Hints:** `synchronized` on shared monitor, `while (full) wait()` — must be while not if; `notifyAll` vs `notify` (two wait conditions on one monitor → notify can wake wrong side); clean shutdown; then ask them to redo it with `ArrayBlockingQueue` in 3 lines to show they know the right production tool.

**LC4. Given a list of transactions (id, timestamp, amount), find any sliding 60-second window where total exceeds a threshold.**
> **Hints:** Sort by timestamp, two-pointer window: advance right adding, shrink left while span > 60s, check running sum — O(n log n) for sort, O(n) scan; edge: exact boundary inclusion; follow-up: streaming version (deque), relate to rate limiting/fraud detection.

**LC5. Implement a generic `Result<T, E>` type (success/failure) with `map`, `flatMap`, `orElse` — no exceptions for control flow.**
> **Hints:** Sealed interface + records (`Ok<T,E>`, `Err<T,E>`); `map` transforms success only, `flatMap` chains fallible ops, pattern-matching switch for consumption; tests generics fluency + modern Java; compare with `Optional`/exceptions honestly.

**LC6. Parse a log file line format `2026-01-15T10:23:45Z level=ERROR service=orders msg="timeout calling payments"` — return error counts per service, top 3.**
> **Hints:** Careful splitting vs regex with named groups (quoted values with spaces!); stream over `Files.lines`, filter ERROR, group/count, sort top 3; robustness to malformed lines (counter, not crash); follow-up: multi-GB file, parallel processing trade-offs.

**LC7. Implement `Debouncer.call(key, runnable, delay)` — runs the action only if no new call with the same key arrives within the delay.**
> **Hints:** `ConcurrentHashMap<K, ScheduledFuture>` + `ScheduledExecutorService`; on call: cancel previous future for key, schedule new; races between cancel and execution (check `cancel` return / remove-on-run); shutdown handling; relate to real uses (search-as-you-type, config reload coalescing).

**LC8. Two sorted arrays — merge them; then find the k-th smallest element of the union without full merge.**
> **Hints:** Merge: two-pointer O(n+m); k-th: binary search partitioning O(log(min(n,m))) — acceptable if they get the pointer version O(k) first and reason toward better; edge cases: duplicates, k out of range, empty arrays.

**LC9. Refactor this (present snippet): a 60-line method with nested ifs mixing validation, business logic, DB calls, and logging.**
> **Hints:** Looking for: guard clauses to flatten nesting, extract methods with intention-revealing names, separate validation → domain → persistence layers, early returns, replace flag arguments; how they narrate trade-offs and keep behavior identical (tests first!); this reveals more about seniority than algorithms do.

**LC10. Implement a simple pub-sub `EventBus` in memory: `subscribe(Class<T>, Consumer<T>)`, `publish(event)` — thread-safe, with polymorphic dispatch as a follow-up.**
> **Hints:** `ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Consumer<?>>>` (read-mostly justification); type-safety at the API boundary with generics + unchecked cast contained in one place; polymorphic dispatch: walk `getSuperclass()`/interfaces; error isolation (one bad subscriber must not break others); sync vs async delivery.

**LC11. Write JUnit tests for a `PriceCalculator.calculate(order)` with tiered discounts — show how you'd design the test suite.**
> **Hints:** Looking for: parameterized tests (`@ParameterizedTest` + `@CsvSource`) for tiers and boundary values (exactly at threshold!), BigDecimal comparison via `compareTo` not `equals` (scale!), naming that documents behavior, edge cases (empty order, negative qty rejected), AssertJ fluency; mocks only where there's a real collaborator — over-mocking is a red flag.

**LC12. SQL live exercise: `orders(id, customer_id, status, amount, created_at)` — write (a) running total of daily revenue for last 30 days, (b) each customer's most recent order, (c) find customers with no orders in the last 90 days (given `customers` table).**
> **Hints:** (a) daily `GROUP BY date_trunc` CTE + `SUM(...) OVER (ORDER BY day)`; (b) `ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY created_at DESC) = 1` or `DISTINCT ON` (Postgres); (c) `LEFT JOIN ... ON o.created_at > now() - interval '90 days' WHERE o.id IS NULL` or `NOT EXISTS` — and why `NOT IN` with nullable subquery is a trap.

---
