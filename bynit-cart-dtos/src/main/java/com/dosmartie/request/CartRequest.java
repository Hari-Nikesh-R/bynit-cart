package com.dosmartie.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartRequest {
    @NotNull(message = "email must not be null")
    private String userEmail;
    @NotNull(message = "cartProduct is mandatory")
    @Valid
    private ProductRequest cartProduct;
}
