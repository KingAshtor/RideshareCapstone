package io.capstone.userservice;

import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {
    private final UserRegistry registry = new UserRegistry();

    @RequestMapping(value="user/put", method=RequestMethod.PUT)
    public ResponseEntity<String> update(@RequestBody Map<String, Object> fields) {
        if (!fields.containsKey("usrID") && !fields.containsKey("email")) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        final User user = new User(
                (Integer) fields.get("usrID"),
                (String) fields.get("email"),
                (String) fields.get("fName"),
                (String) fields.get("lName"),
                (String) fields.get("hashedPwd"),
                (String) fields.get("salt"));
        try {
            registry.updateUser(user);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (SQLException err) {
            return new ResponseEntity<>(err.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @RequestMapping(value="user/del", method=RequestMethod.DELETE)
    public ResponseEntity<String> delete(@RequestParam(value="id") int id) {
        try {
            final User user = registry.userById(id);
            registry.deleteUser(user);
            return new ResponseEntity<>(null, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
        } catch (SQLException err) {
            return new ResponseEntity<>(err.getMessage(), HttpStatus.OK);
        }
    }

    @RequestMapping(value="user/byEmail", method=RequestMethod.GET)
    public ResponseEntity<User> view(@RequestParam(value="email") String email) throws SQLException {
        final User user = registry.userByEmail(email);
        return new ResponseEntity<>(user, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(value="user/byId", method=RequestMethod.GET)
    public ResponseEntity<User> view(@RequestParam(value="id") int id) throws SQLException {
        final User user = registry.userById(id);
        return new ResponseEntity<>(user, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @RequestMapping(value="user/list", method=RequestMethod.GET)
    public ResponseEntity<List<User>> list() throws SQLException {
        return new ResponseEntity<>(registry.getUsers(), HttpStatus.OK);
    }
}
