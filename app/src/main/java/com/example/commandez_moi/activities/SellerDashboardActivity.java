package com.example.commandez_moi.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.example.commandez_moi.utils.ImageUtils;
import java.util.ArrayList;
import java.util.List;

public class SellerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvRevenue;
    private DatabaseService db;
    private SalesAdapter adapter;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_seller_dashboard);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerSales);
        tvRevenue = findViewById(R.id.tvRevenue);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSales();
    }

    private void loadSales() {
        List<Order> allOrders = db.getOrders();
        List<SaleItem> mySales = new ArrayList<>();
        double revenue = 0;

        // Vérifier que l'utilisateur courant existe
        if (currentUser == null) {
            tvRevenue.setText("0.00 €");
            return;
        }

        // On aplatit les commandes pour trouver les items de ce vendeur
        for (Order order : allOrders) {
            for (CartItem item : order.getItems()) {
                String sellerId = item.getSellerId();
                if (sellerId != null && sellerId.equals(currentUser.getId())) {
                    mySales.add(new SaleItem(order.getId(), order.getDate(), item));
                    if (!"Rejeté".equals(item.getSellerStatus())) {
                        revenue += item.getPrice() * item.getQuantity();
                    }
                }
            }
        }

        tvRevenue.setText(String.format("%.2f €", revenue));
        adapter = new SalesAdapter(mySales);
        recyclerView.setAdapter(adapter);
    }

    // Classe interne pour représenter une ligne de vente
    private static class SaleItem {
        String orderId;
        String date;
        CartItem item;

        SaleItem(String orderId, String date, CartItem item) {
            this.orderId = orderId;
            this.date = date;
            this.item = item;
        }
    }

    // Adaptateur interne spécifique au Dashboard
    private class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> {
        List<SaleItem> sales;

        SalesAdapter(List<SaleItem> sales) {
            this.sales = sales;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SaleItem sale = sales.get(position);
            CartItem item = sale.item;

            holder.tvTitle.setText(item.getTitle());
            holder.tvDetails.setText("Qté: " + item.getQuantity() + " • Commande #" + sale.orderId);
            holder.tvStatus.setText(item.getSellerStatus());
            ImageUtils.loadImage(holder.imgProduct, item.getImageUrl());

            // Gestion des couleurs de statut
            if ("Confirmé".equals(item.getSellerStatus())) {
                holder.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
                holder.btnConfirm.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            } else if ("Rejeté".equals(item.getSellerStatus())) {
                holder.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                holder.btnConfirm.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setTextColor(getColor(android.R.color.darker_gray));
                holder.btnConfirm.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
            }

            holder.btnConfirm.setOnClickListener(v -> updateStatus(sale.orderId, item.getId(), "Confirmé"));
            holder.btnReject.setOnClickListener(v -> updateStatus(sale.orderId, item.getId(), "Rejeté"));
        }

        @Override
        public int getItemCount() {
            return sales.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDetails, tvStatus;
            ImageView imgProduct;
            Button btnConfirm, btnReject;

            ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.tvTitle);
                tvDetails = view.findViewById(R.id.tvDetails);
                tvStatus = view.findViewById(R.id.tvStatus);
                imgProduct = view.findViewById(R.id.imgProduct);
                btnConfirm = view.findViewById(R.id.btnConfirm);
                btnReject = view.findViewById(R.id.btnReject);
            }
        }
    }

    private void updateStatus(String orderId, int itemId, String status) {
        db.updateOrderItemStatus(orderId, itemId, status);
        Toast.makeText(this, "Statut mis à jour : " + status, Toast.LENGTH_SHORT).show();
        loadSales(); // Recharger la liste

        if ("Confirmé".equals(status)) {
            // Simulation : Passage automatique à "Livré" après 1 minute
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                db.updateOrderItemStatus(orderId, itemId, "Livré");
                // Si l'activité est toujours visible, on pourrait rafraîchir,
                // mais comme c'est asynchrone, on laisse l'utilisateur rafraîchir manuellement
                // ou au prochain chargement.
            }, 60000); // 60000 ms = 1 minute
        }
    }
}