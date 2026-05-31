package com.raija.auth.controller;



import com.raija.auth.dtos.AddToCartRequest;
import com.raija.auth.dtos.ApiResponse;
import com.raija.auth.dtos.CartDto;
import com.raija.auth.services.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
//@RequiredArgsConstructor
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartDto>> getCart(@PathVariable String userId) {
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Cart fetched successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cart cleared"));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {
        CartDto cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Item added to cart"));
    }

    @PutMapping("/{userId}/items")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {

        CartDto cart = cartService.updateItem(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Cart item updated"));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable String userId,
            @PathVariable String productId) {
        CartDto cart = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
