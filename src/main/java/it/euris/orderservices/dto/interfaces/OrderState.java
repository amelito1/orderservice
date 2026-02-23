package it.euris.orderservices.dto.interfaces;

import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.entities.OrderEntity;

public interface OrderState {

    OrderStatus getStatus();

    void  delivered(OrderEntity order);
    void  cancelled(OrderEntity order);
}
