package com.example.commandez_moi.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.MessageAdapter;
import com.example.commandez_moi.models.Conversation;
import com.example.commandez_moi.models.Message;
import com.example.commandez_moi.models.Product;
import com.example.commandez_moi.models.User;
import com.example.commandez_moi.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvUserName, tvProductTitle;
    private CircleImageView imgUser;
    private LinearLayout productInfoSection;
    private ImageView imgProduct;

    private DatabaseService db;
    private MessageAdapter adapter;
    private List<Message> messages;

    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String productId;
    private String productTitle;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = DatabaseService.getInstance(this);
        currentUser = db.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get intent extras
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        productId = getIntent().getStringExtra("productId");
        productTitle = getIntent().getStringExtra("productTitle");

        if (otherUserId == null) {
            Toast.makeText(this, "Erreur: utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate conversation ID
        conversationId = Conversation.generateConversationId(currentUser.getId(), otherUserId,
                productId != null ? productId : "general");

        initViews();
        setupRecyclerView();
        loadMessages();
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvUserName = findViewById(R.id.tvUserName);
        imgUser = findViewById(R.id.imgUser);
        productInfoSection = findViewById(R.id.productInfoSection);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        imgProduct = findViewById(R.id.imgProduct);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set user info
        tvUserName.setText(otherUserName != null ? otherUserName : "Utilisateur");

        User otherUser = db.getUserById(otherUserId);
        if (otherUser != null && otherUser.getProfileImage() != null && !otherUser.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(otherUser.getProfileImage())
                    .placeholder(R.drawable.ic_profile)
                    .into(imgUser);
        }

        // Show product info if available
        if (productId != null && productTitle != null) {
            productInfoSection.setVisibility(View.VISIBLE);
            tvProductTitle.setText(productTitle);

            Product product = db.getProductById(Integer.parseInt(productId));
            if (product != null && product.getImageUrl() != null) {
                Glide.with(this)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.bg_search)
                        .into(imgProduct);
            }
        } else {
            productInfoSection.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUser.getId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        messages.clear();
        messages.addAll(db.getMessages(conversationId));
        adapter.notifyDataSetChanged();

        // Scroll to bottom
        if (!messages.isEmpty()) {
            recyclerView.scrollToPosition(messages.size() - 1);
        }

        // Mark messages as read
        db.markMessagesAsRead(conversationId, currentUser.getId());
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            return;
        }

        Message message = new Message(
                conversationId,
                currentUser.getId(),
                currentUser.getName(),
                otherUserId,
                content);

        db.sendMessage(message, productId, productTitle, otherUserName);

        // Add to local list
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);

        // Clear input
        etMessage.setText("");
    }
}
