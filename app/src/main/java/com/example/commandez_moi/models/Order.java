package com.example.commandez_moi.models;

import java.util.List;

public class Order {
    private String id;
    private String date;
    private double total;
    private List<CartItem> items;
    private String status; // "pending", "confirmed", "shipped", "delivered", "cancelled"
    private String buyerId;
    private String buyerName;
    private String sellerId;
    private String sellerName;
    private String shippingAddress;
    private String trackingNumber;
    private String paymentMethod;
    private String notes;
    private long createdAt;
    private long updatedAt;

    public Order(String id, String date, double total, List<CartItem> items, String status, String buyerId) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.items = items;
        this.status = status;
        this.buyerId = buyerId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public String getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getStatusDisplay() {
        switch (status) {
            case "pending":
                return "⏳ En attente";
            case "confirmed":
                return "✅ Confirmée";
            case "shipped":
                return "🚚 Expédiée";
            case "delivered":
                return "📦 Livrée";
            case "cancelled":
                return "❌ Annulée";
            default:
                return status;
        }
    }
}