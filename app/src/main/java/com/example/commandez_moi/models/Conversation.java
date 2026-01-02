package com.example.commandez_moi.models;

import java.io.Serializable;

public class Conversation implements Serializable {
    private String id;
    private String user1Id;
    private String user2Id;
    private String user1Name;
    private String user2Name;
    private String user1Photo;
    private String user2Photo;
    private String productId;
    private String productTitle;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public Conversation() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.lastMessageTime = System.currentTimeMillis();
    }

    public Conversation(String user1Id, String user1Name, String user2Id, String user2Name, String productId,
            String productTitle) {
        this.id = generateConversationId(user1Id, user2Id, productId);
        this.user1Id = user1Id;
        this.user1Name = user1Name;
        this.user2Id = user2Id;
        this.user2Name = user2Name;
        this.productId = productId;
        this.productTitle = productTitle;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public static String generateConversationId(String user1Id, String user2Id, String productId) {
        // Create unique conversation ID based on users and product
        String sortedUsers = user1Id.compareTo(user2Id) < 0
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;
        return sortedUsers + "_" + productId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getUser1Name() {
        return user1Name;
    }

    public void setUser1Name(String user1Name) {
        this.user1Name = user1Name;
    }

    public String getUser2Name() {
        return user2Name;
    }

    public void setUser2Name(String user2Name) {
        this.user2Name = user2Name;
    }

    public String getUser1Photo() {
        return user1Photo;
    }

    public void setUser1Photo(String user1Photo) {
        this.user1Photo = user1Photo;
    }

    public String getUser2Photo() {
        return user2Photo;
    }

    public void setUser2Photo(String user2Photo) {
        this.user2Photo = user2Photo;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getOtherUserId(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Id : user1Id;
    }

    public String getOtherUserName(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Name : user1Name;
    }

    public String getOtherUserPhoto(String currentUserId) {
        return currentUserId.equals(user1Id) ? user2Photo : user1Photo;
    }
}
