package dev.danvega.qb.order;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public List<Order> findAll() {
        return orders.values().stream().toList();
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Order create(Order order) {
        orders.put(order.id(), order);
        return order;
    }

    public Optional<Order> update(String id, Order order) {
        if (!orders.containsKey(id)) {
            return Optional.empty();
        }
        Order updated = new Order(id, order.customerId(), order.restaurantId(),
                order.items(), order.totalAmount(), order.paymentId(),
                order.paymentConfirmation(), order.status());
        orders.put(id, updated);
        return Optional.of(updated);
    }
}
