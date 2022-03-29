package com.example.ridesharecapstone.util;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ToastUtils {
    public static void toast(AppCompatActivity activity, String message) {
        activity.runOnUiThread(() -> {
            if (message.length() > 48)
                Toast.makeText(activity.getBaseContext(), message, Toast.LENGTH_LONG).show();
            else Toast.makeText(activity.getBaseContext(), message, Toast.LENGTH_SHORT).show();
        });
    }
}
