package com.example.commandez_moi.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.utils.ThemeManager;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Order;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 101;

    private TextView userName, userEmail, userRole, userLocation;
    private TextView tvProductCount, tvSalesCount, tvRating;
    private Button btnAddProduct, btnDashboard, btnOrderHistory, btnLogout, btnManageProducts;
    private View sellerSection;
    private CardView statsCard;
    private ImageView profileImage, btnEditPhoto, btnEditLocation;
    private DatabaseService db;
    private User currentUser;
    private com.example.commandez_moi.utils.LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_profile);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();
        locationHelper = new com.example.commandez_moi.utils.LocationHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mon Profil");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Vues
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userRole = findViewById(R.id.userRole);
        userLocation = findViewById(R.id.userLocation);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnDashboard = findViewById(R.id.btnDashboard);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        sellerSection = findViewById(R.id.sellerSection);
        statsCard = findViewById(R.id.statsCard);
        profileImage = findViewById(R.id.profileImage);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        btnEditLocation = findViewById(R.id.btnEditLocation);
        tvProductCount = findViewById(R.id.tvProductCount);
        tvSalesCount = findViewById(R.id.tvSalesCount);
        tvRating = findViewById(R.id.tvRating);

        setupUserInfo();
        setupClickListeners();
    }

    private void setupUserInfo() {
        if (currentUser == null)
            return;

        userEmail.setText(currentUser.getEmail());
        userName.setText(currentUser.getName() != null ? currentUser.getName() : currentUser.getEmail().split("@")[0]);

        if (currentUser.getLocation() != null && !currentUser.getLocation().isEmpty()) {
            userLocation.setText(currentUser.getLocation());
        } else {
            userLocation.setText("Localisation non définie");
        }

        // Photo de profil
        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            loadProfileImage(currentUser.getProfileImage());
        }

        if ("seller".equals(currentUser.getRole())) {
            userRole.setText("Vendeur");
            userRole.setTextColor(getResources().getColor(R.color.purple_700));
            sellerSection.setVisibility(View.VISIBLE);
            statsCard.setVisibility(View.VISIBLE);
            loadSellerStats();
        } else {
            userRole.setText("Acheteur");
            userRole.setTextColor(0xFF2196F3);
            sellerSection.setVisibility(View.GONE);
            statsCard.setVisibility(View.GONE);
        }
    }

    private void loadSellerStats() {
        // Compter les produits du vendeur
        List<Product> allProducts = db.getProducts();
        int productCount = 0;
        for (Product p : allProducts) {
            if (currentUser.getId().equals(p.getSellerId())) {
                productCount++;
            }
        }
        tvProductCount.setText(String.valueOf(productCount));

        // Compter les ventes (commandes contenant les produits du vendeur)
        List<Order> allOrders = db.getOrders();
        int salesCount = 0;
        for (Order order : allOrders) {
            if (order.getItems() != null) {
                for (var item : order.getItems()) {
                    if (currentUser.getId().equals(item.getSellerId())) {
                        salesCount++;
                    }
                }
            }
        }
        tvSalesCount.setText(String.valueOf(salesCount));

        // Note moyenne (pour l'instant fixe, sera calculée avec les reviews)
        tvRating.setText("4.5");
    }

    private void setupClickListeners() {
        // Changer photo de profil
        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> pickProfileImage());
        }
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> pickProfileImage());
        }

        if (btnEditLocation != null) {
            btnEditLocation.setOnClickListener(v -> showLocationDialog());
        }

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));

        btnDashboard.setOnClickListener(v -> startActivity(new Intent(this, SellerDashboardActivity.class)));

        if (btnManageProducts != null) {
            btnManageProducts.setOnClickListener(v -> startActivity(new Intent(this, ManageProductsActivity.class)));
        }

        btnOrderHistory.setOnClickListener(v -> startActivity(new Intent(this, OrderHistoryActivity.class)));

        btnLogout.setOnClickListener(v -> {
            db.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void pickProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                // Afficher immédiatement
                profileImage.setImageBitmap(selectedImage);

                // Compression et conversion Base64
                Bitmap resized = Bitmap.createScaledBitmap(selectedImage, 200, 200, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] b = baos.toByteArray();
                String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.DEFAULT);

                // Sauvegarder
                currentUser.setProfileImage(base64Image);
                db.updateUser(currentUser);

                Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLocationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Modifier la localisation");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setHint("Entrez votre ville ou adresse");
        if (currentUser.getLocation() != null) {
            input.setText(currentUser.getLocation());
        }
        builder.setView(input);

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String newLocation = input.getText().toString().trim();
            if (!newLocation.isEmpty()) {
                updateLocation(newLocation);
            }
        });
        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.setNeutralButton("Utiliser ma position actuelle", (dialog, which) -> {
            useCurrentLocation();
        });

        builder.show();
    }

    private void updateLocation(String locationName) {
        locationHelper.getCoordinatesFromAddress(locationName,
                new com.example.commandez_moi.utils.LocationHelper.LocationListener() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String address) {
                        currentUser.setLocation(address);
                        currentUser.setLatitude(latitude);
                        currentUser.setLongitude(longitude);
                        db.updateUser(currentUser);
                        userLocation.setText(address);
                        Toast.makeText(ProfileActivity.this, "Localisation mise à jour", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLocationError(String error) {
                        currentUser.setLocation(locationName);
                        db.updateUser(currentUser);
                        userLocation.setText(locationName);
                        Toast.makeText(ProfileActivity.this, "Localisation mise à jour (sans coordonnées précises)",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void useCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            locationHelper.getCurrentLocation(new com.example.commandez_moi.utils.LocationHelper.LocationListener() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String address) {
                    currentUser.setLocation(address);
                    currentUser.setLatitude(latitude);
                    currentUser.setLongitude(longitude);
                    db.updateUser(currentUser);
                    userLocation.setText(address);
                    Toast.makeText(ProfileActivity.this, "Localisation mise à jour", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLocationError(String error) {
                    Toast.makeText(ProfileActivity.this, "Erreur: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            locationHelper.requestLocationPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
            @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == com.example.commandez_moi.utils.LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                useCurrentLocation();
            } else {
                Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileImage(String imageData) {
        if (imageData.startsWith("data:image")) {
            try {
                String cleanBase64 = imageData.split(",")[1];
                byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                Glide.with(this).load(decodedString).into(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(this).load(imageData).into(profileImage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = db.getCurrentUser();
        setupUserInfo();
    }
}
