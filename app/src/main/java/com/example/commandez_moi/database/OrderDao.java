package com.example.commandez_moi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    List<OrderEntity> getAll();

    @Query("SELECT * FROM orders WHERE id = :id")
    OrderEntity getById(String id);

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    List<OrderEntity> getByBuyer(String buyerId);

    @Query("SELECT * FROM orders WHERE itemsJson LIKE '%\"sellerId\":\"' || :sellerId || '\"%' ORDER BY createdAt DESC")
    List<OrderEntity> getBySeller(String sellerId);

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    List<OrderEntity> getByStatus(String status);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OrderEntity order);

    @Update
    void update(OrderEntity order);

    @Delete
    void delete(OrderEntity order);

    @Query("UPDATE orders SET status = :status, updatedAt = :timestamp WHERE id = :orderId")
    void updateStatus(String orderId, String status, long timestamp);

    @Query("UPDATE orders SET trackingNumber = :tracking WHERE id = :orderId")
    void updateTracking(String orderId, String tracking);
}
