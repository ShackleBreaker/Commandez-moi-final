package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productId;
    public String oderId;
    public String reviewerName;
    public String reviewerImage;
    public float rating;
    public String comment;
    public long createdAt;

    public ReviewEntity() {
        this.createdAt = System.currentTimeMillis();
    }
}
