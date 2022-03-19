package com.example.ridesharecapstone.api;

public class Route {
    private int routeID;
    private Address startAddress, endAddress;
    private User driver;
    private boolean repeated;

    public Route(int routeID, Address startAddress, Address endAddress, User driver, boolean repeated) {
        this.routeID = routeID;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.driver = driver;
        this.repeated = repeated;
    }

    public Route() {

    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public void setStartAddress(Address startAddress) {
        this.startAddress = startAddress;
    }

    public void setEndAddress(Address endAddress) {
        this.endAddress = endAddress;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }
}
