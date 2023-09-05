package com.dosmartie.request.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailRequest {
    private String name;
    private String phoneNumber;
    private AddressRequest address;
}
