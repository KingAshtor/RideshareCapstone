package com.example.ridesharecapstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.handle;
import static com.example.ridesharecapstone.util.ToastUtils.toast;


import static java.lang.String.format;

import com.example.ridesharecapstone.api.retrofit.Api;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

//implements added just for the time/date stuff, not needed for normal views
public class RidePage extends AppCompatActivity {
    private DatePicker datePicker;
    private TimePicker timePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_page);

        //assign buttons
        Button subBtn = findViewById(R.id.subBTN); //declares submit button as a button

        //assign pickup fields
        final TextView pickAddressLine1 = findViewById(R.id.pickAddressLine);
        final TextView pickAddressLine2 = findViewById(R.id.pickAddressLine2);
        final TextView pickCity = findViewById(R.id.pickCity);
        final TextView pickState = findViewById(R.id.pickState);
        final TextView pickZip = findViewById(R.id.pickZip);

        //assign destination fields
        final TextView destAddressLine1 = findViewById(R.id.destAddressLine1);
        final TextView destAddressLine2 = findViewById(R.id.destAddressLine2);
        final TextView destCity = findViewById(R.id.destCity);
        final TextView destState = findViewById(R.id.destState);
        final TextView destZip = findViewById(R.id.destZip);

        //assign dateTime stuff
        datePicker =  findViewById(R.id.datePicker);
        timePicker =  findViewById(R.id.timePicker);

        //context for enqueues
        final AppCompatActivity context = this; //saves the current context for later use

        //assign onClick listener to the submit button
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                                //pickAddressID.set(28);
                //destAddressID.set(29);
                //routeID.set(16);


                //makes the pickupAddress
                CompletableFuture.supplyAsync(() -> {
                    Api.ApiResponse<Integer>pickResponse = handle(API.addAddr(
                            txt(pickAddressLine1),
                            txt(pickAddressLine2),
                            txt(pickCity),
                            txt(pickState),
                            txt(pickZip)), Integer.class);
                    if (pickResponse.hasError() || pickResponse.getCode() != 200) {
                        toast(context, "An error occurred creating pickup addr.");
                        return false;
                    }

                    Api.ApiResponse<Integer>destResponse = handle(API.addAddr(
                            txt(destAddressLine1),
                            txt(destAddressLine2),
                            txt(destCity),
                            txt(destState),
                            txt(destZip)), Integer.class);
                    if (destResponse.hasError() || destResponse.getCode() != 200) {
                        toast(context, "An error occurred creating destination addr.");
                        return false;
                    }

                    Api.ApiResponse<Integer>routeResponse = handle(API.addRoute(
                            pickResponse.getBody(),
                            destResponse.getBody(),
                            8), Integer.class);
                    if (routeResponse.hasError() || routeResponse.getCode() != 200) {
                        toast(context, "An error occurred creating route.");
                        return false;
                    }


                    Api.ApiResponse<Integer>rideResponse = handle(API.addRide(
                            routeResponse.getBody(),
                            17,
                            dateTime(datePicker, timePicker)),
                            Integer.class);
                    if (rideResponse.hasError() || rideResponse.getCode() != 200) {
                        toast(context, "An error occurred creating ride.");
                        return false;
                    }

                    return true;
                }).thenAccept((success) -> {if (success) toRequestComplete();});
            }


        });
    }

    private void collectData() {
    }

    //used to get the text from a textview as a string
    private String txt(TextView view) {
        return view.getText().toString();
    }

    //used to get the date from the date field
    private String dateTime(DatePicker datePicker, TimePicker timePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        System.out.println("Formats broke");
        String output = format("%04d-%02d-%02dT%02d:%02d", year, month, day, hour, minute);

        System.out.println(output);

        return output;
    }

    //used to swap to the request complete activity
    public void toRequestComplete(){
        Intent intent = new Intent(this, RideRequestComplete.class);
        startActivity(intent);
    }




}