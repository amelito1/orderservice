package it.euris.orderservices.entities;

import it.euris.orderservices.constants.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "orders")
@Setter
@Getter
public class OrderEntity {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(updatable = false)
    private long id;

    private Long customerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ElementCollection
    private List<String> productIds;

    private BigDecimal totalPrice;

    public void changeStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }
}
