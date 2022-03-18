package io.capstone.userservice;

import io.capstone.userservice.ride.Address;
import io.capstone.userservice.ride.Ride;
import io.capstone.userservice.ride.RideRegistry;
import io.capstone.userservice.ride.Route;
import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Database database = new Database();
    private final UserRegistry users = new UserRegistry(database);
    private final RideRegistry rides = new RideRegistry(database);

    public Controller() throws SQLException {}

    @RequestMapping(value="ride/addr/add", method=RequestMethod.POST)
    public ResponseEntity<Integer> createAddr(@RequestParam(value="line1") String line1, @RequestParam(value="line2") String line2, @RequestParam(value="city") String city, @RequestParam(value="state") String state, @RequestParam(value="zip") String zip) throws SQLException {
        return new ResponseEntity<>(rides.createAddress(line1, line2, city, state, zip), HttpStatus.OK);
    }

    @RequestMapping(value="ride/addr/del", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAddr(@RequestParam(value="id") int id) throws SQLException {
        rides.deleteAddress(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/addr", method=RequestMethod.GET)
    public ResponseEntity<Address> viewAddr(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getAddress(id), HttpStatus.OK);
    }

    @RequestMapping(value="ride/route/add", method=RequestMethod.POST)
    public ResponseEntity<Integer> createRoute(@RequestParam(value="from") int from, @RequestParam(value="to") int to, @RequestParam(value="driver") int driver) throws SQLException {
        return new ResponseEntity<>(rides.createRoute(from, to, driver), HttpStatus.OK);
    }

    @RequestMapping(value="ride/route/del", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRoute(@RequestParam(value="id") int id) throws SQLException {
        rides.deleteRoute(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/route", method=RequestMethod.GET)
    public ResponseEntity<Route> viewRoute(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getRoute(id, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/route/byId", method=RequestMethod.GET)
    public ResponseEntity<List<Route>> viewRoutesById(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getRoutesByUserId(id, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/route/byEmail", method=RequestMethod.GET)
    public ResponseEntity<List<Route>> viewRoutesByEmail(@RequestParam(value="email") String email) throws SQLException {
        return new ResponseEntity<>(rides.getRoutesByUserEmail(email, users::userByEmail), HttpStatus.OK);
    }

    @RequestMapping(value="ride/route/recurring", method=RequestMethod.POST)
    public ResponseEntity<Void> setRouteRecurring(@RequestParam(value="id") int id, @RequestParam(value="status") boolean status) throws SQLException {
        System.out.println("id = " + id);
        System.out.println("status = " + status);
        rides.setRouteRecurring(id, status);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/add", method=RequestMethod.POST)
    public ResponseEntity<Integer> createRide(@RequestParam(value="route") int route, @RequestParam(value="rider") int rider, @RequestParam(value="datetime") String dateTime) throws SQLException {
        System.out.println("dateTime = " + dateTime.replace("T", " ").concat(":00"));
        return new ResponseEntity<>(rides.createRide(route, rider, Timestamp.valueOf(dateTime.replace("T", " ").concat(":00"))), HttpStatus.OK);
    }

    @RequestMapping(value="ride/del", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRide(@RequestParam(value="id") int id) throws SQLException {
        rides.deleteRide(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride", method=RequestMethod.GET)
    public ResponseEntity<Ride> viewRide(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getRide(id, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/complete", method=RequestMethod.POST)
    public ResponseEntity<Void> completeRide(@RequestParam(value="id") int id) throws SQLException {
        rides.completeRide(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/accept", method=RequestMethod.POST)
    public ResponseEntity<Void> acceptRide(@RequestParam(value="id") int id) throws SQLException {
        rides.acceptRide(id);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/byRiderId", method=RequestMethod.GET)
    public ResponseEntity<List<Ride>> viewRidesByRiderId(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getRidesByRiderId(id, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/byRiderEmail", method=RequestMethod.GET)
    public ResponseEntity<List<Ride>> viewRidesByRiderEmail(@RequestParam(value="email") String email) throws SQLException {
        return new ResponseEntity<>(rides.getRidesByRiderEmail(email, users::userByEmail, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/byDriverId", method=RequestMethod.GET)
    public ResponseEntity<List<Ride>> viewRidesByDriverId(@RequestParam(value="id") int id) throws SQLException {
        return new ResponseEntity<>(rides.getRidesByDriverId(id, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="ride/byDriverEmail", method=RequestMethod.GET)
    public ResponseEntity<List<Ride>> viewRidesByDriverEmail(@RequestParam(value="email") String email) throws SQLException {
        return new ResponseEntity<>(rides.getRidesByDriverEmail(email, users::userByEmail, users::userById), HttpStatus.OK);
    }

    @RequestMapping(value="user/put", method=RequestMethod.PUT)
    public ResponseEntity<Void> update(@RequestBody Map<String, Object> fields) throws SQLException {
        if (!fields.containsKey("usrID") && !fields.containsKey("email")) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        final User user = new User(
                (Integer) fields.get("usrID"),
                (String) fields.get("email"),
                (String) fields.get("fName"),
                (String) fields.get("lName"),
                (String) fields.get("hashedPwd"),
                (String) fields.get("salt"),
                null,
                (int) fields.get("homeAddress"),
                rides::getAddress);

        users.updateUser(user, rides::getAddress);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="roles/add/byId", method=RequestMethod.POST)
    public ResponseEntity<Void> addRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = users.hasUserById(id);
        if (has) users.addRole(id, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/add/byEmail", method=RequestMethod.POST)
    public ResponseEntity<Void> addRole(@RequestParam(value="email") String email, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = users.hasUserByEmail(email);
        if (has) users.addRole(email, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = users.hasUserById(id);
        if (has) users.deleteRole(id, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/del/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@RequestParam(value="email") String email, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = users.hasUserByEmail(email);
        if (has) users.deleteRole(email, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="id") int id) throws SQLException {
        final boolean has = users.hasUserById(id);
        if (has) users.deleteUserById(id, rides::getAddress);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/del/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="email") String email) throws SQLException {
        final boolean has = users.hasUserByEmail(email);
        if (has) users.deleteUserByEmail(email, rides::getAddress);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/byEmail", method=RequestMethod.GET)
    public ResponseEntity<User> view(@RequestParam(value="email") String email) throws SQLException {
        final User user = users.userByEmail(email, rides::getAddress);
        return new ResponseEntity<>(user, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(value="user/byId", method=RequestMethod.GET)
    public ResponseEntity<User> view(@RequestParam(value="id") int id) throws SQLException {
        final User user = users.userById(id, rides::getAddress);
        return new ResponseEntity<>(user, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(value="user/list", method=RequestMethod.GET)
    public ResponseEntity<List<User>> list() throws SQLException {
        return new ResponseEntity<>(users.getUsers(rides::getAddress), HttpStatus.OK);
    }
}
