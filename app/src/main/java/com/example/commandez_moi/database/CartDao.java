package com.example.commandez_moi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    List<CartItemEntity> getByUser(String userId);

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    CartItemEntity getByUserAndProduct(String userId, int productId);

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    int getCartCount(String userId);

    @Query("SELECT SUM(price * quantity) FROM cart_items WHERE userId = :userId")
    double getCartTotal(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItemEntity item);

    @Update
    void update(CartItemEntity item);

    @Delete
    void delete(CartItemEntity item);

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    void clearCart(String userId);

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    void updateQuantity(int id, int quantity);
}
