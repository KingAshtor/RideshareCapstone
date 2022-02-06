package io.capstone.userservice;

import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class Controller {
    private final UserRegistry registry = new UserRegistry();

    public Controller() throws SQLException {
    }

    @RequestMapping(value = "/users/new", method = RequestMethod.POST)
    public ResponseEntity<Void> createUser(@RequestBody User user) throws SQLException {
        final boolean result = registry.newUser(user);
        return new ResponseEntity<>(null, result ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/users/del", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@RequestParam(value = "name") String name) throws SQLException  {
        boolean result = registry.delUser(name);
        return new ResponseEntity<>(null, result ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/users/view", method = RequestMethod.GET)
    public ResponseEntity<User> viewUser(@RequestParam(value = "name") String name) {
        final User user = registry.getUsers().get(name);
        return new ResponseEntity<>(user, user != null ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/users/list", method = RequestMethod.GET)
    public ResponseEntity<Collection<User>> listUsers() {
        Collection<User> users = registry.getUsers().values();
        users = users.size() == 0 ? null : users;
        return new ResponseEntity<>(users, users == null ? HttpStatus.CONFLICT : HttpStatus.OK);
    }

    @RequestMapping(value = "/salts/gen", method = RequestMethod.GET)
    public ResponseEntity<String> generateSalt(@RequestParam(value = "name") String name) throws SQLException {
        final String result = registry.genSalt(name);
        return new ResponseEntity<>(result, result != null ? HttpStatus.OK : HttpStatus.CONFLICT);
    }

    @RequestMapping(value = "/salts/view", method = RequestMethod.GET)
    public ResponseEntity<String> viewSalt(@RequestParam(value = "name") String name) {
        final String salt = registry.getSalts().get(name);
        return new ResponseEntity<>(salt, salt == null ? HttpStatus.CONFLICT : HttpStatus.OK);
    }
}
