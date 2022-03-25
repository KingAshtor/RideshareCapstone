package io.capstone.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.capstone.userservice.ride.Address;
import io.capstone.userservice.ride.Ride;
import io.capstone.userservice.ride.RideRegistry;
import io.capstone.userservice.ride.Route;
import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Database database = new Database();
    private final UserRegistry users = new UserRegistry(database);
    private final RideRegistry rides = new RideRegistry(database);

    private static long previousTimeMillis = System.currentTimeMillis();
    private static long counter = 0L;

    public static synchronized long token() {
        long currentTimeMillis = System.currentTimeMillis();
        counter = (currentTimeMillis == previousTimeMillis) ? (counter + 1L) & 1048575L : 0L;
        previousTimeMillis = currentTimeMillis;
        long timeComponent = (currentTimeMillis & 8796093022207L) << 20;
        return timeComponent | counter;
    }

    public Controller() throws SQLException {}

    @AllArgsConstructor
    @Getter
    private static class Content {
        private String type;
        private String value;
    }
    @AllArgsConstructor
    @Getter
    private static class Email {
        private String[] to;
        private String from, subject;
        private Content[] content;
    }
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private long emailToken(String email) {
        final long token = token();
        try {
            HttpClients.createDefault().execute(new HttpPost(new URIBuilder("https://api.mailazy.com/v1/mail/send").build().toString()) {{
                addHeader("X-Api-Key", "c8q74p2k4fl2q0fupg60vEWaWfttSM");
                addHeader("X-Api-Secret", "POFBxVJBQmxMhSHMgSzWtMEWANv.K6wgDwvXQb4QEDSeq2a5qW");
                addHeader("Content-Type", "application/json");
                setEntity(new ByteArrayEntity(MAPPER.writeValueAsString(new Email(
                        new String[] {email},
                        "PTC Rideshare <rideshare@masondkl.tk>",
                        "View your access token.",
                        new Content[] {new Content("text/plain", format("Your unique confirmation token: %d", token))}
                )).getBytes(StandardCharsets.UTF_8)));
            }});
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    @RequestMapping(value="confirmation", method=RequestMethod.GET)
    public ResponseEntity<Long> confirmation(@RequestParam(value="email") String email) {
        return new ResponseEntity<>(emailToken(email), HttpStatus.OK);
    }

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
        rides.setRouteRecurring(id, status);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="ride/add", method=RequestMethod.POST)
    public ResponseEntity<Integer> createRide(@RequestParam(value="route") int route, @RequestParam(value="rider") int rider, @RequestParam(value="datetime") String dateTime) throws SQLException {
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

    @RequestMapping(value="ride/start", method=RequestMethod.POST)
    public ResponseEntity<Void> startRide(@RequestParam(value="id") int id) throws SQLException {
        rides.startRide(id);
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

    @RequestMapping(value="user/photo/add/byEmail", method=RequestMethod.POST)
    public ResponseEntity<Void> addPhoto(@RequestParam(value="email") String email, @RequestParam(value="url") String url) throws SQLException {
        users.addPhoto(email, url);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="user/photo/delete/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deletePhoto(@RequestParam(value="email") String email, @RequestParam(value="url") String url) throws SQLException {
        users.deletePhoto(email, url);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="user/photo/byEmail", method=RequestMethod.GET)
    public ResponseEntity<String> photo(@RequestParam(value="email") String email) throws SQLException {
        final String url = users.getPhoto(email);
        System.out.println(url);
        return new ResponseEntity<>(url, url == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(value="user/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="id") int id) throws SQLException {
        final boolean has = users.hasUserById(id);
        if (has) users.deleteUserById(id, rides::getAddress, rides);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/del/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="email") String email) throws SQLException {
        final boolean has = users.hasUserByEmail(email);
        if (has) users.deleteUserByEmail(email, rides::getAddress, rides);
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
