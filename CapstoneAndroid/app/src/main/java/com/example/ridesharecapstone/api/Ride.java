package com.example.ridesharecapstone.api;

import java.sql.Timestamp;

public class Ride {
    private int rideID;
    private Route route;
    private User rider;
    private Timestamp dateTime;
    private boolean completed, accepted;

    public Ride(int rideID, Route route, User rider, Timestamp dateTime, boolean completed, boolean accepted) {
        this.rideID = rideID;
        this.route = route;
        this.rider = rider;
        this.dateTime = dateTime;
        this.completed = completed;
        this.accepted = accepted;
    }

    public Ride() {

    }

    public void setRideID(int rideID) {
        this.rideID = rideID;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setRider(User rider) {
        this.rider = rider;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
