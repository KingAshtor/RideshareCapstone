//package io.capstone.userservice;
//
//import io.capstone.userservice.user.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static java.lang.String.format;
//import static org.hamcrest.CoreMatchers.*;
//import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
//import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
//import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class RequestsTests {
//    private final TestRestTemplate template = new TestRestTemplate();
//
//    @Test
//    public void testAddRole() {
//        final int expectedStatus = 200;
//        final User user = create();
//
//        final ResponseEntity<Void> post = template.exchange(format("http://localhost:8080/api/roles/add/byEmail?email=%s&role=%s", user.getEmail(), "admin"), HttpMethod.POST, null, Void.class);
//        final int actualPostStatus = post.getStatusCodeValue();
//        assertEquals(expectedStatus, actualPostStatus);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);
//        final int actualGetStatus = get.getStatusCodeValue();
//        assertEquals(expectedStatus, actualGetStatus);
//
//        final User body = get.getBody();
//        assertNotNull(body);
//        assertTrue(body.getRoles().contains("admin"), "Role not added");
//    }
//
//    @Test
//    public void testRemoveRole() {
//        final int expectedStatus = 200;
//        final User user = create();
//
//        final ResponseEntity<Void> post = template.exchange(format("http://localhost:8080/api/roles/add/byEmail?email=%s&role=%s", user.getEmail(), "admin"), HttpMethod.POST, null, Void.class);
//        final int actualPostStatus = post.getStatusCodeValue();
//        assertEquals(expectedStatus, actualPostStatus);
//
//        final ResponseEntity<Void> delete = template.exchange(format("http://localhost:8080/api/roles/del/byEmail?email=%s&role=%s", user.getEmail(), "admin"), HttpMethod.DELETE, null, Void.class);
//        final int actualDeleteStatus = delete.getStatusCodeValue();
//        assertEquals(expectedStatus, actualDeleteStatus);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);
//        final int actualGetStatus = get.getStatusCodeValue();
//        assertEquals(expectedStatus, actualGetStatus);
//
//        final User body = get.getBody();
//        assertNotNull(body);
//        assertFalse(body.getRoles().contains("admin"), "Role not removed");
//    }
//
//    @Test
//    public void testCreateUser() {
//        create();
//    }
//
//    @Test
//    public void testUpdateUser() {
//        final int expectedStatus = 200;
//        final User user = create();
//        user.setFName(UUID.randomUUID().toString()); user.setLName(UUID.randomUUID().toString());
//
//        final ResponseEntity<Void> put = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(
//                new User(user.getUsrID(), null, user.getFName(), user.getLName(), null, null, null)), Void.class);
//        final int actualPutStatus = put.getStatusCodeValue();
//        assertEquals(expectedStatus, actualPutStatus);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);
//        final int actualGetStatus = get.getStatusCodeValue();
//        assertEquals(expectedStatus, actualGetStatus);
//
//        final User body = get.getBody();
//        assertNotNull(body);
//        assertEquals(user.getFName(), body.getFName());
//        assertEquals(user.getLName(), body.getLName());
//    }
//
//    @Test
//    public void testDeleteUser() {
//        final User user = create();
//
//        final ResponseEntity<Void> delete = template.exchange(format("http://localhost:8080/api/user/del/byEmail?email=%s", user.getEmail()), HttpMethod.DELETE, null, Void.class);
//        final int expectedDeleteStatus = 200,
//                actualDeleteStatus = delete.getStatusCodeValue();
//        assertEquals(expectedDeleteStatus, actualDeleteStatus);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", user.getEmail()), User.class);
//        final int expectedGetStatus = 404,
//                actualGetStatus = get.getStatusCodeValue();
//        assertEquals(expectedGetStatus, actualGetStatus);
//    }
//
//    @Test
//    public void testViewUser() {
//        final String email = UUID.randomUUID() + "@gmail.com";
//        final User expectedUser = new User(null, email, null, null, null, null, null);
//        template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(expectedUser), String.class);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
//        int expectedStatus = 200,
//                actualStatus = get.getStatusCode().value();
//        assertEquals(expectedStatus, actualStatus);
//
//        final User actualUser = get.getBody();
//        assertNotNull(actualUser);
//        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
//    }
//
//    @Test
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    public void testListUser() {
//        final User user1 = create(), user2 = create();
//        final ResponseEntity<Collection> res = template.getForEntity("http://localhost:8080/api/user/list", Collection.class);
//        final int expectedStatus = 200,
//                actualStatus = res.getStatusCode().value();
//        assertEquals(expectedStatus, actualStatus);
//        assertNotNull(res.getBody());
//
//        final List<User> users = new ArrayList<>((Collection<LinkedHashMap<String, Object>>) res.getBody())
//                .stream().map(map -> new User((Integer) map.get("usrID"), (String) map.get("email"), null, null, null, null, null)).collect(Collectors.toList());
//
//        final boolean contains = users.containsAll(Arrays.asList(user1, user2));
//        assertTrue(contains, "Response does not contain the two inserted users.");
//    }
//
//    private User create() {
//        final int expectedStatus = 200;
//        final String email = UUID.randomUUID() + "@gmail.com";
//        final User user = new User(null, email, null, null, null, null, null);
//
//        final ResponseEntity<Void> put = template.exchange("http://localhost:8080/api/user/put", HttpMethod.PUT, new HttpEntity<>(user), Void.class);
//        final int putStatus = put.getStatusCodeValue();
//        assertEquals(expectedStatus, putStatus);
//
//        final ResponseEntity<User> get = template.getForEntity(format("http://localhost:8080/api/user/byEmail?email=%s", email), User.class);
//        final User body = get.getBody();
//        final int getStatus = get.getStatusCodeValue();
//        assertEquals(expectedStatus, getStatus);
//        assertNotNull(body);
//
//        return body;
//    }
//}
