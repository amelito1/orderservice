package it.euris.orderservices.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract  class  CommonOrderedProduct {
    private Long productId;
    private Double productQuantity;


}
