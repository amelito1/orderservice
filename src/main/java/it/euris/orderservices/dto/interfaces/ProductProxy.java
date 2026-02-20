package it.euris.orderservices.dto.interfaces;

import it.euris.orderservices.dto.request.OrderedProduct;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductProxy {

    @PostMapping(path = "/api/product/retrieve-ordered-products", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ProductOrderedResponse> retrievesOrderedProducts(
            @RequestBody List<OrderedProduct>  orderedProductRequests
    );

    @PostMapping(path = "/api/product/retrieve-ordered-products-by-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductOrderedResponse> retrievesOrderedProductsById(
            @RequestBody List<String>  orderedProductRequests
    );
}
