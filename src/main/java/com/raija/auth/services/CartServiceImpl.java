package com.raija.auth.services;



import com.raija.auth.dtos.AddToCartRequest;
import com.raija.auth.dtos.CartDto;
import com.raija.auth.dtos.CartItemDto;
import com.raija.auth.entity.Cart;
import com.raija.auth.entity.CartItem;
import com.raija.auth.exception.BadRequestException;
import com.raija.auth.exception.ResourceNotFoundException;
import com.raija.auth.repos.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
//@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public CartDto getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToDto(cart);
    }

    @Override
    public CartDto addToCart(String userId, AddToCartRequest request) {

        Cart cart = getOrCreateCart(userId);

        CartItem existingItem = findItem(cart, request.getProductId());

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem(
                    request.getProductId(),
                    fetchProductName(request.getProductId()),
                    request.getQuantity(),
                    fetchProductPrice(request.getProductId())
            );

            cart.addItem(item);
        }
        return mapToDto(cartRepository.save(cart));
    }

    @Override
    public CartDto updateItem(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, request.getProductId());
        if (item == null) {
            throw new ResourceNotFoundException("Item not found in cart");
        }
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        item.setQuantity(request.getQuantity());

        return mapToDto(cartRepository.save(cart));
    }

    @Override
    public CartDto removeItem(String userId, String productId) {

        Cart cart = getOrCreateCart(userId);

        CartItem item = findItem(cart, productId);

        if (item == null) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        cart.removeItem(item);

        return mapToDto(cartRepository.save(cart));
    }

    @Override
    public void clearCart(String userId) {

        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    private CartItem findItem(Cart cart, String productId) {
        return cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private CartDto mapToDto(Cart cart) {
        List<CartItemDto> itemDtos = new ArrayList<>();
        double total = 0.0;
        for (CartItem item : cart.getItems()) {
            double subtotal = item.getSubtotal().doubleValue();
            total += subtotal;
            CartItemDto dto = new CartItemDto();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getPrice().doubleValue());
            dto.setSubtotal(subtotal);
            itemDtos.add(dto);
        }
        CartDto dto = new CartDto();
        dto.setUserId(cart.getUserId());
        dto.setItems(itemDtos);
        dto.setTotalAmount(total);
        return dto;
    }

    private String fetchProductName(String productId) {
        return "SKU-" + productId;
    }

    private BigDecimal fetchProductPrice(String productId) {
        return BigDecimal.valueOf(100);
    }
}
