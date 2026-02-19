package it.euris.orderservices.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "api/order" )
@Tag(name = "Order")
public class OrderController {

    @Autowired
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(path = "/create-order", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest orderRequest
    ) {

        final  OrderResponse order = this.orderService.createOrder(orderRequest);

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping(path = "/retrieve-orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponse>> retrieves() {
        final List<OrderResponse> orders = this.orderService.retrieveOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping(path = "/retrieve-orders-pages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<OrderResponse>> retrieveOrdersPages(@RequestParam int page,
                                                                   @RequestParam int size) {
       final Page<OrderResponse> pages = this.orderService.retrievesOrderPages(page, size);

       return ResponseEntity.ok(pages);
    }

}