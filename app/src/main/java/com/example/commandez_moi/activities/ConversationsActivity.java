package com.example.commandez_moi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commandez_moi.R;
import com.example.commandez_moi.adapters.ConversationAdapter;
import com.example.commandez_moi.models.Conversation;
import com.example.commandez_moi.services.DatabaseService;
import com.example.commandez_moi.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity
        implements ConversationAdapter.OnConversationClickListener {

    private RecyclerView recyclerView;
    private View emptyState;
    private DatabaseService db;
    private ConversationAdapter adapter;
    private List<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_conversations);

        db = DatabaseService.getInstance(this);

        if (!db.isLoggedIn()) {
            finish();
            return;
        }

        initViews();
        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        emptyState = findViewById(R.id.emptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversations = new ArrayList<>();
        adapter = new ConversationAdapter(conversations, db.getCurrentUserId(), this);
        recyclerView.setAdapter(adapter);
    }

    private void loadConversations() {
        String userId = db.getCurrentUserId();
        conversations.clear();
        conversations.addAll(db.getUserConversations(userId));
        adapter.notifyDataSetChanged();

        if (conversations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        String currentUserId = db.getCurrentUserId();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserId", conversation.getOtherUserId(currentUserId));
        intent.putExtra("otherUserName", conversation.getOtherUserName(currentUserId));
        intent.putExtra("productId", conversation.getProductId());
        intent.putExtra("productTitle", conversation.getProductTitle());
        startActivity(intent);
    }
}
