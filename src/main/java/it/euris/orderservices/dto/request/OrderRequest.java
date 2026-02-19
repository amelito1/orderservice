package it.euris.orderservices.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    @NotNull
    private long customerId;
    @NotNull
    private List<OrderedProduct> orderedProducts;
    
}