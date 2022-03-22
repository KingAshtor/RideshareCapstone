package com.example.ridesharecapstone;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.enqueue;
import static com.example.ridesharecapstone.util.Hash.doubleHash;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharecapstone.api.User;
import com.example.ridesharecapstone.api.Ride;

import java.util.Arrays;
import java.util.regex.Matcher;

public class RidePage extends AppCompatActivity {

    private String txt(TextView view) {
        return view.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_page);

        //assign values to everything on page
        final Button submit = findViewById(R.id.submitBtn); //declares submit button as a button

        final TextView date = findViewById(R.id.dateInput); //declares date field as a textView
        final TextView time = findViewById(R.id.timeInput); //declares time field as a textView
        final TextView pickup = findViewById(R.id.pickupInput); //declares pickup field as a textView
        final TextView destination = findViewById(R.id.destinationInput); //declares destination field as a textView

        //Submit button listener
        submit.setOnClickListener(view -> {
//            final Matcher pickupMatcher = ADDRESS_PATTERN.matcher(txt(pickup));
//            final Matcher destinationMatcher = ADDRESS_PATTERN.matcher(txt(destination));

            //Test form data
            if (Arrays.asList(
                    txt(date), txt(time), txt(pickup), txt(destination)
            ).contains(""))
                return; //First test to make sure no fields are blank, then tests to make sure destination and pickup are valid addresses
        });

    }
}