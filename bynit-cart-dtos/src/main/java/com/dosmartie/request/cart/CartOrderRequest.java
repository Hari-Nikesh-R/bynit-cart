package com.dosmartie.request.cart;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CartOrderRequest {
    @Valid
    private CustomerDetailRequest orderedCustomerDetail;
    private String email;
}
