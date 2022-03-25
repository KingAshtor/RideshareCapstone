package io.capstone.userservice.ride;

import io.capstone.userservice.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ride {
    private int rideID;
    private Route route;
    private User rider;
    private Timestamp dateTime;
    private boolean completed, accepted, started;
}
