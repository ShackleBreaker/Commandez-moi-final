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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ProductAdapter;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.LocationHelper;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.Slider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
    private View emptyStateContainer;
    private BottomNavigationView bottomNav;
    private LocationHelper locationHelper;
    private double userLatitude = 0;
    private double userLongitude = 0;
    private boolean sortByProximity = false;

    // Filter state
    private Double minPrice = null;
    private Double maxPrice = null;
    private Set<String> selectedConditions = new HashSet<>();
    private int maxDistance = 10000; // Default unlimited (10000km)
    private boolean verifiedOnly = false;

    // Sort state
    private enum SortOption {
        RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST, DISTANCE, RATING
    }

    private SortOption currentSortOption = SortOption.RELEVANCE;

    private Button btnAll, btnTech, btnMode, btnMaison, btnBeaute, btnSport;
    private Button selectedCategoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Appliquer le thème au cas où l'activité est restaurée directement
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_main);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();
        locationHelper = new LocationHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        tvEmptyState = findViewById(R.id.tvEmptyState);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

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

        // Boutons filtres et tri
        ImageButton btnFilter = findViewById(R.id.btnFilter);
        ImageButton btnSort = findViewById(R.id.btnSort);

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }

        // Boutons catégories
        setupCategoryButtons();

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

        // Initialiser le bouton "Tout" comme sélectionné (Noir/Blanc)
        selectedCategoryButton = btnAll;
        updateButtonStyles();

        btnAll.setOnClickListener(v -> selectCategory("Tout", btnAll));
        btnTech.setOnClickListener(v -> selectCategory("Tech", btnTech));
        btnMode.setOnClickListener(v -> selectCategory("Mode", btnMode));
        btnMaison.setOnClickListener(v -> selectCategory("Maison", btnMaison));
        btnBeaute.setOnClickListener(v -> selectCategory("Beauté", btnBeaute));
        btnSport.setOnClickListener(v -> selectCategory("Sport", btnSport));
    }

    private void selectCategory(String category, Button button) {
        currentCategory = category;
        selectedCategoryButton = button;
        updateButtonStyles();
        filterProducts();
    }

    private void updateButtonStyles() {
        Button[] allButtons = { btnAll, btnTech, btnMode, btnMaison, btnBeaute, btnSport };

        for (Button btn : allButtons) {
            if (btn == selectedCategoryButton) {
                // Style Sélectionné : Fond Violet, Texte Blanc
                btn.setBackgroundTintList(android.content.res.ColorStateList
                        .valueOf(ContextCompat.getColor(this, R.color.purple_700)));
                btn.setTextColor(android.graphics.Color.WHITE);
            } else {
                // Style Non Sélectionné : Fond Blanc, Texte Noir
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                btn.setTextColor(android.graphics.Color.BLACK);
            }
        }
    }

    private void filterProducts() {
        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            // 1. Category Filter
            boolean matchesCategory = "Tout".equals(currentCategory) ||
                    (p.getCategory() != null && p.getCategory().equalsIgnoreCase(currentCategory));

            // 2. Search Filter
            boolean matchesSearch = searchQuery.isEmpty() ||
                    (p.getTitle() != null && p.getTitle().toLowerCase().contains(searchQuery)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchQuery));

            // 3. Price Filter
            boolean matchesPrice = true;
            if (minPrice != null && p.getPrice() < minPrice)
                matchesPrice = false;
            if (maxPrice != null && p.getPrice() > maxPrice)
                matchesPrice = false;

            // 4. Condition Filter
            boolean matchesCondition = selectedConditions.isEmpty() ||
                    (p.getCondition() != null && selectedConditions.contains(p.getCondition()));

            // 5. Distance Filter (only if user location is known)
            boolean matchesDistance = true;
            if (userLatitude != 0 && userLongitude != 0 && p.getLatitude() != 0 && p.getLongitude() != 0) {
                double dist = LocationHelper.calculateDistance(userLatitude, userLongitude,
                        p.getLatitude(), p.getLongitude());
                if (dist > maxDistance)
                    matchesDistance = false;
            }

            // 6. Verified Filter
            boolean matchesVerified = true;
            if (verifiedOnly) {
                // Pour l'instant, on considère que "seller1" est le seul vendeur vérifié
                // Dans une vraie app, ce serait un champ dans l'objet User ou Product
                matchesVerified = "seller1".equals(p.getSellerId());
            }

            if (matchesCategory && matchesSearch && matchesPrice && matchesCondition && matchesDistance
                    && matchesVerified) {
                filtered.add(p);
            }
        }

        // Sort
        switch (currentSortOption) {
            case PRICE_ASC:
                filtered.sort(Comparator.comparingDouble(Product::getPrice));
                break;
            case PRICE_DESC:
                filtered.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case NEWEST:
                // Assuming higher ID means newer for now, or if there was a date field
                filtered.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                break;
            case DISTANCE:
                if (userLatitude != 0 && userLongitude != 0) {
                    filtered.sort((p1, p2) -> {
                        double dist1 = LocationHelper.calculateDistance(userLatitude, userLongitude,
                                p1.getLatitude(), p1.getLongitude());
                        double dist2 = LocationHelper.calculateDistance(userLatitude, userLongitude,
                                p2.getLatitude(), p2.getLongitude());
                        return Double.compare(dist1, dist2);
                    });
                }
                break;
            case RATING:
                // Assuming rating field exists
                filtered.sort((p1, p2) -> Float.compare(p2.getRating(), p1.getRating())); // Descending
                break;
            case RELEVANCE:
            default:
                // Default order (usually by ID desc or as fetched)
                // If search query exists, maybe prioritize title match? For now keep simple.
                break;
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
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.VISIBLE);
                if (tvEmptyState != null) {
                    tvEmptyState.setText("Aucun produit trouvé");
                }
            } else if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Aucun produit trouvé");
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (emptyStateContainer != null) {
                emptyStateContainer.setVisibility(View.GONE);
            }
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new ProductAdapter(this, filtered, this);
        recyclerView.setAdapter(adapter);

        if (!filtered.isEmpty()) {
            // Toast.makeText(this, filtered.size() + " produits trouvés",
            // Toast.LENGTH_SHORT).show();
        }
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

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_filters);

        TextInputEditText etPriceMin = dialog.findViewById(R.id.etPriceMin);
        TextInputEditText etPriceMax = dialog.findViewById(R.id.etPriceMax);
        ChipGroup chipGroupCondition = dialog.findViewById(R.id.chipGroupCondition);
        Slider sliderDistance = dialog.findViewById(R.id.sliderDistance);
        TextView tvDistanceValue = dialog.findViewById(R.id.tvDistanceValue);
        SwitchMaterial switchVerified = dialog.findViewById(R.id.switchVerified);
        TextView tvReset = dialog.findViewById(R.id.tvReset);
        Button btnApply = dialog.findViewById(R.id.btnApplyFilters);

        // Init values
        if (minPrice != null)
            etPriceMin.setText(String.valueOf(minPrice));
        if (maxPrice != null)
            etPriceMax.setText(String.valueOf(maxPrice));

        // Init chips
        if (chipGroupCondition != null) {
            for (int i = 0; i < chipGroupCondition.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCondition.getChildAt(i);
                if (selectedConditions.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }

        // Init slider
        if (sliderDistance != null && tvDistanceValue != null) {
            int displayValue = Math.min(maxDistance, 100);
            sliderDistance.setValue(displayValue);
            tvDistanceValue.setText(displayValue >= 100 ? "100+ km" : displayValue + " km");

            sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
                if (value >= 100) {
                    tvDistanceValue.setText("100+ km");
                } else {
                    tvDistanceValue.setText((int) value + " km");
                }
            });
        }

        // Init switch
        if (switchVerified != null) {
            switchVerified.setChecked(verifiedOnly);
        }

        // Reset logic
        if (tvReset != null) {
            tvReset.setOnClickListener(v -> {
                etPriceMin.setText("");
                etPriceMax.setText("");
                if (chipGroupCondition != null)
                    chipGroupCondition.clearCheck();
                if (sliderDistance != null)
                    sliderDistance.setValue(100);
                if (switchVerified != null)
                    switchVerified.setChecked(false);
            });
        }

        // Apply logic
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                // Price
                String minStr = etPriceMin.getText().toString();
                String maxStr = etPriceMax.getText().toString();
                minPrice = minStr.isEmpty() ? null : Double.parseDouble(minStr);
                maxPrice = maxStr.isEmpty() ? null : Double.parseDouble(maxStr);

                // Conditions
                selectedConditions.clear();
                if (chipGroupCondition != null) {
                    for (int id : chipGroupCondition.getCheckedChipIds()) {
                        Chip chip = dialog.findViewById(id);
                        if (chip != null)
                            selectedConditions.add(chip.getText().toString());
                    }
                }

                // Distance
                if (sliderDistance != null) {
                    float val = sliderDistance.getValue();
                    if (val >= 100) {
                        maxDistance = 10000; // Unlimited
                    } else {
                        maxDistance = (int) val;
                    }
                }

                // Verified
                if (switchVerified != null) {
                    verifiedOnly = switchVerified.isChecked();
                }

                filterProducts();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showSortDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_sort);

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupSort);

        // Set current selection
        if (radioGroup != null) {
            switch (currentSortOption) {
                case RELEVANCE:
                    radioGroup.check(R.id.rbRelevance);
                    break;
                case PRICE_ASC:
                    radioGroup.check(R.id.rbPriceLowHigh);
                    break;
                case PRICE_DESC:
                    radioGroup.check(R.id.rbPriceHighLow);
                    break;
                case NEWEST:
                    radioGroup.check(R.id.rbNewest);
                    break;
                case DISTANCE:
                    radioGroup.check(R.id.rbDistance);
                    break;
                case RATING:
                    radioGroup.check(R.id.rbRating);
                    break;
            }

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbRelevance)
                    currentSortOption = SortOption.RELEVANCE;
                else if (checkedId == R.id.rbPriceLowHigh)
                    currentSortOption = SortOption.PRICE_ASC;
                else if (checkedId == R.id.rbPriceHighLow)
                    currentSortOption = SortOption.PRICE_DESC;
                else if (checkedId == R.id.rbNewest)
                    currentSortOption = SortOption.NEWEST;
                else if (checkedId == R.id.rbDistance)
                    currentSortOption = SortOption.DISTANCE;
                else if (checkedId == R.id.rbRating)
                    currentSortOption = SortOption.RATING;

                filterProducts();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void getUserLocation() {
        if (locationHelper.hasLocationPermission()) {
            locationHelper.getCurrentLocation(new LocationHelper.LocationListener() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String address) {
                    userLatitude = latitude;
                    userLongitude = longitude;
                    // Recharger les produits si tri par proximité activé
                    if (currentSortOption == SortOption.DISTANCE) {
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

            // Rediriger vers l'écran de connexion
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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