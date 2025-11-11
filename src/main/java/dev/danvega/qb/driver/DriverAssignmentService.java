package dev.danvega.qb.driver;

import dev.danvega.qb.order.Order;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DriverAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(DriverAssignmentService.class);
    private final List<Driver> availableDrivers = new ArrayList<>();
    private final RetryTemplate retryTemplate;
    private final Random random = new Random();
    private final DriverRetryListener driverRetryListener;

    public DriverAssignmentService(DriverRetryListener driverRetryListener) {
        this.driverRetryListener = driverRetryListener;

        // Configure RetryTemplate programmatically
        // This is useful when you need more dynamic control over retry behavior
        // new RetryTemplate() :: Implicitly uses RetryPolicy.withDefaults()
        // retryTemplate = new RetryTemplate();
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(10)
                .delay(Duration.ofMillis(2000))
                .multiplier(1.5)
                .maxDelay(Duration.ofMillis(10000))
                .includes(NoDriversAvailableException.class)
                .build();

        // new RetryTemplate() :: Implicitly uses RetryPolicy.withDefaults()
        retryTemplate = new RetryTemplate(retryPolicy);
        retryTemplate.setRetryListener(driverRetryListener);
    }

    public Driver assignDriver(Order order) throws RetryException {
        // log.info("🚗 Attempting to assign driver for order {}", order.id());

        // Use AtomicInteger to track attempts in the lambda
        final AtomicInteger attempt = new AtomicInteger(0);

        return retryTemplate.execute(() -> {
            int currentAttempt = attempt.incrementAndGet();
            //log.info("  Attempt #{} to find available driver", currentAttempt);

            // Simulate random driver availability (50% chance of success)
            if (random.nextDouble() > 0.5 || availableDrivers.isEmpty()) {
                throw new NoDriversAvailableException("No drivers available in area. Will retry...");
            }

            // Assign a random available driver
            Driver assignedDriver = availableDrivers.get(
                    random.nextInt(availableDrivers.size())
            );

            //log.info("✅ Driver {} assigned to order {}", assignedDriver.name(), order.id());

            return assignedDriver;
        });

    }

    @PostConstruct
    private void initializeDrivers() {
        availableDrivers.addAll(List.of(
                new Driver("1", "Alex Johnson", 4.8),
                new Driver("2", "Maria Garcia", 4.9),
                new Driver("3", "James Wilson", 4.5),
                new Driver("4", "Sarah Chen", 4.7),
                new Driver("5", "Mike Roberts", 4.6)
        ));
    }
}
