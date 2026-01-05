package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.Review;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.example.commandez_moi.utils.ImageUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProductDetailActivity extends AppCompatActivity {

    private DatabaseService db;
    private Product product;
    private boolean isFavorite = false;

    private ImageView imgDetail, btnBack, btnFavorite;
    private TextView tvTitle, tvPrice, tvDesc, tvCategory, tvLocation, tvConditionBadge;
    private TextView tvRating, tvReviewCount, btnViewReviews, tvSellerName;
    private RatingBar ratingBar;
    private LinearLayout locationSection, ratingSection;
    private CircleImageView imgSeller;
    private Button btnAddToCart, btnContactSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_product_detail);

        db = DatabaseService.getInstance(this);

        product = (Product) getIntent().getSerializableExtra("product_obj");

        if (product == null) {
            Toast.makeText(this, "Erreur chargement produit", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateData();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviewsInfo();
    }

    private void initViews() {
        imgDetail = findViewById(R.id.imgDetail);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvCategory = findViewById(R.id.tvCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvConditionBadge = findViewById(R.id.tvConditionBadge);
        locationSection = findViewById(R.id.locationSection);
        ratingSection = findViewById(R.id.ratingSection);
        ratingBar = findViewById(R.id.ratingBar);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        tvSellerName = findViewById(R.id.tvSellerName);
        imgSeller = findViewById(R.id.imgSeller);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnContactSeller = findViewById(R.id.btnContactSeller);
    }

    private void populateData() {
        // Image principale
        ImageUtils.loadImage(imgDetail, product.getImageUrl());

        // Infos de base
        tvTitle.setText(product.getTitle());
        tvPrice.setText(String.format("%.2f €", product.getPrice()));
        tvDesc.setText(product.getDescription());
        tvCategory.setText(product.getCategory());

        // État du produit
        String condition = product.getCondition();
        if (condition != null && !condition.isEmpty()) {
            tvConditionBadge.setText(condition);
            if ("Neuf".equals(condition)) {
                tvConditionBadge.getBackground().setTint(0xFF4CAF50);
            } else if ("Comme neuf".equals(condition)) {
                tvConditionBadge.getBackground().setTint(getResources().getColor(R.color.purple_700, null));
            } else {
                tvConditionBadge.getBackground().setTint(getResources().getColor(R.color.orange_primary, null));
            }
        }

        // Localisation
        if (product.getLocation() != null && !product.getLocation().isEmpty()) {
            locationSection.setVisibility(View.VISIBLE);
            tvLocation.setText(product.getLocation());
        }

        // Favoris
        if (db.isLoggedIn()) {
            isFavorite = db.isProductFavorite(db.getCurrentUserId(), product.getId());
            updateFavoriteIcon();
        }

        // Vendeur
        loadSellerInfo();

        // Avis
        loadReviewsInfo();
    }

    private void loadSellerInfo() {
        User seller = db.getUserById(product.getSellerId());
        if (seller != null) {
            tvSellerName.setText(seller.getName());
            if (seller.getProfileImage() != null && !seller.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(seller.getProfileImage())
                        .placeholder(R.drawable.ic_profile)
                        .into(imgSeller);
            }
        } else {
            tvSellerName.setText("Vendeur");
        }
    }

    private void loadReviewsInfo() {
        List<Review> reviews = db.getReviews(String.valueOf(product.getId()));

        if (reviews.isEmpty()) {
            ratingBar.setRating(0);
            tvRating.setText("0.0");
            tvReviewCount.setText("(0 avis)");
        } else {
            float sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            float avg = sum / reviews.size();
            ratingBar.setRating(avg);
            tvRating.setText(String.format("%.1f", avg));
            tvReviewCount.setText("(" + reviews.size() + " avis)");
        }
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnFavorite.setOnClickListener(v -> {
            if (!db.isLoggedIn()) {
                Toast.makeText(this, "Connectez-vous pour ajouter aux favoris", Toast.LENGTH_SHORT).show();
                return;
            }
            db.toggleFavorite(db.getCurrentUserId(), product.getId());
            isFavorite = !isFavorite;
            updateFavoriteIcon();
            Toast.makeText(this, isFavorite ? "Ajouté aux favoris" : "Retiré des favoris", Toast.LENGTH_SHORT).show();
        });

        btnAddToCart.setOnClickListener(v -> {
            db.addToCart(product);
            Toast.makeText(this, "Ajouté au panier !", Toast.LENGTH_SHORT).show();
        });

        ratingSection.setOnClickListener(v -> openReviews());
        btnViewReviews.setOnClickListener(v -> openReviews());

        btnContactSeller.setOnClickListener(v -> {
            if (!db.isLoggedIn()) {
                Toast.makeText(this, "Connectez-vous pour contacter le vendeur", Toast.LENGTH_SHORT).show();
                return;
            }

            // Vérifier que ce n'est pas le vendeur lui-même
            if (db.getCurrentUserId().equals(product.getSellerId())) {
                Toast.makeText(this, "C'est votre propre produit", Toast.LENGTH_SHORT).show();
                return;
            }

            User seller = db.getUserById(product.getSellerId());
            String sellerName = seller != null ? seller.getName() : "Vendeur";

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("otherUserId", product.getSellerId());
            chatIntent.putExtra("otherUserName", sellerName);
            chatIntent.putExtra("productId", String.valueOf(product.getId()));
            chatIntent.putExtra("productTitle", product.getTitle());
            startActivity(chatIntent);
        });
    }

    private void openReviews() {
        Intent intent = new Intent(this, ReviewsActivity.class);
        intent.putExtra("productId", String.valueOf(product.getId()));
        startActivity(intent);
    }
}