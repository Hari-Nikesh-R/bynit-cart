package com.dosmartie.request.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
    @NotNull(message = "itemSku must not be null")
    private String sku;
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must not be negative or zero")
    private Integer quantity;
}
