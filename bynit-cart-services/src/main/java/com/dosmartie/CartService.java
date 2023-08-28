package com.dosmartie;

import com.dosmartie.request.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

public interface CartService {
    ResponseEntity<?> addToCart(CartRequest cartRequest);
    BaseResponse<Object> clearCart();
    BaseResponse<List<ProductResponse>> clearCartItems(String guestId);
    BaseResponse<?> deleteItem(String guestId, String productName);
    BaseResponse<List<ProductResponse>> viewCartItems(String guestId);


}
