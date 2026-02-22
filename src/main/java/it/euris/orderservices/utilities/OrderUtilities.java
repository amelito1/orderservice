package it.euris.orderservices.utilities;


import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.entities.OrderedProductEntity;

import java.util.List;

public class OrderUtilities {

    public  static OrderResponse mapToResponseFromEntity(OrderEntity orderEntity, List<ProductOrderedResponse> products){
        return  new OrderResponse(
                orderEntity.getId(),
                orderEntity.getCustomerId(),
                orderEntity.getOrderStatus(),
                products,
                orderEntity.getTotalPrice()
        );
    }

    public  static  ProductOrderedResponse mapToOrderedProduct(OrderedProductEntity product) {
        return new ProductOrderedResponse(
                product.getProductId(),
                product.getProductName(),
                product.getQuantity(),
                product.getUnitPrice()
        );
    }
}
