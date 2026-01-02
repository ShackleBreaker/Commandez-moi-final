package com.example.commandez_moi.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.commandez_moi.R;
import com.example.commandez_moi.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);

        void onFavoriteClick(Product product, int position);
    }

    // Ancien interface pour compatibilité
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    // Constructeur pour compatibilité avec l'ancien interface
    public ProductAdapter(Context context, List<Product> products, OnItemClickListener oldListener) {
        this.context = context;
        this.products = products;
        this.listener = new OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                oldListener.onItemClick(product);
            }

            @Override
            public void onFavoriteClick(Product product, int position) {
                // Ne rien faire par défaut
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvTitle.setText(product.getTitle());
        holder.tvPrice.setText(String.format("%.2f €", product.getPrice()));

        // Prix original barré si différent
        if (holder.tvOriginalPrice != null) {
            if (product.getOriginalPrice() > product.getPrice()) {
                holder.tvOriginalPrice.setVisibility(View.VISIBLE);
                holder.tvOriginalPrice.setText(String.format("%.2f €", product.getOriginalPrice()));
                holder.tvOriginalPrice
                        .setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvOriginalPrice.setVisibility(View.GONE);
            }
        }

        // Condition du produit
        if (holder.tvCondition != null) {
            holder.tvCondition.setText(product.getCondition());
        }

        // Localisation
        if (holder.tvLocation != null && product.getLocation() != null) {
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(product.getLocation());
        } else if (holder.tvLocation != null) {
            holder.tvLocation.setVisibility(View.GONE);
        }

        // Rating
        if (holder.ratingBar != null) {
            holder.ratingBar.setRating(product.getRating());
        }
        if (holder.tvRatingCount != null) {
            if (product.getRatingCount() > 0) {
                holder.tvRatingCount.setVisibility(View.VISIBLE);
                holder.tvRatingCount.setText("(" + product.getRatingCount() + ")");
            } else {
                holder.tvRatingCount.setVisibility(View.GONE);
            }
        }

        // Bouton favori
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setImageResource(
                    product.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
            holder.btnFavorite.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFavoriteClick(product, adapterPosition);
                }
            });
        }

        // Gestion intelligente de l'image (Base64 ou URL)
        if (product.getImageUrl() != null && product.getImageUrl().startsWith("data:image")) {
            try {
                String cleanBase64 = product.getImageUrl().split(",")[1];
                byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                Glide.with(context)
                        .load(decodedString)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.imgProduct);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Fallback si c'est une URL normale
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(holder.imgProduct);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public void updateProductFavorite(int position, boolean isFavorite) {
        if (position >= 0 && position < products.size()) {
            products.get(position).setFavorite(isFavorite);
            notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvOriginalPrice, tvCondition, tvLocation, tvRatingCount;
        ImageView imgProduct;
        ImageButton btnFavorite;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}