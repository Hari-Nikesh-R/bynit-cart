package com.dosmartie.controller;

import com.dosmartie.OrderService;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.CartOrderRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dosmartie.helper.Constants.AUTH_ID;


@RestController
@RequestMapping(value = Urls.ORDER)
public class PlaceOrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody @Valid CartOrderRequest orderRequest, @RequestHeader(AUTH_ID) String authId) {
        return orderService.placeOrder(orderRequest, authId);
    }

}
