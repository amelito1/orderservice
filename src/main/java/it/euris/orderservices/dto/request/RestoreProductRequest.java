package it.euris.orderservices.dto.request;

import it.euris.orderservices.dto.CommonOrderedProduct;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestoreProductRequest extends CommonOrderedProduct {
    public RestoreProductRequest(Long productId, BigDecimal quantity) {
        super();
    }
}
