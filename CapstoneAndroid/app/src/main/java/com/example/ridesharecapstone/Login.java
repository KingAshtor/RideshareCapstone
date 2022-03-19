package com.example.ridesharecapstone;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.enqueue;
import static com.example.ridesharecapstone.util.Hash.doubleHash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ridesharecapstone.api.User;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button login = findViewById(R.id.loginBtn);
        final TextView email = findViewById(R.id.emailInput);
        final TextView password = findViewById(R.id.passwordInput);

        login.setOnClickListener(view -> enqueue((user, response) -> {
            final String actualPwd = user.getHashedPwd(),
                    expectedPwd = doubleHash(password.getText().toString(), user.getSalt());
            if (!actualPwd.equals(expectedPwd)) {
                Toast.makeText(getBaseContext(), "There is no account that matches these details.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getBaseContext(), "Logged in.",
                    Toast.LENGTH_SHORT).show();
            toProfile();
        }, this, API.getUser(email.getText().toString()), User.class));
    }

    private void toProfile() {
        startActivity(new Intent(this, ProfilePage.class));
    }
}