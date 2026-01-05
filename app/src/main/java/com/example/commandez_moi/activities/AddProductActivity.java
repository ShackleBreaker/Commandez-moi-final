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
import com.example.commandez_moi.utils.ThemeManager;

import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.LocationHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private ImageView imgPreview;
    private String base64Image = null;
    private DatabaseService db;
    private Spinner spinnerCategory;
    private Spinner spinnerCondition;
    private TextView tvLocation;
    private Button btnGetLocation;
    private LocationHelper locationHelper;
    private double productLatitude = 0;
    private double productLongitude = 0;
    private String productLocation = "";

    private final String[] CATEGORIES = { "Tech", "Mode", "Maison", "Beauté", "Sport", "Divers" };
    private final String[] CONDITIONS = { "Neuf", "Très bon état", "Bon état", "Correct" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_add_product);

        db = DatabaseService.getInstance(this);
        User currentUser = db.getCurrentUser();
        locationHelper = new LocationHelper(this);

        imgPreview = findViewById(R.id.imgPreview);
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etDesc = findViewById(R.id.etDesc);
        Button btnUpload = findViewById(R.id.btnUpload);
        Button btnPublish = findViewById(R.id.btnPublish);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        tvLocation = findViewById(R.id.tvLocation);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        // Configuration des Spinners
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CONDITIONS);
        spinnerCondition.setAdapter(conditionAdapter);

        // Bouton localisation
        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> showLocationDialog());
        }

        // Récupérer automatiquement la localisation au démarrage si pas encore définie
        if (productLocation.isEmpty()) {
            getCurrentLocation();
        }

        // 1. Ouvrir la galerie
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        // 2. Sauvegarder
        btnPublish.setOnClickListener(v -> {
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
            if (base64Image == null) {
                Toast.makeText(this, "Image obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = spinnerCategory.getSelectedItem().toString();
            String condition = spinnerCondition.getSelectedItem().toString();

            Product p = new Product(
                    0,
                    title,
                    Double.parseDouble(priceStr),
                    category,
                    etDesc.getText().toString(),
                    base64Image,
                    currentUser.getId(),
                    condition);

            // Ajouter la localisation
            p.setLatitude(productLatitude);
            p.setLongitude(productLongitude);
            p.setLocation(productLocation);

            db.addProduct(p);
            Toast.makeText(this, "Produit ajouté !", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showLocationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Définir la localisation du produit");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        input.setHint("Entrez une ville ou adresse");
        if (!productLocation.isEmpty()) {
            input.setText(productLocation);
        }
        builder.setView(input);

        builder.setPositiveButton("Valider l'adresse", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (!address.isEmpty()) {
                getLocationFromAddress(address);
            }
        });

        builder.setNeutralButton("Utiliser ma position GPS", (dialog, which) -> {
            getCurrentLocation();
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void getLocationFromAddress(String addressStr) {
        if (btnGetLocation != null) {
            btnGetLocation.setText("Recherche...");
            btnGetLocation.setEnabled(false);
        }

        locationHelper.getCoordinatesFromAddress(addressStr, new LocationHelper.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String address) {
                updateLocationUI(latitude, longitude, address);
            }

            @Override
            public void onLocationError(String error) {
                // Fallback: on garde l'adresse texte même sans coordonnées précises
                updateLocationUI(0, 0, addressStr);
                Toast.makeText(AddProductActivity.this, "Coordonnées non trouvées, adresse textuelle utilisée",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentLocation() {
        if (locationHelper.hasLocationPermission()) {
            if (btnGetLocation != null) {
                btnGetLocation.setText("Localisation en cours...");
                btnGetLocation.setEnabled(false);
            }

            locationHelper.getCurrentLocation(new LocationHelper.LocationListener() {
                @Override
                public void onLocationReceived(double latitude, double longitude, String address) {
                    updateLocationUI(latitude, longitude, address);
                }

                @Override
                public void onLocationError(String error) {
                    runOnUiThread(() -> {
                        if (btnGetLocation != null) {
                            btnGetLocation.setText("Définir la localisation");
                            btnGetLocation.setEnabled(true);
                        }
                        Toast.makeText(AddProductActivity.this, "Erreur GPS: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            locationHelper.requestLocationPermission(this);
        }
    }

    private void updateLocationUI(double latitude, double longitude, String address) {
        productLatitude = latitude;
        productLongitude = longitude;
        productLocation = address;

        runOnUiThread(() -> {
            if (tvLocation != null) {
                tvLocation.setVisibility(View.VISIBLE);
                tvLocation.setText("📍 " + address);
            }
            if (btnGetLocation != null) {
                btnGetLocation.setText("Modifier la localisation");
                btnGetLocation.setEnabled(true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    // 3. Réception et Conversion de l'image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                // Afficher
                imgPreview.setImageBitmap(selectedImage);

                // COMPRESSION ET CONVERSION BASE64
                // Important : On réduit la taille pour ne pas faire planter l'appli
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
}