package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ProductAdapter;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ProductAdapter adapter;
    private DatabaseService databaseService;
    private List<Product> favoriteProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_favorites);

        databaseService = DatabaseService.getInstance(this);

        setupToolbar();
        setupViews();
        loadFavorites();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes Favoris");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recycler_favorites);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        favoriteProducts = new ArrayList<>();
        adapter = new ProductAdapter(this, favoriteProducts, this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        String userId = databaseService.getCurrentUserId();
        if (userId == null) {
            showEmptyState("Connectez-vous pour voir vos favoris");
            return;
        }

        // Récupérer les produits favoris
        List<Product> allProducts = databaseService.getProducts();
        favoriteProducts.clear();

        for (Product product : allProducts) {
            if (databaseService.isProductFavorite(userId, product.getId())) {
                product.setFavorite(true);
                favoriteProducts.add(product);
            }
        }

        if (favoriteProducts.isEmpty()) {
            showEmptyState("Aucun favori pour le moment\nAppuyez sur ❤️ pour ajouter des produits");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateProducts(favoriteProducts);
        }
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Product product, int position) {
        String userId = databaseService.getCurrentUserId();
        if (userId == null)
            return;

        // Retirer des favoris
        databaseService.removeFromFavorites(userId, product.getId());
        favoriteProducts.remove(position);
        adapter.notifyItemRemoved(position);

        if (favoriteProducts.isEmpty()) {
            showEmptyState("Aucun favori pour le moment\nAppuyez sur ❤️ pour ajouter des produits");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}
