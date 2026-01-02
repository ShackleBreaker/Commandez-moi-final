package com.example.commandez_moi.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "favorites")
public class FavoriteEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String oderId;
    public int productId;
    public long createdAt;

    public FavoriteEntity() {
        this.createdAt = System.currentTimeMillis();
    }
}
