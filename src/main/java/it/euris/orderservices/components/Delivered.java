package it.euris.orderservices.components;

import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.entities.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class Delivered implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }

    @Override
    public void delivered(OrderEntity order) {
        order.changeStatus(OrderStatus.DELIVERED);

    }

    @Override
    public void cancelled(OrderEntity order) {
        throw new IllegalStateException("Cannot cancel unordered order or delivered order");
    }
}
