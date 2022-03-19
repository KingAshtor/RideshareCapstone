package com.example.ridesharecapstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button login = findViewById(R.id.loginBtn);
        login.setOnClickListener(view -> startActivity(new Intent(this, Login.class)));

        final Button register = findViewById(R.id.signUpBtn);
        register.setOnClickListener(view -> startActivity(new Intent(this, CreateAccount.class)));
    }
}