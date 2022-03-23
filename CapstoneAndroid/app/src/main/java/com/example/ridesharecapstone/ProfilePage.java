package com.example.ridesharecapstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ProfilePage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        final Button needARide = findViewById(R.id.rideBtn); //declares needRB button as a button
        needARide.setOnClickListener(view -> startActivity(new Intent(this, RidePage.class)));
    }
}