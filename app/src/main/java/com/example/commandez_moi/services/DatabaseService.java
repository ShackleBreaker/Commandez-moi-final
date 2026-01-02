package com.example.commandez_moi.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.Review;
import com.example.commandez_moi.models.Message;
import com.example.commandez_moi.models.Conversation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseService {
    private static final String PREF_NAME = "CommandezMoiDB";
    private static final String KEY_PRODUCTS = "products";
    private static final String KEY_USERS = "users";
    private static final String KEY_CART = "cart";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_FAVORITES_PREFIX = "favorites_";
    private static final String KEY_REVIEWS = "reviews";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_CONVERSATIONS = "conversations";

    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static DatabaseService instance;

    private DatabaseService(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        initializeMockData();
    }

    public static synchronized DatabaseService getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseService(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeMockData() {
        if (getProducts().isEmpty()) {
            List<Product> mockProducts = new ArrayList<>();
            mockProducts.add(new Product(1, "Écouteurs sans fil", 29.99, "Tech", "Super son",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e", "seller1", "Neuf"));
            mockProducts.add(new Product(2, "Montre Connectée", 45.00, "Tech", "GPS inclus",
                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30", "seller1", "Neuf"));
            saveProducts(mockProducts);
        }

        if (getUsers().isEmpty()) {
            List<User> mockUsers = new ArrayList<>();
            // Ajout d'un utilisateur de test par défaut
            mockUsers.add(new User("1", "demo@test.com", "demo", "seller"));
            mockUsers.add(new User("2", "demo@test.com", "demo", "buyer"));
            saveUsers(mockUsers);
        }
    }

    // --- GESTION UTILISATEURS & LOGIN ---

    public User login(String email, String password) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                setCurrentUser(user);
                return user;
            }
        }
        return null;
    }

    public void setCurrentUser(User user) {
        String json = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_CURRENT_USER, json).apply();
    }

    public User getCurrentUser() {
        String json = sharedPreferences.getString(KEY_CURRENT_USER, null);
        if (json == null)
            return null;
        return gson.fromJson(json, User.class);
    }

    public void logout() {
        sharedPreferences.edit().remove(KEY_CURRENT_USER).apply();
    }

    public List<User> getUsers() {
        String json = sharedPreferences.getString(KEY_USERS, null);
        Type type = new TypeToken<ArrayList<User>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveUsers(List<User> users) {
        String json = gson.toJson(users);
        sharedPreferences.edit().putString(KEY_USERS, json).apply();
    }

    // --- GESTION PRODUITS ---

    public List<Product> getProducts() {
        String json = sharedPreferences.getString(KEY_PRODUCTS, null);
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveProducts(List<Product> products) {
        String json = gson.toJson(products);
        sharedPreferences.edit().putString(KEY_PRODUCTS, json).apply();
    }

    public void addProduct(Product product) {
        List<Product> products = getProducts();
        products.add(product);
        saveProducts(products);
    }

    // --- GESTION PANIER ---

    public List<CartItem> getCart() {
        String json = sharedPreferences.getString(KEY_CART, null);
        Type type = new TypeToken<ArrayList<CartItem>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveCart(List<CartItem> cartItems) {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }

    public void addToCart(Product product) {
        List<CartItem> cart = getCart();
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            // Conversion Product -> CartItem
            CartItem newItem = new CartItem();
            newItem.setId(product.getId());
            newItem.setTitle(product.getTitle());
            newItem.setPrice(product.getPrice());
            newItem.setImageUrl(product.getImageUrl());
            newItem.setSellerId(product.getSellerId());
            newItem.setQuantity(1);
            newItem.setSellerStatus("En attente");
            cart.add(newItem);
        }
        saveCart(cart);
    }

    public void clearCart() {
        sharedPreferences.edit().remove(KEY_CART).apply();
    }

    // --- GESTION COMMANDES ---

    public List<Order> getOrders() {
        String json = sharedPreferences.getString(KEY_ORDERS, null);
        Type type = new TypeToken<ArrayList<Order>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveOrder(Order order) {
        List<Order> orders = getOrders();
        orders.add(order);
        String json = gson.toJson(orders);
        sharedPreferences.edit().putString(KEY_ORDERS, json).apply();
    }

    public void updateOrderItemStatus(String orderId, int itemId, String status) {
        List<Order> orders = getOrders();
        for (Order order : orders) {
            if (order.getId().equals(orderId)) {
                for (CartItem item : order.getItems()) {
                    if (item.getId() == itemId) {
                        item.setSellerStatus(status);
                    }
                }
            }
        }
        String json = gson.toJson(orders);
        sharedPreferences.edit().putString(KEY_ORDERS, json).apply();
    }

    // --- GESTION FAVORIS ---

    public void addToFavorites(String userId, int productId) {
        Set<String> favorites = getFavoriteIds(userId);
        favorites.add(String.valueOf(productId));
        saveFavorites(userId, favorites);
    }

    public void removeFromFavorites(String userId, int productId) {
        Set<String> favorites = getFavoriteIds(userId);
        favorites.remove(String.valueOf(productId));
        saveFavorites(userId, favorites);
    }

    public boolean isProductFavorite(String userId, int productId) {
        Set<String> favorites = getFavoriteIds(userId);
        return favorites.contains(String.valueOf(productId));
    }

    public void toggleFavorite(String userId, int productId) {
        if (isProductFavorite(userId, productId)) {
            removeFromFavorites(userId, productId);
        } else {
            addToFavorites(userId, productId);
        }
    }

    private Set<String> getFavoriteIds(String userId) {
        String json = sharedPreferences.getString(KEY_FAVORITES_PREFIX + userId, null);
        if (json == null) {
            return new HashSet<>();
        }
        Type type = new TypeToken<HashSet<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void saveFavorites(String userId, Set<String> favoriteIds) {
        String json = gson.toJson(favoriteIds);
        sharedPreferences.edit().putString(KEY_FAVORITES_PREFIX + userId, json).apply();
    }

    public List<Product> getFavoriteProducts(String userId) {
        Set<String> favoriteIds = getFavoriteIds(userId);
        List<Product> allProducts = getProducts();
        List<Product> favorites = new ArrayList<>();

        for (Product product : allProducts) {
            if (favoriteIds.contains(String.valueOf(product.getId()))) {
                product.setFavorite(true);
                favorites.add(product);
            }
        }
        return favorites;
    }

    // --- HELPERS ---

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public boolean isSeller() {
        User user = getCurrentUser();
        return user != null && "seller".equals(user.getRole());
    }

    public int getCartCount() {
        List<CartItem> cart = getCart();
        int count = 0;
        for (CartItem item : cart) {
            count += item.getQuantity();
        }
        return count;
    }

    public Product getProductById(int id) {
        List<Product> products = getProducts();
        for (Product product : products) {
            if (product.getId() == id) {
                return product;
            }
        }
        return null;
    }

    public void updateProduct(Product updatedProduct) {
        List<Product> products = getProducts();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == updatedProduct.getId()) {
                products.set(i, updatedProduct);
                break;
            }
        }
        saveProducts(products);
    }

    public void deleteProduct(int productId) {
        List<Product> products = getProducts();
        products.removeIf(p -> p.getId() == productId);
        saveProducts(products);
    }

    public User getUserById(String userId) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public void updateUser(User updatedUser) {
        List<User> users = getUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                break;
            }
        }
        saveUsers(users);

        // Mettre à jour l'utilisateur courant si c'est lui qui est modifié
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
            setCurrentUser(updatedUser);
        }
    }

    // --- GESTION DES AVIS (REVIEWS) ---

    public List<Review> getAllReviews() {
        String json = sharedPreferences.getString(KEY_REVIEWS, null);
        Type type = new TypeToken<ArrayList<Review>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private void saveAllReviews(List<Review> reviews) {
        String json = gson.toJson(reviews);
        sharedPreferences.edit().putString(KEY_REVIEWS, json).apply();
    }

    public List<Review> getReviews(String productId) {
        List<Review> allReviews = getAllReviews();
        List<Review> productReviews = new ArrayList<>();
        for (Review review : allReviews) {
            if (review.getProductId().equals(productId)) {
                productReviews.add(review);
            }
        }
        return productReviews;
    }

    public void addReview(Review review) {
        List<Review> allReviews = getAllReviews();

        // Vérifier si l'utilisateur a déjà un avis pour ce produit (mise à jour)
        boolean found = false;
        for (int i = 0; i < allReviews.size(); i++) {
            Review r = allReviews.get(i);
            if (r.getProductId().equals(review.getProductId()) && r.getUserId().equals(review.getUserId())) {
                allReviews.set(i, review);
                found = true;
                break;
            }
        }

        if (!found) {
            allReviews.add(review);
        }

        saveAllReviews(allReviews);
    }

    public void deleteReview(String reviewId) {
        List<Review> allReviews = getAllReviews();
        allReviews.removeIf(r -> r.getId().equals(reviewId));
        saveAllReviews(allReviews);
    }

    public float getProductAverageRating(String productId) {
        List<Review> productReviews = getReviews(productId);
        if (productReviews.isEmpty()) {
            return 0;
        }
        float sum = 0;
        for (Review review : productReviews) {
            sum += review.getRating();
        }
        return sum / productReviews.size();
    }

    // --- GESTION DES MESSAGES ET CONVERSATIONS ---

    private List<Message> getAllMessages() {
        String json = sharedPreferences.getString(KEY_MESSAGES, null);
        Type type = new TypeToken<ArrayList<Message>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private void saveAllMessages(List<Message> messages) {
        String json = gson.toJson(messages);
        sharedPreferences.edit().putString(KEY_MESSAGES, json).apply();
    }

    public List<Message> getMessages(String conversationId) {
        List<Message> allMessages = getAllMessages();
        List<Message> conversationMessages = new ArrayList<>();
        for (Message msg : allMessages) {
            if (msg.getConversationId().equals(conversationId)) {
                conversationMessages.add(msg);
            }
        }
        return conversationMessages;
    }

    public void sendMessage(Message message, String productId, String productTitle, String otherUserName) {
        // Save message
        List<Message> allMessages = getAllMessages();
        allMessages.add(message);
        saveAllMessages(allMessages);

        // Update or create conversation
        Conversation conversation = getConversation(message.getConversationId());
        if (conversation == null) {
            User currentUser = getCurrentUser();
            conversation = new Conversation(
                    message.getSenderId(),
                    currentUser != null ? currentUser.getName() : "Utilisateur",
                    message.getReceiverId(),
                    otherUserName,
                    productId,
                    productTitle);
            conversation.setId(message.getConversationId());
        }
        conversation.setLastMessage(message.getContent());
        conversation.setLastMessageTime(message.getTimestamp());
        saveConversation(conversation);
    }

    private List<Conversation> getAllConversations() {
        String json = sharedPreferences.getString(KEY_CONVERSATIONS, null);
        Type type = new TypeToken<ArrayList<Conversation>>() {
        }.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    private void saveAllConversations(List<Conversation> conversations) {
        String json = gson.toJson(conversations);
        sharedPreferences.edit().putString(KEY_CONVERSATIONS, json).apply();
    }

    public Conversation getConversation(String conversationId) {
        List<Conversation> conversations = getAllConversations();
        for (Conversation conv : conversations) {
            if (conv.getId().equals(conversationId)) {
                return conv;
            }
        }
        return null;
    }

    private void saveConversation(Conversation conversation) {
        List<Conversation> conversations = getAllConversations();
        boolean found = false;
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getId().equals(conversation.getId())) {
                conversations.set(i, conversation);
                found = true;
                break;
            }
        }
        if (!found) {
            conversations.add(conversation);
        }
        saveAllConversations(conversations);
    }

    public List<Conversation> getUserConversations(String userId) {
        List<Conversation> allConversations = getAllConversations();
        List<Conversation> userConversations = new ArrayList<>();
        for (Conversation conv : allConversations) {
            if (conv.getUser1Id().equals(userId) || conv.getUser2Id().equals(userId)) {
                userConversations.add(conv);
            }
        }
        // Sort by last message time (newest first)
        userConversations.sort((c1, c2) -> Long.compare(c2.getLastMessageTime(), c1.getLastMessageTime()));
        return userConversations;
    }

    public void markMessagesAsRead(String conversationId, String userId) {
        List<Message> allMessages = getAllMessages();
        for (Message msg : allMessages) {
            if (msg.getConversationId().equals(conversationId) && msg.getReceiverId().equals(userId)) {
                msg.setRead(true);
            }
        }
        saveAllMessages(allMessages);
    }

    public int getUnreadMessagesCount(String userId) {
        List<Message> allMessages = getAllMessages();
        int count = 0;
        for (Message msg : allMessages) {
            if (msg.getReceiverId().equals(userId) && !msg.isRead()) {
                count++;
            }
        }
        return count;
    }
}