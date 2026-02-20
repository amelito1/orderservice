package it.euris.orderservices.dto.response;

import it.euris.orderservices.constants.OrderStatus;

public record OrderChangeStateResponse(Long orderId, OrderStatus orderStatus) {
}
