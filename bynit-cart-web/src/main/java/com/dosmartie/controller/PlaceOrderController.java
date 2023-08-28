package com.dosmartie.controller;

import com.dosmartie.OrderService;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.CartOrderRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = Urls.ORDER)
public class PlaceOrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody @Valid CartOrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }

}
