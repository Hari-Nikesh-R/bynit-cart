package com.dosmartie.controller;

import com.dosmartie.CartService;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = Urls.CART)
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping(value = Urls.ADD_PRODUCTS)
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartRequest cartRequest) {
        return cartService.addToCart(cartRequest);
    }

    @DeleteMapping
    public BaseResponse<Object> clearCart() {
        return cartService.clearCart();
    }

    @DeleteMapping(value = Urls.DELETE_ITEM + "/{itemSku}")
    public BaseResponse<?> deleteCartItem(@RequestParam("email") String email, @PathVariable("itemSku") String itemSku) {
        return cartService.deleteItem(email, itemSku);
    }

    @DeleteMapping(value = Urls.CLEAR_CART)
    public BaseResponse<List<ProductResponse>> clearCartItem(@RequestParam("email") String email) {
        return cartService.clearCartItems(email);
    }

    @GetMapping(value = Urls.VIEW)
    public BaseResponse<List<ProductResponse>> getCartItems(@RequestParam("email") String email) {
        return cartService.viewCartItems(email);
    }
}
