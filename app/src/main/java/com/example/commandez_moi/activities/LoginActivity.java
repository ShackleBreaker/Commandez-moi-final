package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.commandez_moi.R;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button registerButton;
    private TextView switchModeText;
    private TextView titleText;
    private TextView subtitleText;
    private View roleSection;
    private RadioGroup roleGroup;
    private DatabaseService dbService;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_login);

        dbService = DatabaseService.getInstance(this);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        switchModeText = findViewById(R.id.switchModeText);
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        roleSection = findViewById(R.id.roleSection);
        roleGroup = findViewById(R.id.roleGroup);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> attemptRegister());
        switchModeText.setOnClickListener(v -> toggleMode());

        updateUI();
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUI();
    }

    private void updateUI() {
        if (isLoginMode) {
            titleText.setText("Connexion");
            subtitleText.setText("Connectez-vous pour continuer");
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
            roleSection.setVisibility(View.GONE);
            switchModeText.setText("Pas de compte ? S'inscrire");
        } else {
            titleText.setText("Inscription");
            subtitleText.setText("Créez votre compte");
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            roleSection.setVisibility(View.VISIBLE);
            switchModeText.setText("Déjà un compte ? Se connecter");
        }
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbService.login(email, password);
        if (user != null) {
            Toast.makeText(this, "Connecté en tant que " + user.getRole(), Toast.LENGTH_SHORT).show();
            redirectToHome(user);
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToHome(User user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void attemptRegister() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRoleId = roleGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Veuillez choisir un rôle", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRole = findViewById(selectedRoleId);
        String role = selectedRole.getId() == R.id.radioBuyer ? "buyer" : "seller";

        // Vérifier si l'email existe déjà
        if (dbService.emailExists(email)) {
            Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer le nouvel utilisateur
        User newUser = new User(UUID.randomUUID().toString(), email, password, role);
        dbService.registerUser(newUser);
        dbService.setCurrentUser(newUser);

        Toast.makeText(this, "Inscription réussie ! Connecté en tant que " + role, Toast.LENGTH_SHORT).show();
        redirectToHome(newUser);
    }
}