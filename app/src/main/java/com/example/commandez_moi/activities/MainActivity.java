package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ProductAdapter;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.LocationHelper;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private DatabaseService db;
    private User currentUser;
    private EditText searchInput;
    private String currentCategory = "Tout";
    private String searchQuery = "";
    private List<Product> allProducts = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private BottomNavigationView bottomNav;
    private LocationHelper locationHelper;
    private double userLatitude = 0;
    private double userLongitude = 0;
    private boolean sortByProximity = false;

    private Button btnAll, btnTech, btnMode, btnMaison, btnBeaute, btnSport;
    private Button selectedCategoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();
        locationHelper = new LocationHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        tvEmptyState = findViewById(R.id.tvEmptyState);

        // SwipeRefresh
        swipeRefresh = findViewById(R.id.swipeRefresh);
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.purple_700);
            swipeRefresh.setOnRefreshListener(() -> {
                loadProducts();
                swipeRefresh.setRefreshing(false);
            });
        }

        // Recherche
        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase().trim();
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Boutons catégories
        setupCategoryButtons();

        // FAB Panier
        FloatingActionButton fabCart = findViewById(R.id.fabCart);
        fabCart.setOnClickListener(v -> {
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                startActivity(new Intent(this, CartActivity.class));
            }
        });

        // Bottom Navigation
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_favorites) {
                if (currentUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    startActivity(new Intent(this, FavoritesActivity.class));
                }
                return true;
            } else if (id == R.id.nav_messages) {
                if (currentUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    startActivity(new Intent(this, ConversationsActivity.class));
                }
                return true;
            } else if (id == R.id.nav_cart) {
                if (currentUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    startActivity(new Intent(this, CartActivity.class));
                }
                return true;
            } else if (id == R.id.nav_profile) {
                if (currentUser == null) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    startActivity(new Intent(this, ProfileActivity.class));
                }
                return true;
            }
            return false;
        });

        // Demander la localisation de l'utilisateur
        getUserLocation();
    }

    private void setupCategoryButtons() {
        btnAll = findViewById(R.id.btnAll);
        btnTech = findViewById(R.id.btnTech);
        btnMode = findViewById(R.id.btnMode);
        btnMaison = findViewById(R.id.btnMaison);
        btnBeaute = findViewById(R.id.btnBeaute);
        btnSport = findViewById(R.id.btnSport);

        selectedCategoryButton = btnAll;

        btnAll.setOnClickListener(v -> selectCategory("Tout", btnAll));
        btnTech.setOnClickListener(v -> selectCategory("Tech", btnTech));
        btnMode.setOnClickListener(v -> selectCategory("Mode", btnMode));
        btnMaison.setOnClickListener(v -> selectCategory("Maison", btnMaison));
        btnBeaute.setOnClickListener(v -> selectCategory("Beauté", btnBeaute));
        btnSport.setOnClickListener(v -> selectCategory("Sport", btnSport));
    }

    private void selectCategory(String category, Button button) {
        currentCategory = category;

        // Reset style de l'ancien bouton
        if (selectedCategoryButton != null) {
            selectedCategoryButton.setBackgroundTintList(null);
        }

        // Appliquer style au nouveau bouton
        selectedCategoryButton = button;

        filterProducts();
    }

    private void filterProducts() {
        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            boolean matchesCategory = "Tout".equals(currentCategory) ||
                    (p.getCategory() != null && p.getCategory().equalsIgnoreCase(currentCategory));

            boolean matchesSearch = searchQuery.isEmpty() ||
                    (p.getTitle() != null && p.getTitle().toLowerCase().contains(searchQuery)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchQuery));

            if (matchesCategory && matchesSearch) {
                filtered.add(p);
            }
        }

        // Trier par proximité si activé et localisation disponible
        if (sortByProximity && userLatitude != 0 && userLongitude != 0) {
            filtered.sort((p1, p2) -> {
                double dist1 = LocationHelper.calculateDistance(userLatitude, userLongitude,
                        p1.getLatitude(), p1.getLongitude());
                double dist2 = LocationHelper.calculateDistance(userLatitude, userLongitude,
                        p2.getLatitude(), p2.getLongitude());
                return Double.compare(dist1, dist2);
            });
        }

        // Marquer les favoris
        String userId = db.getCurrentUserId();
        if (userId != null) {
            for (Product p : filtered) {
                p.setFavorite(db.isProductFavorite(userId, p.getId()));
            }
        }

        // Afficher/masquer l'état vide
        if (filtered.isEmpty()) {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Aucun produit trouvé");
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new ProductAdapter(this, filtered, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_obj", product);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Product product, int position) {
        if (currentUser == null) {
            Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        db.toggleFavorite(currentUser.getId(), product.getId());
        boolean isFavorite = db.isProductFavorite(currentUser.getId(), product.getId());
        adapter.updateProductFavorite(position, isFavorite);

        if (isFavorite) {
            Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserLocation() {
        if (locationHelper.hasLocationPermission()) {
            locationHelper.getCurrentLocation(new LocationHelper.LocationListener() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String address) {
                    userLatitude = latitude;
                    userLongitude = longitude;
                    // Recharger les produits si tri par proximité activé
                    if (sortByProximity) {
                        filterProducts();
                    }
                }

                @Override
                public void onLocationError(String error) {
                    // Ignorer silencieusement
                }
            });
        } else {
            locationHelper.requestLocationPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }

    private void updateCartBadge() {
        if (bottomNav != null) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_cart);
            int cartCount = db.getCartCount();
            if (cartCount > 0) {
                badge.setVisible(true);
                badge.setNumber(cartCount);
            } else {
                badge.setVisible(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.action_login);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        MenuItem addProductItem = menu.findItem(R.id.action_add_product);
        MenuItem dashboardItem = menu.findItem(R.id.action_dashboard);

        if (currentUser == null) {
            loginItem.setVisible(true);
            logoutItem.setVisible(false);
            addProductItem.setVisible(false);
            dashboardItem.setVisible(false);
        } else {
            loginItem.setVisible(false);
            logoutItem.setVisible(true);
            if ("seller".equals(currentUser.getRole())) {
                addProductItem.setVisible(true);
                dashboardItem.setVisible(true);
            } else {
                addProductItem.setVisible(false);
                dashboardItem.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            db.logout();
            currentUser = null;
            invalidateOptionsMenu();
            return true;
        } else if (id == R.id.action_add_product) {
            startActivity(new Intent(this, AddProductActivity.class));
            return true;
        } else if (id == R.id.action_dashboard) {
            startActivity(new Intent(this, SellerDashboardActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = db.getCurrentUser();
        invalidateOptionsMenu();
        loadProducts();
        updateCartBadge();

        // Reset bottom nav to home
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadProducts() {
        allProducts = db.getProducts();
        filterProducts();
    }
}