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
    public ResponseEntity<Void> update(@RequestBody Map<String, Object> fields) throws SQLException {
        if (!fields.containsKey("usrID") && !fields.containsKey("email")) return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        final User user = new User(
                (Integer) fields.get("usrID"),
                (String) fields.get("email"),
                (String) fields.get("fName"),
                (String) fields.get("lName"),
                (String) fields.get("hashedPwd"),
                (String) fields.get("salt"),
                null);

        registry.updateUser(user);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="roles/add/byId", method=RequestMethod.POST)
    public ResponseEntity<Void> addRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = registry.hasUserById(id);
        if (has) registry.addRole(id, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/add/byEmail", method=RequestMethod.POST)
    public ResponseEntity<Void> addRole(@RequestParam(value="email") String email, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = registry.hasUserByEmail(email);
        if (has) registry.addRole(email, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = registry.hasUserById(id);
        if (has) registry.deleteRole(id, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="roles/del/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@RequestParam(value="email") String email, @RequestParam(value="role") String role) throws SQLException {
        final boolean has = registry.hasUserByEmail(email);
        if (has) registry.deleteRole(email, role);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="id") int id) throws SQLException {
        final boolean has = registry.hasUserById(id);
        if (has) registry.deleteUserById(id);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value="user/del/byEmail", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="email") String email) throws SQLException {
        final boolean has = registry.hasUserByEmail(email);
        if (has) registry.deleteUserByEmail(email);
        return new ResponseEntity<>(null, has ? HttpStatus.OK : HttpStatus.NOT_FOUND);
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
