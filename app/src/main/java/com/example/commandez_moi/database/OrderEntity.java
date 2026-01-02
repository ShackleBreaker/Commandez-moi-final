package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "orders")
public class OrderEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String buyerId;
    public String buyerName;
    public double total;
    public String status; // "En attente", "Confirmé", "Expédié", "Livré", "Annulé"
    public String itemsJson; // JSON array of cart items
    public long createdAt;
    public long updatedAt;
    public String trackingNumber;
    public String deliveryAddress;

    public OrderEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "En attente";
    }
}
