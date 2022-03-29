package com.example.ridesharecapstone;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.fromJson;
import static com.example.ridesharecapstone.util.Hash.doubleHash;
import static com.example.ridesharecapstone.util.ToastUtils.toast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridesharecapstone.api.User;
import com.example.ridesharecapstone.api.retrofit.Api;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button login = findViewById(R.id.loginBtn);
        final TextView emailView = findViewById(R.id.emailInput);
        final TextView passwordView = findViewById(R.id.passwordInput);

        login.setOnClickListener(view -> CompletableFuture.supplyAsync(() -> {
            final String email = emailView.getText().toString();
            final Api.ApiResponse<JsonObject> userResponse = Api.handle(API.getUser(email), JsonObject.class);

            if (userResponse.hasError() || userResponse.getCode() != 200) {
                toast(this, "An error occurred.");
                return false;
            }

            final User user = fromJson(userResponse.getBody(), User.class);
            final String actualPwd = user.getHashedPwd(),
                    expectedPwd = doubleHash(passwordView.getText().toString(), user.getSalt());
            if (!actualPwd.equals(expectedPwd)) {
                toast(this, "There is no account that matches these details.");
                return false;
            }

            toast(this, "Logged In.");
            return true;
        }).thenAccept(success -> { if (success) startActivity(new Intent(this, ProfilePage.class)); }));
    }
}