package it.euris.orderservices.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductOrderedResponse(
        @NotNull long id,
        @NotNull String productName,
        @NotNull Double stock,
        BigDecimal unitPrice
) {
}
