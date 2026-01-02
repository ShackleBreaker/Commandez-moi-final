package com.example.commandez_moi.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.LocationHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class EditProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 102;

    private ImageView imgPreview;
    private EditText etTitle, etPrice, etDesc;
    private Spinner spinnerCategory, spinnerCondition;
    private TextView tvLocation;
    private Button btnUpload, btnSave, btnGetLocation;
    private DatabaseService db;
    private Product product;
    private String base64Image = null;
    private LocationHelper locationHelper;
    private double productLatitude = 0;
    private double productLongitude = 0;
    private String productLocation = "";

    private final String[] CATEGORIES = { "Tech", "Mode", "Maison", "Beauté", "Sport", "Divers" };
    private final String[] CONDITIONS = { "Neuf", "Très bon état", "Bon état", "Correct" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = DatabaseService.getInstance(this);
        locationHelper = new LocationHelper(this);

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "Erreur: produit non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupViews();
        populateFields();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Modifier le produit");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        imgPreview = findViewById(R.id.imgPreview);
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        etDesc = findViewById(R.id.etDesc);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        tvLocation = findViewById(R.id.tvLocation);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        // Setup spinners
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CONDITIONS);
        spinnerCondition.setAdapter(conditionAdapter);

        // Listeners
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getProductLocation());
        }

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void populateFields() {
        etTitle.setText(product.getTitle());
        etPrice.setText(String.valueOf(product.getPrice()));
        etDesc.setText(product.getDescription());

        // Sélectionner la catégorie
        int categoryIndex = Arrays.asList(CATEGORIES).indexOf(product.getCategory());
        if (categoryIndex >= 0) {
            spinnerCategory.setSelection(categoryIndex);
        }

        // Sélectionner la condition
        int conditionIndex = Arrays.asList(CONDITIONS).indexOf(product.getCondition());
        if (conditionIndex >= 0) {
            spinnerCondition.setSelection(conditionIndex);
        }

        // Image
        base64Image = product.getImageUrl();
        loadImage(product.getImageUrl());

        // Localisation
        productLatitude = product.getLatitude();
        productLongitude = product.getLongitude();
        productLocation = product.getLocation();
        if (productLocation != null && !productLocation.isEmpty()) {
            tvLocation.setVisibility(View.VISIBLE);
            tvLocation.setText("📍 " + productLocation);
        }
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            try {
                String cleanBase64 = imageUrl.split(",")[1];
                byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                Glide.with(this).load(decodedString).into(imgPreview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(this).load(imageUrl).into(imgPreview);
        }
    }

    private void getProductLocation() {
        if (locationHelper.hasLocationPermission()) {
            btnGetLocation.setText("Localisation...");
            btnGetLocation.setEnabled(false);

            locationHelper.getCurrentLocation(new LocationHelper.LocationListener() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String address) {
                    productLatitude = latitude;
                    productLongitude = longitude;
                    productLocation = address;

                    runOnUiThread(() -> {
                        tvLocation.setVisibility(View.VISIBLE);
                        tvLocation.setText("📍 " + address);
                        btnGetLocation.setText("Changer la localisation");
                        btnGetLocation.setEnabled(true);
                    });
                }

                @Override
                public void onLocationError(String error) {
                    runOnUiThread(() -> {
                        btnGetLocation.setText("Ajouter la localisation");
                        btnGetLocation.setEnabled(true);
                    });
                }
            });
        } else {
            locationHelper.requestLocationPermission(this);
        }
    }

    private void saveProduct() {
        String title = etTitle.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Titre obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Prix obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        product.setTitle(title);
        product.setPrice(Double.parseDouble(priceStr));
        product.setDescription(etDesc.getText().toString());
        product.setCategory(spinnerCategory.getSelectedItem().toString());
        product.setCondition(spinnerCondition.getSelectedItem().toString());
        product.setImageUrl(base64Image);
        product.setLatitude(productLatitude);
        product.setLongitude(productLongitude);
        product.setLocation(productLocation);

        db.updateProduct(product);
        Toast.makeText(this, "Produit mis à jour !", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                imgPreview.setImageBitmap(selectedImage);

                Bitmap resized = Bitmap.createScaledBitmap(selectedImage, 400, 400, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] b = baos.toByteArray();

                base64Image = "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.DEFAULT);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getProductLocation();
            }
        }
    }
}
