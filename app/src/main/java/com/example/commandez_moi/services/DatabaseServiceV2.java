package com.example.commandez_moi.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.commandez_moi.database.*;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.models.Order;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de base de données amélioré utilisant Room (SQLite)
 * avec fallback sur SharedPreferences pour la session
 */
public class DatabaseServiceV2 {
    private static final String PREF_NAME = "CommandezMoiSession";
    private static final String KEY_CURRENT_USER = "current_user";

    private Context context;
    private AppDatabase db;
    private SharedPreferences sessionPrefs;
    private Gson gson;

    public DatabaseServiceV2(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.sessionPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        initializeMockData();
    }

    private void initializeMockData() {
        if (db.productDao().getAll().isEmpty()) {
            ProductEntity p1 = new ProductEntity();
            p1.title = "Écouteurs sans fil";
            p1.price = 29.99;
            p1.originalPrice = 49.99;
            p1.category = "Tech";
            p1.description = "Super son, autonomie 20h";
            p1.imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e";
            p1.sellerId = "seller1";
            p1.condition = "Neuf";
            p1.latitude = 48.8566;
            p1.longitude = 2.3522;
            p1.location = "Paris";
            db.productDao().insert(p1);

            ProductEntity p2 = new ProductEntity();
            p2.title = "Montre Connectée";
            p2.price = 45.00;
            p2.originalPrice = 89.00;
            p2.category = "Tech";
            p2.description = "GPS inclus, étanche";
            p2.imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30";
            p2.sellerId = "seller1";
            p2.condition = "Neuf";
            p2.latitude = 48.8566;
            p2.longitude = 2.3522;
            p2.location = "Paris";
            db.productDao().insert(p2);

            ProductEntity p3 = new ProductEntity();
            p3.title = "Sac à main cuir";
            p3.price = 65.00;
            p3.category = "Mode";
            p3.description = "Cuir véritable, fait main";
            p3.imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3";
            p3.sellerId = "seller1";
            p3.condition = "Neuf";
            p3.latitude = 45.7640;
            p3.longitude = 4.8357;
            p3.location = "Lyon";
            db.productDao().insert(p3);
        }

        if (db.userDao().getByEmail("demo@test.com") == null) {
            UserEntity seller = new UserEntity();
            seller.id = "seller1";
            seller.email = "vendeur@test.com";
            seller.password = "demo";
            seller.name = "Vendeur Demo";
            seller.role = "seller";
            seller.latitude = 48.8566;
            seller.longitude = 2.3522;
            seller.location = "Paris";
            db.userDao().insert(seller);

            UserEntity buyer = new UserEntity();
            buyer.id = "buyer1";
            buyer.email = "demo@test.com";
            buyer.password = "demo";
            buyer.name = "Acheteur Demo";
            buyer.role = "buyer";
            db.userDao().insert(buyer);
        }
    }

    // ============== UTILISATEURS ==============

    public User login(String email, String password) {
        UserEntity entity = db.userDao().login(email, password);
        if (entity != null) {
            User user = entityToUser(entity);
            setCurrentUser(user);
            return user;
        }
        return null;
    }

    public void setCurrentUser(User user) {
        String json = gson.toJson(user);
        sessionPrefs.edit().putString(KEY_CURRENT_USER, json).apply();
    }

    public User getCurrentUser() {
        String json = sessionPrefs.getString(KEY_CURRENT_USER, null);
        if (json == null)
            return null;
        return gson.fromJson(json, User.class);
    }

    public void logout() {
        sessionPrefs.edit().remove(KEY_CURRENT_USER).apply();
    }

    public void registerUser(User user) {
        UserEntity entity = userToEntity(user);
        db.userDao().insert(entity);
    }

    public boolean emailExists(String email) {
        return db.userDao().getByEmail(email) != null;
    }

    public void updateUserLocation(String oderId, double lat, double lng, String location) {
        db.userDao().updateLocation(oderId, lat, lng, location);
    }

    public void updateProfileImage(String oderId, String imageUrl) {
        db.userDao().updateProfileImage(oderId, imageUrl);
    }

    public User getUserById(String id) {
        UserEntity entity = db.userDao().getById(id);
        return entity != null ? entityToUser(entity) : null;
    }

    // ============== PRODUITS ==============

    public List<Product> getProducts() {
        List<ProductEntity> entities = db.productDao().getAll();
        return entitiesToProducts(entities);
    }

    public List<Product> getProductsByCategory(String category) {
        List<ProductEntity> entities = db.productDao().getByCategory(category);
        return entitiesToProducts(entities);
    }

    public List<Product> getProductsBySeller(String sellerId) {
        List<ProductEntity> entities = db.productDao().getBySeller(sellerId);
        return entitiesToProducts(entities);
    }

    public List<Product> searchProducts(String query) {
        List<ProductEntity> entities = db.productDao().search(query);
        return entitiesToProducts(entities);
    }

    public List<Product> getNearbyProducts(double lat, double lng, double radiusKm) {
        // Approximation: 1 degré ≈ 111 km
        double delta = radiusKm / 111.0;
        List<ProductEntity> entities = db.productDao().getNearby(
                lat - delta, lat + delta, lng - delta, lng + delta);
        return entitiesToProducts(entities);
    }

    public List<Product> getFavorites() {
        List<ProductEntity> entities = db.productDao().getFavorites();
        return entitiesToProducts(entities);
    }

    public Product getProductById(int id) {
        ProductEntity entity = db.productDao().getById(id);
        return entity != null ? entityToProduct(entity) : null;
    }

    public void addProduct(Product product) {
        ProductEntity entity = productToEntity(product);
        db.productDao().insert(entity);
    }

    public void updateProduct(Product product) {
        ProductEntity entity = productToEntity(product);
        entity.id = product.getId();
        db.productDao().update(entity);
    }

    public void deleteProduct(int productId) {
        db.productDao().deleteById(productId);
    }

    public void toggleFavorite(int productId) {
        ProductEntity entity = db.productDao().getById(productId);
        if (entity != null) {
            db.productDao().setFavorite(productId, !entity.isFavorite);
        }
    }

    public void setFavorite(int productId, boolean isFavorite) {
        db.productDao().setFavorite(productId, isFavorite);
    }

    // ============== PANIER ==============

    public List<CartItem> getCart() {
        User user = getCurrentUser();
        if (user == null)
            return new ArrayList<>();

        List<CartItemEntity> entities = db.cartDao().getByUser(user.getId());
        List<CartItem> items = new ArrayList<>();
        for (CartItemEntity e : entities) {
            CartItem item = new CartItem();
            item.setId(e.productId);
            item.setTitle(e.title);
            item.setPrice(e.price);
            item.setImageUrl(e.imageUrl);
            item.setQuantity(e.quantity);
            item.setSellerId(e.sellerId);
            item.setSellerStatus(e.sellerStatus);
            items.add(item);
        }
        return items;
    }

    public int getCartCount() {
        User user = getCurrentUser();
        if (user == null)
            return 0;
        return db.cartDao().getCartCount(user.getId());
    }

    public void addToCart(Product product) {
        User user = getCurrentUser();
        if (user == null)
            return;

        CartItemEntity existing = db.cartDao().getByUserAndProduct(user.getId(), product.getId());
        if (existing != null) {
            db.cartDao().updateQuantity(existing.id, existing.quantity + 1);
        } else {
            CartItemEntity item = new CartItemEntity();
            item.productId = product.getId();
            item.title = product.getTitle();
            item.price = product.getPrice();
            item.imageUrl = product.getImageUrl();
            item.sellerId = product.getSellerId();
            item.userId = user.getId();
            item.quantity = 1;
            item.sellerStatus = "En attente";
            db.cartDao().insert(item);
        }
    }

    public void updateCartItemQuantity(int productId, int quantity) {
        User user = getCurrentUser();
        if (user == null)
            return;

        CartItemEntity item = db.cartDao().getByUserAndProduct(user.getId(), productId);
        if (item != null) {
            if (quantity <= 0) {
                db.cartDao().delete(item);
            } else {
                db.cartDao().updateQuantity(item.id, quantity);
            }
        }
    }

    public void removeFromCart(int productId) {
        User user = getCurrentUser();
        if (user == null)
            return;

        CartItemEntity item = db.cartDao().getByUserAndProduct(user.getId(), productId);
        if (item != null) {
            db.cartDao().delete(item);
        }
    }

    public void clearCart() {
        User user = getCurrentUser();
        if (user == null)
            return;
        db.cartDao().clearCart(user.getId());
    }

    public void saveCart(List<CartItem> items) {
        User user = getCurrentUser();
        if (user == null)
            return;

        db.cartDao().clearCart(user.getId());
        for (CartItem item : items) {
            CartItemEntity entity = new CartItemEntity();
            entity.productId = item.getId();
            entity.title = item.getTitle();
            entity.price = item.getPrice();
            entity.imageUrl = item.getImageUrl();
            entity.sellerId = item.getSellerId();
            entity.userId = user.getId();
            entity.quantity = item.getQuantity();
            entity.sellerStatus = item.getSellerStatus();
            db.cartDao().insert(entity);
        }
    }

    // ============== COMMANDES ==============

    public List<Order> getOrders() {
        List<OrderEntity> entities = db.orderDao().getAll();
        return entitiesToOrders(entities);
    }

    public List<Order> getOrdersByBuyer(String buyerId) {
        List<OrderEntity> entities = db.orderDao().getByBuyer(buyerId);
        return entitiesToOrders(entities);
    }

    public List<Order> getOrdersBySeller(String sellerId) {
        List<OrderEntity> entities = db.orderDao().getBySeller(sellerId);
        return entitiesToOrders(entities);
    }

    public void saveOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.getId();
        entity.buyerId = order.getBuyerId();
        entity.total = order.getTotal();
        entity.status = order.getStatus();
        entity.itemsJson = gson.toJson(order.getItems());
        db.orderDao().insert(entity);
    }

    public void updateOrderStatus(String orderId, String status) {
        db.orderDao().updateStatus(orderId, status, System.currentTimeMillis());
    }

    public void updateOrderItemStatus(String orderId, int itemId, String status) {
        OrderEntity entity = db.orderDao().getById(orderId);
        if (entity != null) {
            Type type = new TypeToken<List<CartItem>>() {
            }.getType();
            List<CartItem> items = gson.fromJson(entity.itemsJson, type);
            for (CartItem item : items) {
                if (item.getId() == itemId) {
                    item.setSellerStatus(status);
                }
            }
            entity.itemsJson = gson.toJson(items);
            entity.updatedAt = System.currentTimeMillis();
            db.orderDao().update(entity);
        }
    }

    // ============== AVIS / REVIEWS ==============

    public void addReview(int productId, String oderId, String reviewerName,
            String reviewerImage, float rating, String comment) {
        ReviewEntity review = new ReviewEntity();
        review.productId = productId;
        review.oderId = oderId;
        review.reviewerName = reviewerName;
        review.reviewerImage = reviewerImage;
        review.rating = rating;
        review.comment = comment;
        db.reviewDao().insert(review);

        // Mettre à jour la note moyenne du produit
        float avgRating = db.reviewDao().getAverageRating(productId);
        int count = db.reviewDao().getReviewCount(productId);
        db.productDao().updateRating(productId, avgRating, count);
    }

    public List<ReviewEntity> getReviews(int productId) {
        return db.reviewDao().getByProduct(productId);
    }

    // ============== CONVERSIONS ==============

    private User entityToUser(UserEntity e) {
        User u = new User(e.id, e.email, e.password, e.role);
        u.setName(e.name);
        u.setProfileImage(e.profileImage);
        u.setLatitude(e.latitude);
        u.setLongitude(e.longitude);
        u.setLocation(e.location);
        return u;
    }

    private UserEntity userToEntity(User u) {
        UserEntity e = new UserEntity();
        e.id = u.getId();
        e.email = u.getEmail();
        e.password = u.getPassword();
        e.name = u.getName();
        e.role = u.getRole();
        e.profileImage = u.getProfileImage();
        e.latitude = u.getLatitude();
        e.longitude = u.getLongitude();
        e.location = u.getLocation();
        return e;
    }

    private Product entityToProduct(ProductEntity e) {
        Product p = new Product(e.id, e.title, e.price, e.category,
                e.description, e.imageUrl, e.sellerId, e.condition);
        p.setOriginalPrice(e.originalPrice);
        p.setAdditionalImages(e.additionalImages);
        p.setLatitude(e.latitude);
        p.setLongitude(e.longitude);
        p.setLocation(e.location);
        p.setRating(e.rating);
        p.setRatingCount(e.ratingCount);
        p.setFavorite(e.isFavorite);
        return p;
    }

    private ProductEntity productToEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.title = p.getTitle();
        e.price = p.getPrice();
        e.originalPrice = p.getOriginalPrice();
        e.category = p.getCategory();
        e.description = p.getDescription();
        e.imageUrl = p.getImageUrl();
        e.additionalImages = p.getAdditionalImages();
        e.sellerId = p.getSellerId();
        e.condition = p.getCondition();
        e.latitude = p.getLatitude();
        e.longitude = p.getLongitude();
        e.location = p.getLocation();
        return e;
    }

    private List<Product> entitiesToProducts(List<ProductEntity> entities) {
        List<Product> products = new ArrayList<>();
        for (ProductEntity e : entities) {
            products.add(entityToProduct(e));
        }
        return products;
    }

    private List<Order> entitiesToOrders(List<OrderEntity> entities) {
        List<Order> orders = new ArrayList<>();
        Type type = new TypeToken<List<CartItem>>() {
        }.getType();
        for (OrderEntity e : entities) {
            List<CartItem> items = gson.fromJson(e.itemsJson, type);
            Order o = new Order(e.id,
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date(e.createdAt)),
                    e.total, items, e.status, e.buyerId);
            orders.add(o);
        }
        return orders;
    }

    // Pour compatibilité avec l'ancien code
    public List<User> getUsers() {
        // Retourne une liste vide - utiliser getUserById à la place
        return new ArrayList<>();
    }

    public void saveUsers(List<User> users) {
        for (User u : users) {
            registerUser(u);
        }
    }

    public void saveProducts(List<Product> products) {
        for (Product p : products) {
            addProduct(p);
        }
    }
}
