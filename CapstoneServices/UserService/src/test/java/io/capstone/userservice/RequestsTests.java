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
    public void testCreateUser() {
        final String name = UUID.randomUUID().toString();
        final ResponseEntity<User> res = template.postForEntity("http://localhost:8080/api/users/new", new User(name, "pwd"), User.class);
        int expectedStatus = 200,
            actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testCreateDuplicateUser() {
        final String name = UUID.randomUUID().toString();
        final User user = new User(name, "pwd");
        template.postForEntity("http://localhost:8080/api/users/new", user, User.class);
        final ResponseEntity<User> res = template.postForEntity("http://localhost:8080/api/users/new", user, User.class);
        int expectedStatus = 409,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testDeleteUser() {
        final String name = UUID.randomUUID().toString();
        template.postForEntity("http://localhost:8080/api/users/new", new User(name, "pwd"), User.class);
        final ResponseEntity<Void> res = template.exchange(format("http://localhost:8080/api/users/del?name=%s", name), HttpMethod.DELETE, null, Void.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testViewUser() {
        final String name = UUID.randomUUID().toString();
        final User expectedUser = new User(name, "pwd");
        template.postForEntity("http://localhost:8080/api/users/new", expectedUser, User.class);
        final ResponseEntity<User> res = template.getForEntity(format("http://localhost:8080/api/users/view?name=%s", name), User.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();
        final User actualUser = res.getBody();

        assertEquals(expectedStatus, actualStatus);
        assertEquals(expectedUser, actualUser);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testListUser() {
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();
        final User user1 = new User(name1, "pwd");
        final User user2 = new User(name2, "pwd");
        template.postForEntity("http://localhost:8080/api/users/new", user1, User.class);
        template.postForEntity("http://localhost:8080/api/users/new", user2, User.class);
        final ResponseEntity<Collection> res = template.getForEntity("http://localhost:8080/api/users/list", Collection.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(res.getBody());

        final List<User> users = new ArrayList<>((Collection<LinkedHashMap<String, String>>) res.getBody())
                .stream().map(map -> new User(map.get("name"), map.get("hashedPwd"))).collect(Collectors.toList());
        final boolean contains = users.containsAll(Arrays.asList(user1, user2));

        assertEquals(expectedStatus, actualStatus);
        assertTrue(contains, "Response does not contain the two inserted users.");
    }

    @Test
    public void testGenerateSalt() {
        final String name = UUID.randomUUID().toString();
        final ResponseEntity<String> res = template.getForEntity(format("http://localhost:8080/api/salts/gen?name=%s", name), String.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(res.getBody());
    }

    @Test
    public void testGenerateDuplicateSalt() {
        final String name = UUID.randomUUID().toString();
        template.getForEntity(format("http://localhost:8080/api/salts/gen?name=%s", name), String.class);
        final ResponseEntity<String> res = template.getForEntity(format("http://localhost:8080/api/salts/gen?name=%s", name), String.class);
        int expectedStatus = 409,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
        assertNull(res.getBody());
    }

    @Test
    public void testDeleteSalt() {
        final String name = UUID.randomUUID().toString();
        template.getForEntity(format("http://localhost:8080/api/salts/gen?name=%s", name), String.class);
        final ResponseEntity<Void> res = template.exchange(format("http://localhost:8080/api/salts/del?name=%s", name), HttpMethod.DELETE, null, Void.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void testViewSalt() {
        final String name = UUID.randomUUID().toString();
        template.getForEntity(format("http://localhost:8080/api/salts/gen?name=%s", name), String.class);
        final ResponseEntity<String> res = template.getForEntity(format("http://localhost:8080/api/salts/view?name=%s", name), String.class);
        int expectedStatus = 200,
                actualStatus = res.getStatusCode().value();

        assertEquals(expectedStatus, actualStatus);
        assertNotNull(res.getBody());
    }
}
