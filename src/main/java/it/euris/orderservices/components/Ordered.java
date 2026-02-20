package it.euris.orderservices.components;

import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.entities.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class Ordered implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ORDERED;
    }

    @Override
    public void delivered(OrderEntity order) {

        throw new IllegalStateException("Cannot delivered unordered order");

    }

    @Override
    public void cancelled(OrderEntity order) {
        order.changeStatus(OrderStatus.CANCELED);

    }
}
