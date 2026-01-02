package com.example.commandez_moi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    private List<Conversation> conversations;
    private String currentUserId;
    private OnConversationClickListener listener;

    public ConversationAdapter(List<Conversation> conversations, String currentUserId,
            OnConversationClickListener listener) {
        this.conversations = conversations;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.tvUserName.setText(conversation.getOtherUserName(currentUserId));

        // Product title if available
        if (conversation.getProductTitle() != null && !conversation.getProductTitle().isEmpty()) {
            holder.tvProductTitle.setVisibility(View.VISIBLE);
            holder.tvProductTitle.setText("📦 " + conversation.getProductTitle());
        } else {
            holder.tvProductTitle.setVisibility(View.GONE);
        }

        // Last message
        if (conversation.getLastMessage() != null) {
            holder.tvLastMessage.setText(conversation.getLastMessage());
        } else {
            holder.tvLastMessage.setText("Nouvelle conversation");
        }

        // Time
        holder.tvTime.setText(formatTime(conversation.getLastMessageTime()));

        // Profile photo
        String photo = conversation.getOtherUserPhoto(currentUserId);
        if (photo != null && !photo.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(photo)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_profile);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "À l'instant";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " min";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " h";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " j";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.FRANCE);
            return sdf.format(new Date(timestamp));
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgProfile;
        TextView tvUserName, tvProductTitle, tvLastMessage, tvTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvProductTitle = itemView.findViewById(R.id.tvProductTitle);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
