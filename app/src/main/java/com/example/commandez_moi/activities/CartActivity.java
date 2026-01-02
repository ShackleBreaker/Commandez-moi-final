package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.CartAdapter;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartListener {

    private RecyclerView recyclerView;
    private TextView tvTotal;
    private Button btnCheckout;
    private DatabaseService db;
    private List<CartItem> cartItems;
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = DatabaseService.getInstance(this);
        cartItems = db.getCart();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Mon Panier");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.cartRecyclerView);
        tvTotal = findViewById(R.id.totalPriceText);
        btnCheckout = findViewById(R.id.checkoutButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartItems, this);
        recyclerView.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotal.setText(String.format("Total: %.2f €", total));
        btnCheckout.setEnabled(!cartItems.isEmpty());
        btnCheckout.setText("Commander (" + cartItems.size() + ")");
    }

    private void handleCheckout() {
        User user = db.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        for (CartItem item : cartItems) {
            item.setSellerStatus("En attente");
            total += item.getPrice() * item.getQuantity();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        String date = sdf.format(new Date());

        Order order = new Order(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                date,
                total,
                new ArrayList<>(cartItems),
                "En cours",
                user.getId());

        db.saveOrder(order);
        db.clearCart();
        cartItems.clear();

        Toast.makeText(this, "Commande validée !", Toast.LENGTH_LONG).show();

        // Rediriger vers l'historique
        startActivity(new Intent(this, OrderHistoryActivity.class));
        finish();
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        item.setQuantity(newQuantity);
        db.saveCart(cartItems);
        adapter.notifyDataSetChanged();
        updateTotal();
    }

    @Override
    public void onRemove(CartItem item) {
        cartItems.remove(item);
        db.saveCart(cartItems);
        adapter.notifyDataSetChanged();
        updateTotal();
    }
}