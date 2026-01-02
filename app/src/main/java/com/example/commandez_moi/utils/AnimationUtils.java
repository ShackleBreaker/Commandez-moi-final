package com.example.commandez_moi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.commandez_moi.R;

public class AnimationUtils {

    public static void startActivityWithSlide(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void finishWithSlide(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public static void startActivityWithFade(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void finishWithFade(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
