package com.dosmartie.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CartOrderRequest {
    @Valid
    private CustomerDetailRequest orderedCustomerDetail;
    private String email;
}
