package com.example.commandez_moi.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        ProductEntity.class,
        UserEntity.class,
        OrderEntity.class,
        CartItemEntity.class,
        FavoriteEntity.class,
        ReviewEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ProductDao productDao();

    public abstract UserDao userDao();

    public abstract OrderDao orderDao();

    public abstract CartDao cartDao();

    public abstract ReviewDao reviewDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "commandezmoi_db")
                            .allowMainThreadQueries() // Pour simplifier - en prod utiliser des coroutines/threads
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
