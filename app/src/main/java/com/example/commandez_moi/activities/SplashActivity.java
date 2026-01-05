package com.example.commandez_moi.activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.commandez_moi.R;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 secondes

    private LottieAnimationView animationView;
    private TextView tvAppName;
    private TextView tvTagline;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer le thème sauvegardé avant la création de la vue
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Masquer la barre d'action
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        databaseService = DatabaseService.getInstance(this);

        // Initialiser les vues
        animationView = findViewById(R.id.animation_view);
        tvAppName = findViewById(R.id.tv_app_name);
        tvTagline = findViewById(R.id.tv_tagline);

        // Animation de fade-in pour le texte
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        fadeIn.setStartOffset(500);
        fadeIn.setFillAfter(true);

        tvAppName.startAnimation(fadeIn);

        AlphaAnimation fadeIn2 = new AlphaAnimation(0.0f, 1.0f);
        fadeIn2.setDuration(1000);
        fadeIn2.setStartOffset(1000);
        fadeIn2.setFillAfter(true);
        tvTagline.startAnimation(fadeIn2);

        // Configurer l'animation Lottie
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // L'animation est terminée, naviguer vers l'écran suivant
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Naviguer vers l'écran suivant après le délai
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void navigateToNextScreen() {
        Intent intent;

        // Vérifier si l'utilisateur est déjà connecté
        if (databaseService.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);

        // Animation de transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    @Override
    public void onBackPressed() {
        // Désactiver le bouton retour sur l'écran splash
    }
}
