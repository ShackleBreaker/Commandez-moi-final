package com.example.commandez_moi.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ImageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private DatabaseService db;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes commandes");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.ordersRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();
    }

    private void loadOrders() {
        if (currentUser == null) {
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        List<Order> allOrders = db.getOrders();
        List<Order> myOrders = new ArrayList<>();

        for (Order order : allOrders) {
            if (currentUser.getId().equals(order.getBuyerId())) {
                myOrders.add(order);
            }
        }

        // Trier par date (plus récentes en premier)
        Collections.reverse(myOrders);

        if (myOrders.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new OrdersAdapter(myOrders));
        }
    }

    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {
        private List<Order> orders;

        OrdersAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.orderId.setText("Commande #" + order.getId());
            holder.orderDate.setText(order.getDate());
            holder.orderStatus.setText(order.getStatus());
            holder.orderTotal.setText(String.format("%.2f €", order.getTotal()));

            // Afficher les items
            holder.itemsContainer.removeAllViews();
            for (CartItem item : order.getItems()) {
                View itemView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_order_product, holder.itemsContainer, false);

                ImageView img = itemView.findViewById(R.id.productImage);
                TextView title = itemView.findViewById(R.id.productTitle);
                TextView qty = itemView.findViewById(R.id.productQty);
                TextView status = itemView.findViewById(R.id.productStatus);

                ImageUtils.loadImage(img, item.getImageUrl());
                title.setText(item.getTitle());
                qty.setText("x" + item.getQuantity());
                status.setText(item.getSellerStatus() != null ? item.getSellerStatus() : "En attente");

                // Couleur du statut
                String sellerStatus = item.getSellerStatus();
                if ("Confirmé".equals(sellerStatus)) {
                    status.setTextColor(0xFF4CAF50); // Vert
                } else if ("Rejeté".equals(sellerStatus)) {
                    status.setTextColor(0xFFF44336); // Rouge
                } else {
                    status.setTextColor(0xFFFF9800); // Orange
                }

                holder.itemsContainer.addView(itemView);
            }
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView orderId, orderDate, orderStatus, orderTotal;
            LinearLayout itemsContainer;

            ViewHolder(View itemView) {
                super(itemView);
                orderId = itemView.findViewById(R.id.orderId);
                orderDate = itemView.findViewById(R.id.orderDate);
                orderStatus = itemView.findViewById(R.id.orderStatus);
                orderTotal = itemView.findViewById(R.id.orderTotal);
                itemsContainer = itemView.findViewById(R.id.itemsContainer);
            }
        }
    }
}
