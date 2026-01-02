package com.example.commandez_moi.models;

public class CartItem extends Product {
    private int quantity;
    private String sellerStatus; // "En attente", "Confirmé", "Rejeté"

    // Constructeur vide pour Gson
    public CartItem() {}

    public CartItem(Product product, int quantity) {
        super(product.getId(), product.getTitle(), product.getPrice(), product.getCategory(), product.getDescription(), product.getImageUrl(), product.getSellerId(), product.getCondition());
        this.quantity = quantity;
        this.sellerStatus = "En attente";
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSellerStatus() { return sellerStatus; }
    public void setSellerStatus(String sellerStatus) { this.sellerStatus = sellerStatus; }
}