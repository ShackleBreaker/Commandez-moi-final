package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commandez_moi.R;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.AnimationUtils;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseService db;
    private SwitchMaterial switchDarkMode;
    private LinearLayout sellerSection;
    private TextView tvVerificationStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_settings);

        db = DatabaseService.getInstance(this);

        initViews();
        setupListeners();
        loadUserSettings();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            AnimationUtils.finishWithSlide(this);
        });

        switchDarkMode = findViewById(R.id.switchDarkMode);
        sellerSection = findViewById(R.id.sellerSection);
        tvVerificationStatus = findViewById(R.id.tvVerificationStatus);

        // Set current dark mode state
        switchDarkMode.setChecked(ThemeManager.isDarkMode(this));
    }

    private void setupListeners() {
        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.setDarkMode(this, isChecked);
        });

        // Edit profile
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            AnimationUtils.startActivityWithSlide(this, intent);
        });

        // Order history
        findViewById(R.id.btnOrderHistory).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            AnimationUtils.startActivityWithSlide(this, intent);
        });

        // Favorites
        findViewById(R.id.btnFavorites).setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoritesActivity.class);
            AnimationUtils.startActivityWithSlide(this, intent);
        });

        // Stats (seller only)
        findViewById(R.id.btnStats).setOnClickListener(v -> {
            Intent intent = new Intent(this, SellerStatsActivity.class);
            AnimationUtils.startActivityWithSlide(this, intent);
        });

        // Verification (seller only)
        findViewById(R.id.btnVerification).setOnClickListener(v -> {
            // TODO: Open verification flow
        });

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            db.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserSettings() {
        User user = db.getCurrentUser();
        if (user != null) {
            // Show seller section if user is seller
            if ("seller".equals(user.getRole())) {
                sellerSection.setVisibility(View.VISIBLE);

                if (user.isVerified()) {
                    tvVerificationStatus.setText("✓ Vérifié");
                    tvVerificationStatus.setTextColor(getResources().getColor(R.color.verified_blue));
                } else {
                    tvVerificationStatus.setText("Non vérifié - Cliquez pour postuler");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
