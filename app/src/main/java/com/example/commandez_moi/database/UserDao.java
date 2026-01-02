package com.example.commandez_moi.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(String id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity login(String email, String password);

    @Query("SELECT * FROM users WHERE role = 'seller' ORDER BY sellerRating DESC")
    List<UserEntity> getTopSellers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("UPDATE users SET profileImage = :imageUrl WHERE id = :userId")
    void updateProfileImage(String userId, String imageUrl);

    @Query("UPDATE users SET totalSales = totalSales + 1, totalRevenue = totalRevenue + :amount WHERE id = :sellerId")
    void addSale(String sellerId, double amount);

    @Query("UPDATE users SET sellerRating = :rating, sellerRatingCount = :count WHERE id = :sellerId")
    void updateSellerRating(String sellerId, float rating, int count);

    @Query("UPDATE users SET latitude = :lat, longitude = :lng, location = :location WHERE id = :userId")
    void updateLocation(String userId, double lat, double lng, String location);
}
