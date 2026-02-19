package it.euris.orderservices.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        @NotNull long orderId,
        @NotNull long customer,
        List<ProductOrderedResponse> orderedProducts,
        @NotNull BigDecimal totalPrice) {
}