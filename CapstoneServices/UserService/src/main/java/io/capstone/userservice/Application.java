package io.capstone.userservice;

import io.capstone.userservice.ride.RideRegistry;
import io.capstone.userservice.user.UserRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.sql.Timestamp;

@Configuration
@SpringBootApplication
public class Application {
    public static void main(String[] args) throws SQLException {
        SpringApplication.run(Application.class, args);

//        final Database database = new Database();
//        final UserRegistry users = new UserRegistry(database);
//        final RideRegistry rides = new RideRegistry(database);
//        users.getUsers().stream().findFirst().ifPresent(user -> {
//            try {
//                final int addr = rides.createAddress("gg1", "gg2", "city", "st", "12345");
//                final int route = rides.createRoute(addr, addr, user.getUsrID());
//                final int ride = rides.createRide(route, user.getUsrID(), new Timestamp(System.currentTimeMillis()));
//
//                System.out.println(rides.getRide(ride, id -> {
//                    try {
//                        return users.userById(id);
//                    } catch (SQLException e) {
//                        throw new RuntimeException(e);
//                    }
//                }));
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }
}
