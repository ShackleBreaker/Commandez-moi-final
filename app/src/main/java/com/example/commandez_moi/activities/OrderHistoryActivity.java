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
import com.example.commandez_moi.utils.ThemeManager;
import com.example.commandez_moi.utils.ImageUtils;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyText;
    private DatabaseService db;
    private User currentUser;
    private TabLayout tabLayout;
    private List<Order> allMyOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
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

        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.ordersRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadOrders();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadOrders() {
        if (currentUser == null) {
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        List<Order> allOrders = db.getOrders();
        allMyOrders.clear();

        for (Order order : allOrders) {
            if (currentUser.getId().equals(order.getBuyerId())) {
                allMyOrders.add(order);
            }
        }

        // Trier par date (plus récentes en premier)
        Collections.reverse(allMyOrders);

        // Initial filter
        filterOrders(tabLayout.getSelectedTabPosition());
    }

    private void filterOrders(int tabPosition) {
        List<Order> filteredList = new ArrayList<>();

        if (tabPosition == 0) { // Toutes
            filteredList.addAll(allMyOrders);
        } else {
            for (Order order : allMyOrders) {
                List<CartItem> items = order.getItems();

                // Fallback logic if no items
                if (items == null || items.isEmpty()) {
                    String status = order.getStatus();
                    if (tabPosition == 1) { // En attente
                        if (status == null || status.equalsIgnoreCase("En cours") ||
                                status.equalsIgnoreCase("En attente") || status.equalsIgnoreCase("Pending")) {
                            filteredList.add(order);
                        }
                    }
                    continue;
                }

                boolean hasPending = false;
                boolean hasConfirmed = false;
                boolean allDelivered = true;
                boolean hasDelivered = false;

                for (CartItem item : items) {
                    String s = item.getSellerStatus();
                    if (s == null || s.equalsIgnoreCase("En attente") || s.equalsIgnoreCase("En cours")) {
                        hasPending = true;
                        allDelivered = false;
                    } else if (s.equalsIgnoreCase("Confirmé") || s.equalsIgnoreCase("Confirmed")) {
                        hasConfirmed = true;
                        allDelivered = false;
                    } else if (s.equalsIgnoreCase("Livré") || s.equalsIgnoreCase("Delivered")) {
                        hasConfirmed = true;
                        hasDelivered = true;
                    } else if (s.equalsIgnoreCase("Rejeté")) {
                        allDelivered = false;
                    }
                }

                if (tabPosition == 1) { // En attente
                    if (hasPending) {
                        filteredList.add(order);
                    }
                } else if (tabPosition == 2) { // Confirmées
                    if (!hasPending && hasConfirmed) {
                        filteredList.add(order);
                    }
                } else if (tabPosition == 3) { // Livrées
                    if (allDelivered && hasDelivered) {
                        filteredList.add(order);
                    }
                }
            }
        }

        updateRecyclerView(filteredList);
    }

    private void updateRecyclerView(List<Order> orders) {
        if (orders.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new OrdersAdapter(orders));
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
