package it.euris.orderservices.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderedProduct {
   private Long productId;
   private Long productQuantity;
   private BigDecimal productUnitPrice;
}