package com.example.commandez_moi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.CartItem;
import com.example.commandez_moi.utils.ImageUtils;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> items;
    private CartListener listener;

    public interface CartListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> items, CartListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(String.format("%.2f €", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        ImageUtils.loadImage(holder.imgProduct, item.getImageUrl());

        holder.btnPlus.setOnClickListener(v -> listener.onQuantityChanged(item, item.getQuantity() + 1));
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(item, item.getQuantity() - 1);
            }
        });
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvQuantity;
        ImageView imgProduct;
        ImageButton btnPlus, btnMinus, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}