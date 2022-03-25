package com.example.ridesharecapstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class RideRequestComplete extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request_complete);

        final Button returnToProfile = findViewById(R.id.returnToProfileBtn); //declares needRB button as a button
        returnToProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfilePage.class)));
    }
}