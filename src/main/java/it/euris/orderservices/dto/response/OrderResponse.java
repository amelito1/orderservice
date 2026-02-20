package it.euris.orderservices.dto.response;

import it.euris.orderservices.constants.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        @NotNull long orderId,
        @NotNull long customer,
        @NotNull OrderStatus orderStatus,
        List<ProductOrderedResponse> orderedProducts,
        @NotNull BigDecimal totalPrice) {
}