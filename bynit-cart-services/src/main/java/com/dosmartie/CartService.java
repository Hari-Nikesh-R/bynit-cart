package com.dosmartie;

import com.dosmartie.request.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

public interface CartService {
    ResponseEntity<?> addToCart(CartRequest cartRequest, String authId);
    BaseResponse<Object> clearCart(String email, String authId);
    BaseResponse<List<ProductResponse>> clearCartItems(String userEmail);
    BaseResponse<?> deleteItem(String userEmail, String itemSku, String authId);
    BaseResponse<List<ProductResponse>> viewCartItems(String userEmail);


}
