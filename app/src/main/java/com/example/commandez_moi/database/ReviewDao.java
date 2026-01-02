package com.example.commandez_moi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    List<ReviewEntity> getByProduct(int productId);

    @Query("SELECT AVG(rating) FROM reviews WHERE productId = :productId")
    float getAverageRating(int productId);

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    int getReviewCount(int productId);

    @Insert
    void insert(ReviewEntity review);

    @Delete
    void delete(ReviewEntity review);
}
