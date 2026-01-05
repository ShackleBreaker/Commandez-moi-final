package com.example.commandez_moi.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ReviewAdapter;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.Review;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View tvEmptyState;
    private TextView tvAverageRating, tvReviewCount;
    private RatingBar ratingBarAverage;
    private Button btnAddReview;
    private View addReviewSection;

    private DatabaseService db;
    private String productId;
    private Product product;
    private List<Review> reviews;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_reviews);

        db = DatabaseService.getInstance(this);
        productId = getIntent().getStringExtra("productId");

        if (productId == null) {
            Toast.makeText(this, "Erreur: produit non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProduct();
        loadReviews();
        setupListeners();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);
        btnAddReview = findViewById(R.id.btnAddReview);
        addReviewSection = findViewById(R.id.addReviewSection);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviews = new ArrayList<>();
        adapter = new ReviewAdapter(reviews);
        recyclerView.setAdapter(adapter);

        // Masquer le bouton si l'utilisateur n'est pas connecté ou est le vendeur
        if (!db.isLoggedIn()) {
            addReviewSection.setVisibility(View.GONE);
        }
    }

    private void loadProduct() {
        List<Product> products = db.getProducts();
        for (Product p : products) {
            if (String.valueOf(p.getId()).equals(productId)) {
                product = p;
                break;
            }
        }

        if (product != null && db.isLoggedIn()) {
            String currentUserId = db.getCurrentUserId();
            if (currentUserId.equals(product.getSellerId())) {
                // Le vendeur ne peut pas noter son propre produit
                addReviewSection.setVisibility(View.GONE);
            }

            // Vérifier si l'utilisateur a déjà donné un avis
            for (Review review : db.getReviews(productId)) {
                if (review.getUserId().equals(currentUserId)) {
                    btnAddReview.setText("Modifier mon avis");
                    break;
                }
            }
        }
    }

    private void loadReviews() {
        reviews.clear();
        reviews.addAll(db.getReviews(productId));
        adapter.notifyDataSetChanged();

        if (reviews.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        updateAverageRating();
    }

    private void updateAverageRating() {
        if (reviews.isEmpty()) {
            tvAverageRating.setText("0.0");
            tvReviewCount.setText("0 avis");
            ratingBarAverage.setRating(0);
        } else {
            float sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            float avg = sum / reviews.size();
            tvAverageRating.setText(String.format("%.1f", avg));
            tvReviewCount.setText(reviews.size() + " avis");
            ratingBarAverage.setRating(avg);
        }
    }

    private void setupListeners() {
        btnAddReview.setOnClickListener(v -> showAddReviewDialog());
    }

    private void showAddReviewDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        dialog.setContentView(view);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Pré-remplir si modification
        Review existingReview = null;
        String currentUserId = db.getCurrentUserId();
        for (Review r : reviews) {
            if (r.getUserId().equals(currentUserId)) {
                existingReview = r;
                ratingBar.setRating(r.getRating());
                etComment.setText(r.getComment());
                break;
            }
        }

        final Review finalExistingReview = existingReview;

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Veuillez donner une note", Toast.LENGTH_SHORT).show();
                return;
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Veuillez écrire un commentaire", Toast.LENGTH_SHORT).show();
                return;
            }

            User currentUser = db.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vous devez être connecté", Toast.LENGTH_SHORT).show();
                return;
            }

            Review review;
            if (finalExistingReview != null) {
                // Mise à jour
                review = finalExistingReview;
                review.setRating(rating);
                review.setComment(comment);
                review.setTimestamp(System.currentTimeMillis());
            } else {
                // Nouvel avis
                review = new Review(productId, currentUser.getId(), currentUser.getName(), rating, comment);
                if (currentUser.getProfileImage() != null) {
                    review.setUserPhoto(currentUser.getProfileImage());
                }
            }

            db.addReview(review);
            Toast.makeText(this, "Avis enregistré !", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadReviews();

            // Mettre à jour la note du produit
            updateProductRating();
        });

        dialog.show();
    }

    private void updateProductRating() {
        if (product != null && !reviews.isEmpty()) {
            float sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            float avg = sum / reviews.size();
            product.setRating(avg);
            db.updateProduct(product);
        }
    }
}
