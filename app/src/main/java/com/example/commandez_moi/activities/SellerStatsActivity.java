package com.example.commandez_moi.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SellerStatsActivity extends AppCompatActivity {

    private DatabaseService db;
    private User currentUser;

    private TextView tvTotalSales, tvTotalRevenue, tvTotalProducts, tvAverageRating, tvTotalReviews;
    private RecyclerView rvRecentOrders, rvTopProducts;
    private Chip chipWeek, chipMonth, chipYear, chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_stats);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();

        initViews();
        loadStats();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        rvRecentOrders = findViewById(R.id.rvRecentOrders);
        rvTopProducts = findViewById(R.id.rvTopProducts);

        chipWeek = findViewById(R.id.chipWeek);
        chipMonth = findViewById(R.id.chipMonth);
        chipYear = findViewById(R.id.chipYear);
        chipAll = findViewById(R.id.chipAll);

        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        rvTopProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Period chip listeners
        View.OnClickListener chipListener = v -> {
            chipWeek.setChecked(v == chipWeek);
            chipMonth.setChecked(v == chipMonth);
            chipYear.setChecked(v == chipYear);
            chipAll.setChecked(v == chipAll);
            loadStats();
        };

        chipWeek.setOnClickListener(chipListener);
        chipMonth.setOnClickListener(chipListener);
        chipYear.setOnClickListener(chipListener);
        chipAll.setOnClickListener(chipListener);
    }

    private void loadStats() {
        if (currentUser == null)
            return;

        String sellerId = currentUser.getId();

        // Count products
        List<Product> allProducts = db.getProducts();
        List<Product> sellerProducts = new ArrayList<>();
        for (Product p : allProducts) {
            if (sellerId.equals(p.getSellerId())) {
                sellerProducts.add(p);
            }
        }

        // Count orders and revenue
        List<Order> allOrders = db.getOrders();
        int totalSales = 0;
        double totalRevenue = 0;
        for (Order order : allOrders) {
            if (sellerId.equals(order.getSellerId())) {
                totalSales++;
                totalRevenue += order.getTotal();
            }
        }

        // Calculate average rating
        float totalRating = 0;
        int reviewCount = 0;
        for (Product p : sellerProducts) {
            if (p.getRatingCount() > 0) {
                totalRating += p.getRating() * p.getRatingCount();
                reviewCount += p.getRatingCount();
            }
        }
        float avgRating = reviewCount > 0 ? totalRating / reviewCount : 0;

        // Update UI
        tvTotalSales.setText(String.valueOf(totalSales));
        tvTotalRevenue.setText(String.format("€%.0f", totalRevenue));
        tvTotalProducts.setText(String.valueOf(sellerProducts.size()));
        tvAverageRating.setText(String.format("%.1f", avgRating));
        tvTotalReviews.setText(reviewCount + " avis");

        // Update user stats
        currentUser.setTotalSales(totalSales);
        currentUser.setTotalRevenue(totalRevenue);
        currentUser.setTotalProducts(sellerProducts.size());
        currentUser.setAverageRating(avgRating);
        currentUser.setTotalReviews(reviewCount);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
