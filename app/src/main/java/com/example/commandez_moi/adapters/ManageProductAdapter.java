package com.example.commandez_moi.adapters;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Product;

import java.util.List;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditClick(Product product);

        void onDeleteClick(Product product, int position);

        void onViewClick(Product product);
    }

    public ManageProductAdapter(Context context, List<Product> products, OnProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(String.format("%.2f €", product.getPrice()));
        holder.tvCategory.setText(product.getCategory());
        holder.tvCondition.setText(product.getCondition());

        // Image
        if (product.getImageUrl() != null && product.getImageUrl().startsWith("data:image")) {
            try {
                String cleanBase64 = product.getImageUrl().split(",")[1];
                byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                Glide.with(context).load(decodedString).into(holder.imgProduct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.imgProduct);
        }

        // Click listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(product));
        holder.btnDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(product, adapterPosition);
            }
        });
        holder.itemView.setOnClickListener(v -> listener.onViewClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvCategory, tvCondition;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
