package com.example.commandez_moi.models;

import java.io.Serializable;

public class Message implements Serializable {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private long timestamp;
    private boolean isRead;

    public Message() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public Message(String conversationId, String senderId, String senderName, String receiverId, String content) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
