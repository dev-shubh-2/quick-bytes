package dev.danvega.qb.restaurant;

import dev.danvega.qb.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);
    private final RestaurantService restaurantService;
    private final RestaurantNotificationService restaurantNotificationService;

    public RestaurantController(RestaurantService restaurantService, RestaurantNotificationService restaurantNotificationService) {
        this.restaurantService = restaurantService;
        this.restaurantNotificationService = restaurantNotificationService;
    }

    @GetMapping("/")
    public List<Restaurant> findAllRestaurants() {
        return restaurantService.findAll();
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<Map<String, Object>> getRestaurantMenu(@PathVariable String restaurantId) {
        log.info("🍽️  API request: Get menu for restaurant {}", restaurantId);

        try {
            List<MenuItem> menu = restaurantService.getMenuFromPartner(restaurantId);

            return ResponseEntity.ok(Map.of(
                    "restaurantId", restaurantId,
                    "menuItems", menu,
                    "count", menu.size(),
                    "message", "Menu fetched successfully (possibly after retries)"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to fetch menu after all retries: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Service temporarily unavailable",
                    "message", e.getMessage(),
                    "restaurantId", restaurantId
            ));
        }
    }

    @GetMapping("/lunch-rush")
    public ResponseEntity<Map<String, Object>> lunchRush() {
        log.info("🍔 LUNCH RUSH STARTED - Simulating 10 concurrent order notifications");
        log.info("⚠️  Concurrency Limit: 3 (only 3 notifications can process simultaneously)");

        LocalDateTime startTime = LocalDateTime.now();

        // Create 10 orders to simulate lunch rush
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Order order = new Order(
                String.format("lunch-%04d", i),
                "customer-" + i,
                "restaurant-001",
                List.of("burger", "fries", "drink"),
                new BigDecimal("15.99"),
                "payment-" + i,
                "confirmed-" + i,
                Order.OrderStatus.CONFIRMED
            );
            orders.add(order);
        }

        // Use fixed thread pool with 10 threads to submit all orders concurrently
        ExecutorService executor = Executors.newFixedThreadPool(10);

        log.info("📤 Submitting all 10 orders to thread pool...");

        // Submit all notification tasks concurrently
        for (Order order : orders) {
            executor.submit(() -> {
                try {
                    restaurantNotificationService.notifyRestaurant(order);
                } catch (Exception e) {
                    log.error("❌ Error notifying restaurant for order {}: {}", order.id(), e.getMessage());
                }
            });
        }

        // Shutdown executor and wait for all tasks to complete
        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(2, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("⚠️  Some notifications did not complete within 2 minutes");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("❌ Thread pool interrupted: {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = Duration.between(startTime, endTime).toSeconds();

        log.info("\uD83C\uDF89 LUNCH RUSH COMPLETED - All 10 notifications processed in {} seconds", durationSeconds);
        log.info("📊 Expected time: ~6-8 seconds (10 orders / 3 concurrent * 2s each)");

        return ResponseEntity.ok(Map.of(
            "message", "Lunch rush simulation completed",
            "totalOrders", 10,
            "concurrencyLimit", 3,
            "durationSeconds", durationSeconds,
            "expectedDuration", "6-8 seconds",
            "threadPoolType", "Fixed thread pool (10 threads)",
            "explanation", "With @ConcurrencyLimit(3), only 3 notifications process simultaneously. " +
                          "The remaining 7 orders queue and wait for permits to become available."
        ));
    }

    @GetMapping("/lunch-rush-virtual")
    public ResponseEntity<Map<String, Object>> lunchRushVirtual() {
        log.info("🍔 LUNCH RUSH STARTED (Virtual Threads) - Simulating 10 concurrent order notifications");
        log.info("⚠️  Concurrency Limit: 3 (only 3 notifications can process simultaneously)");

        LocalDateTime startTime = LocalDateTime.now();

        // Create 10 orders to simulate lunch rush
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Order order = new Order(
                String.format("lunch-v-%04d", i),
                "customer-" + i,
                "restaurant-001",
                List.of("burger", "fries", "drink"),
                new BigDecimal("15.99"),
                "payment-" + i,
                "confirmed-" + i,
                Order.OrderStatus.CONFIRMED
            );
            orders.add(order);
        }

        // Use virtual threads (Java 21+) - lightweight, created on-demand
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        log.info("📤 Submitting all 10 orders to virtual thread executor...");

        // Submit all notification tasks concurrently
        for (Order order : orders) {
            executor.submit(() -> {
                try {
                    restaurantNotificationService.notifyRestaurant(order);
                } catch (Exception e) {
                    log.error("❌ Error notifying restaurant for order {}: {}", order.id(), e.getMessage());
                }
            });
        }

        // Shutdown executor and wait for all tasks to complete
        executor.shutdown();
        try {
            boolean finished = executor.awaitTermination(2, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("⚠️  Some notifications did not complete within 2 minutes");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("❌ Thread pool interrupted: {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = Duration.between(startTime, endTime).toSeconds();

        log.info("🎉 LUNCH RUSH COMPLETED (Virtual Threads) - All 10 notifications processed in {} seconds", durationSeconds);
        log.info("📊 Expected time: ~6-8 seconds (10 orders / 3 concurrent * 2s each)");

        return ResponseEntity.ok(Map.of(
            "message", "Lunch rush simulation completed (Virtual Threads)",
            "totalOrders", 10,
            "concurrencyLimit", 3,
            "durationSeconds", durationSeconds,
            "expectedDuration", "6-8 seconds",
            "threadPoolType", "Virtual threads (Java 21+)",
            "explanation", "With @ConcurrencyLimit(3), only 3 notifications process simultaneously. " +
                          "Virtual threads are lightweight and created on-demand, but the concurrency limit still applies. " +
                          "Check logs to see VirtualThread names vs platform thread pool names."
        ));
    }
}
