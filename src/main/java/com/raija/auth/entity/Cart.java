package com.raija.auth.entity;


import com.raija.auth.dtos.CartItemDto;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Cart(Long id, String userId, List<CartItem> items) {
        this.id = id;
        this.userId = userId;
        this.items = items;
    }

    public Cart() {
    }
}