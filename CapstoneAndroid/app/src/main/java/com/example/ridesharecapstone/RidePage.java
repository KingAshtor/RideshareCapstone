package com.example.ridesharecapstone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static com.example.ridesharecapstone.api.retrofit.Api.API;
import static com.example.ridesharecapstone.api.retrofit.Api.enqueue;

import com.example.ridesharecapstone.api.Route;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Response;

//implements added just for the time/date stuff, not needed for normal views
public class RidePage extends AppCompatActivity {

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

        final AppCompatActivity context = this; //saves the current context for later use

        //assign onClick listener to the submit button
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                enqueue(sendRequest(context, Route.class));
                sendTheStuff();
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

//    private void sendRequest(Type body, Response<JsonObject> response){
//
//
//        Toast.makeText(getBaseContext(), "Request Sent", Toast.LENGTH_SHORT).show();
//
//    }

    private void sendTheStuff(){
        API.addRoute(28,29, 8);
        Toast.makeText(getBaseContext(), "Stuff sent?", Toast.LENGTH_SHORT).show();
    }

    //used to swap to the request complete activity
    public void toRequestComplete(){
        Intent intent = new Intent(this, RideRequestComplete.class);
        startActivity(intent);
    }




}