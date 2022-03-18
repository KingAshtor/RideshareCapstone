package io.capstone.userservice.ride;

import io.capstone.userservice.user.User;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Route {
    private int routeID;
    private Address startAddress, endAddress;
    private User driver;
    private boolean repeated;
}
