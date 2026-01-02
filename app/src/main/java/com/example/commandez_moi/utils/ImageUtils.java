package com.example.commandez_moi.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class ImageUtils {

    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            try {
                // Nettoyer le préfixe "data:image/jpeg;base64,"
                String cleanBase64 = imageUrl.contains(",") ? imageUrl.split(",")[1] : imageUrl;
                byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                view.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Si c'est une URL normale (pour les mocks)
            Glide.with(view.getContext()).load(imageUrl).into(view);
        }
    }
}