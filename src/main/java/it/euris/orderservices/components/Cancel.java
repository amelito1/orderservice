package it.euris.orderservices.components;

import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.entities.OrderEntity;

@Deprecated
public class Cancel implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELED;
    }

    @Override
    public void delivered(OrderEntity order) {

        throw new IllegalStateException("Cannot deliver a cancelled order");

    }

    @Override
    public void cancelled(OrderEntity order) {

        throw new IllegalStateException("Order is already cancelled");

    }
}
