package it.euris.orderservices.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.euris.orderservices.constants.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
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


    private BigDecimal totalPrice;

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderedProductEntity> orderedProduct = new ArrayList<>();

    public void changeStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
    }
}
