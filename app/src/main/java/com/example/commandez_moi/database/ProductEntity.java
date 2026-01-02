package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public double price;
    public double originalPrice;
    public String category;
    public String description;
    public String imageUrl;
    public String additionalImages; // JSON array of image URLs
    public String sellerId;
    public String sellerName;
    public String condition;
    public double latitude;
    public double longitude;
    public String location;
    public float rating;
    public int ratingCount;
    public long createdAt;
    public boolean isFavorite;

    public ProductEntity() {
        this.createdAt = System.currentTimeMillis();
        this.rating = 0;
        this.ratingCount = 0;
        this.isFavorite = false;
    }
}
