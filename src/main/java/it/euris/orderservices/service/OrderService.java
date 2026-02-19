package it.euris.orderservices.service;
import it.euris.orderservices.dto.interfaces.ProductProxy;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.request.OrderedProduct;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.PartialTotalPrice;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

import static it.euris.orderservices.utilities.OrderUtilities.mapToResponseFromEntity;

@Service
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private ProductProxy productProxy;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    public OrderResponse createOrder(OrderRequest orderRequest) {

        final List<ProductOrderedResponse> orderedResponses = this.productProxy.retrievesOrderedProducts(( orderRequest.getOrderedProducts()));

        final BigDecimal totalPrice = this.calculateTotalPricePerProduct(orderRequest);


        final List<String> productsIds = this.retrieveProductIdsFromRequest(orderRequest);

        final OrderEntity orderEntity = new OrderEntity();

        orderEntity.setTotalPrice(totalPrice);
        orderEntity.setCustomerId(orderRequest.getCustomerId());
        orderEntity.setProductIds(productsIds);

        final OrderEntity savedOrder = this.orderRepository.save(orderEntity);

        return mapToResponseFromEntity(savedOrder, orderedResponses);
    }

    public List<OrderResponse> retrieveOrders() {

        final List<OrderEntity> orderEntities = this.orderRepository.findAll();

       return orderEntities.stream().map(
                order -> {
                    List<ProductOrderedResponse> products =this.productProxy
                            .retrievesOrderedProductsById(( order.getProductIds()));

                    return mapToResponseFromEntity(order, products);
                }
        ).toList();

    }

    private PartialTotalPrice calculateTotalPricePerProduct(OrderedProduct orderedProduct) {
        final BigDecimal partialTotal = orderedProduct
                .getProductUnitPrice()
                .multiply(BigDecimal.valueOf(orderedProduct.getProductQuantity()));

        return new PartialTotalPrice(orderedProduct.getProductId(), partialTotal);
    }

    private BigDecimal calculateTotalPricePerProduct(OrderRequest orderRequest) {
        return orderRequest
                .getOrderedProducts()
                .stream()
                .map(this::calculateTotalPricePerProduct)
                .reduce(BigDecimal.ZERO,
                        (sum, product) -> sum.add(product.totalPrice()),
                        BigDecimal::add);
    }

    private List<String> retrieveProductIdsFromRequest(OrderRequest orderRequest) {
        return orderRequest
                .getOrderedProducts()
                .stream().map(product -> product.getProductId().toString()).toList();
    }
}