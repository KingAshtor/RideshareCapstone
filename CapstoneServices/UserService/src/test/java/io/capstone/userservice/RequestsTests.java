package io.capstone.userservice;

import io.capstone.userservice.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RequestsTests {
    private final TestRestTemplate template = new TestRestTemplate();

    @Test
    public void testAddRole() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final String salt = UUID.randomUUID().toString();
        User user = new User(null, email, null, null, null, salt, null);
        ResponseEntity<String> res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), String.class);
        final int expectedStatus = 200;
        int actualStatus = res.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);

        final ResponseEntity<User> userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user = userRes.getBody();
        assertNotNull(user);

        final ResponseEntity<Void> roleRes = template.exchange(format("http://localhost:8080/api/roles/add/byId?id=%d&role=%s", user.getUsrID(), "admin"), HttpMethod.POST, null, Void.class);
        actualStatus = roleRes.getStatusCodeValue();
        assertEquals(expectedStatus, actualStatus);

        ResponseEntity<User> updated = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(updated.getBody());
        assertTrue(updated.getBody().getRoles().contains("admin"), "Role not added");
    }

    @Test
    public void testRemoveRole() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final String salt = UUID.randomUUID().toString();
        User user = new User(null, email, null, null, null, salt, null);
        ResponseEntity<String> res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), String.class);
        final int expectedStatus = 200;
        int actualStatus = res.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);

        final ResponseEntity<User> userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user = userRes.getBody();
        assertNotNull(user);

        final ResponseEntity<Void> addRoleRes = template.exchange(format("http://localhost:8080/api/roles/add/byId?id=%d&role=%s", user.getUsrID(), "admin"), HttpMethod.POST, null, Void.class);
        actualStatus = addRoleRes.getStatusCodeValue();
        assertEquals(expectedStatus, actualStatus);

        final ResponseEntity<Void> removeRoleRes = template.exchange(format("http://localhost:8080/api/roles/del/byId?id=%d&role=%s", user.getUsrID(), "admin"), HttpMethod.DELETE, null, Void.class);
        actualStatus = removeRoleRes.getStatusCodeValue();
        assertEquals(expectedStatus, actualStatus);

        ResponseEntity<User> updated = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(updated.getBody());
        assertFalse(updated.getBody().getRoles().contains("admin"), "Role not removed");
    }

    @Test
    public void testCreateUser() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final String salt = UUID.randomUUID().toString();
        final User user = new User(null, email, null, null, null, salt, null);
        final ResponseEntity<String> res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), String.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus, res.getBody());
    }

    @Test
    public void testUpdateUser() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final String salt = UUID.randomUUID().toString();
        User user = new User(null, email, null, null, null, salt, null);
        ResponseEntity<String> res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), String.class);
        final int expectedStatus = 200;
        int actualStatus = res.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus, res.getBody());

        final ResponseEntity<User> userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user = userRes.getBody();
        assertNotNull(user);

        user.setFName(UUID.randomUUID().toString());
        user.setLName(UUID.randomUUID().toString());

        final String fName = user.getFName();
        final String lName = user.getLName();

        res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(new User(user.getUsrID(), null, user.getFName(), user.getLName(), null, null, new HashSet<>())), String.class);
        actualStatus = res.getStatusCodeValue();

        ResponseEntity<User> updated = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);

        assertEquals(expectedStatus, actualStatus, res.getBody());
        assertNotNull(updated.getBody());
        assertEquals(fName, updated.getBody().getFName());
        assertEquals(lName, updated.getBody().getLName());
    }

    @Test
    public void testDeleteUser() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final String salt = UUID.randomUUID().toString();
        User user = new User(null, email, null, null, null, salt, null);
        ResponseEntity<String> res = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), String.class);
        int expectedStatus = 200;
        int actualStatus = res.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);

        final ResponseEntity<User> userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user = userRes.getBody();
        assertNotNull(user);

        ResponseEntity<Void> delete = template.exchange(format("http://localhost:8080/api/user/del/byId?id=%d", user.getUsrID()), HttpMethod.DELETE, null, Void.class);
        actualStatus = delete.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);

        ResponseEntity<User> updated = template.getForEntity(format("http://localhost:8080/api/user/byId?id=%d", user.getUsrID()), User.class);
        expectedStatus = 404;
        actualStatus = updated.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testViewUser() {
        final String email = UUID.randomUUID() + "@gmail.com";
        final User expectedUser = new User(null, email, null, null, null, UUID.randomUUID().toString(), null);
        template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(expectedUser), String.class);

        final ResponseEntity<User> res = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();
        final User actualUser = res.getBody();

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(actualUser);
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getSalt(), actualUser.getSalt());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testListUser() {
        final String email1 = UUID.randomUUID() + "@gmail.com";
        final String email2 = UUID.randomUUID() + "@gmail.com";
        User user1 = new User(null, email1, null, null, null, UUID.randomUUID().toString(), null);
        User user2 = new User(null, email2, null, null, null, UUID.randomUUID().toString(), null);
        template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user1), String.class);
        template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user2), String.class);
        final ResponseEntity<Collection> res = template.getForEntity("http://localhost:8080/api/user/list", Collection.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        //Update both users to obtain ids

        assertEquals(expectedStatus, actualStatus);

        ResponseEntity<User> userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email1), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user1 = userRes.getBody();
        assertNotNull(user1);

        assertEquals(expectedStatus, actualStatus);

        userRes = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email2), User.class);
        actualStatus = userRes.getStatusCodeValue();

        assertEquals(expectedStatus, actualStatus);
        user2 = userRes.getBody();
        assertNotNull(user1);

        //end updating

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(res.getBody());

        final List<User> users = new ArrayList<>((Collection<LinkedHashMap<String, Object>>) res.getBody())
                .stream().map(map -> new User((Integer) map.get("usrID"), (String) map.get("email"), null, null, null, (String) map.get("salt"), new HashSet<>())).collect(Collectors.toList());

        final boolean contains = users.containsAll(Arrays.asList(user1, user2));

        assertTrue(contains, "Response does not contain the two inserted users.");
    }
}
