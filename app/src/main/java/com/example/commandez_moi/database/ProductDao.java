package com.example.commandez_moi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    List<ProductEntity> getAll();

    @Query("SELECT * FROM products WHERE id = :id")
    ProductEntity getById(int id);

    @Query("SELECT * FROM products WHERE category = :category ORDER BY createdAt DESC")
    List<ProductEntity> getByCategory(String category);

    @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    List<ProductEntity> getBySeller(String sellerId);

    @Query("SELECT * FROM products WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<ProductEntity> search(String query);

    @Query("SELECT * FROM products WHERE " +
            "(latitude BETWEEN :minLat AND :maxLat) AND " +
            "(longitude BETWEEN :minLng AND :maxLng) " +
            "ORDER BY createdAt DESC")
    List<ProductEntity> getNearby(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY createdAt DESC")
    List<ProductEntity> getFavorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProductEntity product);

    @Update
    void update(ProductEntity product);

    @Delete
    void delete(ProductEntity product);

    @Query("DELETE FROM products WHERE id = :id")
    void deleteById(int id);

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
    void setFavorite(int id, boolean isFavorite);

    @Query("UPDATE products SET rating = :rating, ratingCount = :count WHERE id = :id")
    void updateRating(int id, float rating, int count);
}
