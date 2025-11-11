package dev.danvega.qb.restaurant;

import java.math.BigDecimal;

public record MenuItem(
        String id,
        String restaurantId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available
) {}
