package io.capstone.userservice.ride;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Address {
    private int addressID;
    private String line1, line2;
    private String city, state, zip;
}
