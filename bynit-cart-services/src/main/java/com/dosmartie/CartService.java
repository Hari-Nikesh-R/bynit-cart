package com.dosmartie;

import com.dosmartie.request.cart.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.cart.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CartService {
    ResponseEntity<?> addToCart(CartRequest cartRequest, String email);
    BaseResponse<Object> clearCart(String email);
    BaseResponse<List<ProductResponse>> clearCartItems(String userEmail);
    BaseResponse<?> deleteItem(String userEmail, String itemSku);
    BaseResponse<List<ProductResponse>> viewCartItems(String userEmail);


}
