package io.capstone.userservice.user;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UserRegistry {
    //TODO("add database functionality")
    private final Map<String, String> salts = new HashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();

    public boolean newUser(User user) {
        return users.putIfAbsent(user.getName(), user) == null;
    }

    public boolean delUser(String name) {
        return users.remove(name) != null;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public String genSalt(String name) {
        final String salt = UUID.randomUUID().toString();
        return salts.putIfAbsent(name, salt) == null ? salt : null;
    }

    public boolean delSalt(String name) {
        return salts.remove(name) != null;
    }

    public Map<String, String> getSalts() {
        return salts;
    }
}
