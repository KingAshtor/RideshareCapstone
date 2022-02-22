package io.capstone.userservice;

import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        System.out.println("nerd");
        if (!registry.getUsers().stream().map(User::getUsrID).collect(Collectors.toSet()).contains(id)) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        registry.addRole(id, role);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @RequestMapping(value="roles/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> deleteRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
        if (!registry.getUsers().stream().map(User::getUsrID).collect(Collectors.toSet()).contains(id)) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        registry.deleteRole(id, role);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

//    @RequestMapping(value="roles/has/byId", method=RequestMethod.DELETE)
//    public ResponseEntity<Void> hasRole(@RequestParam(value="id") int id, @RequestParam(value="role") String role) throws SQLException {
//        if (!registry.getUsers().stream().map(User::getUsrID).collect(Collectors.toSet()).contains(id)) return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        return new ResponseEntity<>(null, registry.roles(id).contains(role) ? HttpStatus.FOUND : HttpStatus.NOT_FOUND);
//    }

    @RequestMapping(value="user/del/byId", method=RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@RequestParam(value="id") int id) throws SQLException {
        final User user = registry.userById(id);
        registry.deleteUser(user);
        return new ResponseEntity<>(null, user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
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
