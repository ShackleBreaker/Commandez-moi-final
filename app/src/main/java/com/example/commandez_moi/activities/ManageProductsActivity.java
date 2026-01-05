package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ManageProductAdapter;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity implements ManageProductAdapter.OnProductActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private ManageProductAdapter adapter;
    private DatabaseService db;
    private User currentUser;
    private List<Product> myProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_manage_products);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();

        setupToolbar();
        setupViews();
        loadMyProducts();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes Produits");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerProducts);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        myProducts = new ArrayList<>();
        adapter = new ManageProductAdapter(this, myProducts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMyProducts() {
        if (currentUser == null) {
            showEmptyState("Connectez-vous pour voir vos produits");
            return;
        }

        List<Product> allProducts = db.getProducts();
        myProducts.clear();

        for (Product p : allProducts) {
            if (currentUser.getId().equals(p.getSellerId())) {
                myProducts.add(p);
            }
        }

        if (myProducts.isEmpty()) {
            showEmptyState("Vous n'avez pas encore de produits\nAppuyez sur + pour en ajouter");
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateProducts(myProducts);
        }
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Product product, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le produit")
                .setMessage("Êtes-vous sûr de vouloir supprimer \"" + product.getTitle() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    db.deleteProduct(product.getId());
                    myProducts.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Produit supprimé", Toast.LENGTH_SHORT).show();

                    if (myProducts.isEmpty()) {
                        showEmptyState("Vous n'avez pas encore de produits");
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onViewClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_obj", product);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts();
    }
}
