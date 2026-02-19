package it.euris.orderservices.entities;

import jakarta.persistence.*;
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

    @ElementCollection
    private List<String> productIds;

    private BigDecimal totalPrice;
}
