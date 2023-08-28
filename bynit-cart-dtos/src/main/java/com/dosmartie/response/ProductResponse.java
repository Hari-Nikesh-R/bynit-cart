package com.dosmartie.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ProductResponse implements Serializable {
    @Pattern(regexp = "^[a-zA-Z_ ]*$", message = "Invalid brand")
    private String brand;
    @Pattern(regexp = "^[a-zA-Z_ ]*$", message = "Invalid ProductName")
    private String name;
    @NotNull(message = "itemSku must not be null")
    private String sku;
    private String defaultSku;
    private String color;
    private String size;
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must not be negative or zero")
    private Integer quantity;
    private String category;
    @NotNull(message = "Price must not be null")
    @Positive(message = "Price cannot be negative")
    private double price;
}
