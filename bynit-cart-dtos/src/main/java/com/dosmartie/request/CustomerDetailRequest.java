package com.dosmartie.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailRequest {
    @NotNull(message = "Name cannot be null")
    private String name;
    private String phoneNumber;

}
