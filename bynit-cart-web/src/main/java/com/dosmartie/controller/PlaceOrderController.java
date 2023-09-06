package com.dosmartie.controller;

import com.dosmartie.OrderService;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.cart.CartOrderRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dosmartie.helper.Constants.AUTH_ID;


@RestController
@RequestMapping(value = Urls.CART + Urls.ORDER)
public class PlaceOrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody @Valid CartOrderRequest orderRequest, @RequestHeader("email") String email) {
        return orderService.placeOrder(orderRequest, email);
    }

}
