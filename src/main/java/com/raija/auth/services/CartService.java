package com.raija.auth.services;

import com.raija.auth.dtos.AddToCartRequest;
import com.raija.auth.dtos.CartDto;

public interface CartService {
    CartDto getCart(String userId);

    CartDto addToCart(String userId, AddToCartRequest request);

    CartDto updateItem(String userId, AddToCartRequest request);

    CartDto removeItem(String userId, String productId);

    void clearCart(String userId);
}
