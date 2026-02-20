package it.euris.orderservices.components;

import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStateFactory {

    private final Map<OrderStatus, OrderState> states = new EnumMap<>(OrderStatus.class);

    public OrderStateFactory(List<OrderState> stateList) {
        for (OrderState state : stateList) {
            states.put(state.getStatus(), state);
        }
    }

    public OrderState getState(OrderStatus status) {
        OrderState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No state found for " + status);
        }
        return state;
    }

}
