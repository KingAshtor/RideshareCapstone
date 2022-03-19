package com.example.ridesharecapstone.api;

public class Address {
    private int addressID;
    private String line1, line2;
    private String city, state, zip;

    public Address() {

    }

    public Address(int addressID, String line1, String line2, String city, String state, String zip) {
        this.addressID = addressID;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.zip = zip;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
