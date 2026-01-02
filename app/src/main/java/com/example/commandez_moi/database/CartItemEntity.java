package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "cart_items")
public class CartItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productId;
    public String title;
    public double price;
    public String imageUrl;
    public int quantity;
    public String sellerId;
    public String sellerStatus; // "En attente", "Confirmé", "Rejeté"
    public String userId; // Owner of the cart

    public CartItemEntity() {
        this.quantity = 1;
        this.sellerStatus = "En attente";
    }
}
