package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id;

    public String email;
    public String password;
    public String name;
    public String role; // "buyer" or "seller"
    public String profileImage;
    public double latitude;
    public double longitude;
    public String location;
    public long createdAt;

    // Stats vendeur
    public int totalSales;
    public double totalRevenue;
    public float sellerRating;
    public int sellerRatingCount;

    public UserEntity() {
        this.createdAt = System.currentTimeMillis();
        this.totalSales = 0;
        this.totalRevenue = 0;
        this.sellerRating = 0;
        this.sellerRatingCount = 0;
    }
}
