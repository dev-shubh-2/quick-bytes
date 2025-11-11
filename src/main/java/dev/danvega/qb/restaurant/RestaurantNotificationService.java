package dev.danvega.qb.restaurant;

import dev.danvega.qb.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;

@Service
public class RestaurantNotificationService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantNotificationService.class);

    /**
     * Notify restaurant of new order - limited to 3 concurrent notifications.
     *
     * Without concurrency limit: During rush hour, 50 concurrent calls could overwhelm
     * the restaurant's notification system, causing crashes or dropped notifications.
     *
     * With @ConcurrencyLimit: Only 3 notifications sent at a time, others queued.
     *
     * Note: No try-catch-finally needed! Spring handles permit management automatically,
     * even if exceptions occur during method execution.
     */
    @ConcurrencyLimit(3)
    public void notifyRestaurant(Order order) {
        LocalTime start = LocalTime.now();
        log.info("📢 [CONCURRENT] Sending notification to restaurant for order {} (Thread: {})",
                order.id(), Thread.currentThread().getName());

        // Simulate notification taking time (network call, webhook, etc.)
        simulateDelay(Duration.ofSeconds(2));

        LocalTime end = LocalTime.now();
        log.info("✅ [CONCURRENT] Notification sent for order {} (took {}ms)",
                order.id(), Duration.between(start, end).toMillis());
    }

    private void simulateDelay(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
