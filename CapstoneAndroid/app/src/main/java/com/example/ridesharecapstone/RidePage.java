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
import static com.example.ridesharecapstone.api.retrofit.Api.enqueue;

import java.util.Calendar;
import java.util.Date;
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

        //context for enqueues
        final AppCompatActivity context = this; //saves the current context for later use

        //assign onClick listener to the submit button
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AtomicInteger pickAddressID = new AtomicInteger();
                AtomicInteger destAddressID = new AtomicInteger();
                AtomicInteger routeID = new AtomicInteger();

                //pickAddressID.set(28);
                //destAddressID.set(29);
                //routeID.set(16);


                //makes the pickupAddress
                enqueue((pickID, request) -> {
                    pickAddressID.set(pickID);

                }, context, API.addAddr(
                        txt(pickAddressLine1),
                        txt(pickAddressLine2),
                        txt(pickCity),
                        txt(pickState),
                        txt(pickZip)
                ), Integer.class);

                //makes the destinationAddress
                enqueue((destID, request) -> {
                    destAddressID.set(destID);

                }, context, API.addAddr(
                        txt(destAddressLine1),
                        txt(destAddressLine2),
                        txt(destCity),
                        txt(destState),
                        txt(destZip)
                ), Integer.class);

                //waits half a second before starting code as this is asynchronous
                // but needs to run after the addresses calls
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //creates the route
                enqueue((RouteID, request) -> {
                    routeID.set(RouteID);
                }, context, API.addRoute(pickAddressID.intValue(), destAddressID.intValue(),
                        8), Integer.class);

                //waits half a second before starting code as this is asynchronous
                // but needs to run after the route call
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //creates the ride
                enqueue((rideID, request) -> {

                }, context, API.addRide(routeID.intValue(), 9 /*user goes here*/,
                        dateTime(datePicker, timePicker)), Integer.class);

                toRequestComplete();
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
    private Date dateTime(DatePicker datePicker, TimePicker timePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        return calendar.getTime();
    }

    //used to swap to the request complete activity
    public void toRequestComplete(){
        Intent intent = new Intent(this, RideRequestComplete.class);
        startActivity(intent);
    }




}