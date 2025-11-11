# QuickBites

Demo application showcasing Spring Framework 7's native resilience features: 
`@Retryable`, `RetryTemplate`, `@ConcurrencyLimit`, and `RetryListener`.

## Quick Start

```bash
./mvnw spring-boot:run
```

The application starts on port 8080 with test data loaded from JSON files.

**Testing:** Open `qb.http` in your IDE to run HTTP requests demonstrating each resilience feature.

## Features

### Data Loading (Jackson 3)

`restaurant/DataLoader.java` - Loads restaurant and menu data on startup using Jackson 3's `JsonMapper` (new API in Spring Boot 4). Data files are in `src/main/resources/data/`.

### @Retryable - Declarative Retry

`restaurant/RestaurantService.java` - Handles flaky restaurant partner API (simulated 40% failure rate) with automatic retries and exponential backoff (1s → 2s → 4s). Success rate improves from 60% (no retries) to 95%+ with retries enabled.

### RetryTemplate - Programmatic Retry

`driver/DriverAssignmentService.java` - Assigns drivers with custom retry logic (10 attempts, exponential backoff). Simulates 50% driver availability. Use `RetryTemplate` when you need more control than `@Retryable` provides, or when you need retry listeners for observability.

### RetryListener - Retry Observability

`driver/DriverRetryListener.java` - Hooks into the retry lifecycle to track metrics and log detailed retry behavior. Provides thread-safe counters for monitoring.

Attached to `RetryTemplate` via `setRetryListener()` for production observability without polluting business logic.

### @ConcurrencyLimit - Resource Protection

`restaurant/RestaurantNotificationService.java` - Limits concurrent notifications to 3 to prevent overwhelming the restaurant notification system. Two lunch-rush endpoints demonstrate this with platform threads (`/lunch-rush`) and virtual threads (`/lunch-rush-virtual`). Both simulate 10 concurrent orders with expected duration of ~6-8 seconds. The `@ConcurrencyLimit(3)` works identically with both thread types - logs show exactly 3 notifications processing at any given time regardless of the underlying thread implementation.

## Configuration

Enable resilience features in `QuickBytesApplication.java`:

```java
@SpringBootApplication
@EnableResilientMethods  // Enables @Retryable and @ConcurrencyLimit
public class QuickBytesApplication { }
```

## When to Use What

| Pattern | Use Case |
|---------|----------|
| `@Retryable` | Simple retries for transient failures (API calls, network timeouts) |
| `RetryTemplate` | Complex workflows, custom logic, or when you need retry listeners |
| `@ConcurrencyLimit` | Protecting downstream systems from overload |
| `RetryListener` | Production observability, metrics, debugging retry behavior |

## Resources

- [Spring Framework 7 Resilience Docs](https://docs.spring.io/spring-framework/reference/7.0/core/resilience.html)
- [Spring Blog: Core Resilience Features](https://spring.io/blog/2025/09/09/core-spring-resilience-features)