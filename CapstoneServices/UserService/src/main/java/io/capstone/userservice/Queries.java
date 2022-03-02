package io.capstone.userservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.capstone.userservice.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class Queries {
    //Demo class
    public static void main(String[] args) throws JsonProcessingException {
        new Queries();
    }

    public Queries() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final Response viewResponse = get(format("http://127.0.0.1:8080/api/user/byEmail?email=%s", "email"));

        if (viewResponse.status != 200) {
            //404 for email not existing
            return;
        }

        User responseUser = mapper.readValue(viewResponse.text, User.class);
        final Response deleteResponse = delete(format("http://127.0.0.1:8080/api/user/del?id=%s", responseUser.getUsrID()));

        if (deleteResponse.status != 200) {
            //404 for not existing
            return;
        }

        final Response listResponse = get("http://127.0.0.1:8080/api/user/list");
        @SuppressWarnings("unchecked") final List<User> users = mapper.readValue(listResponse.text, List.class);
    }

    public void register(String email, String password) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final ObjectMapper mapper = new ObjectMapper();
            final String simpleHash = new String(digest.digest(password.getBytes(StandardCharsets.UTF_8))),
                    salt = UUID.randomUUID().toString(),
                    finalHash = new String(digest.digest((salt + simpleHash).getBytes(StandardCharsets.UTF_8)));
            final Map<String, String> user = new HashMap<>() {{
                put("email", email);
                put("salt", salt);
                put("hashedPwd", finalHash);
            }};

            final Response updateResponse = put("http://127.0.0.1:8080/api/user/put", mapper.writeValueAsString(user));

            if (updateResponse.status != 200) {

            }
        } catch (NoSuchAlgorithmException | JsonProcessingException ignored) {}
    }

    public Response put(String url, String json) {
        Response putResponse;

        try {
            final HttpClient client = HttpClients.createDefault();
            final ContentType content = ContentType.APPLICATION_JSON.withCharset("utf-8");
            final HttpPut method = new HttpPut(new URIBuilder(url).build().toString()) {{
                setEntity(new ByteArrayEntity(json.getBytes(content.getCharset()), content));
            }};
            final HttpResponse response = client.execute(method);

            putResponse = new Response(
                    EntityUtils.toString(response.getEntity()),
                    response.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        return putResponse;
    }

    public Response delete(String url) {
        Response deleteResponse;

        try {
            final HttpClient client = HttpClients.createDefault();
            final HttpDelete method = new HttpDelete(new URIBuilder(url).build().toString());
            final HttpResponse response = client.execute(method);

            deleteResponse = new Response(
                    EntityUtils.toString(response.getEntity()),
                    response.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        return deleteResponse;
    }

    public Response get(String url) {
        Response getResponse;

        try {
            final HttpClient client = HttpClients.createDefault();
            final HttpGet method = new HttpGet(new URIBuilder(url).build().toString());
            final HttpResponse response = client.execute(method);

            getResponse = new Response(
                    EntityUtils.toString(response.getEntity()),
                    response.getStatusLine().getStatusCode());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        return getResponse;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Response {
        private final String text;
        private final int status;

        @Override
        public String toString() {
            return "Response{" +
                    "text='" + text + '\'' +
                    ", status=" + status +
                    '}';
        }
    }
}
