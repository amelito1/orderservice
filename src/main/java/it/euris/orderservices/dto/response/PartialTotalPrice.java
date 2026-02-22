package it.euris.orderservices.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PartialTotalPrice(@NotNull  long productId, @NotNull BigDecimal partialPrice) {
}