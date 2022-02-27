package io.capstone.userservice.user;

import io.capstone.userservice.Database;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.capstone.userservice.Database.CONNECTION_STRING;
import static java.lang.String.format;

public class UserRegistry {
    private final Database database = new Database();

    public User userByEmail(String email) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE email = '%s'", email));
            res.next();
            return user(res);
        });
    }

    public User userById(int usrID) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE usrID = %d", usrID));
            res.next();
            return user(res);
        });
    }

    public void updateUser(User user) throws SQLException {
        if (user.getUsrID() == null && user.getEmail() == null) return;

        database.consumer(database.connection(), stmt -> {
            final String
                    email = nullToEmpty(user.getEmail()),
                    fName = nullToEmpty(user.getFName()),
                    lName = nullToEmpty(user.getLName()),
                    hashedPwd = nullToEmpty(user.getHashedPwd()),
                    salt = nullToEmpty(user.getSalt());
            if (!hasUserById(user.getUsrID())
                    && !hasUserByEmail(user.getEmail())) {
                stmt.execute(format("INSERT INTO Rideshare.Usr VALUES ('%s', '%s', '%s', '%s', '%s', 0, 0)", email, fName, lName, hashedPwd, salt));
            } else {
                if (user.getUsrID() == null)
                    user.setUsrID(userByEmail(user.getEmail()).getUsrID());
                final List<Pair> entries = Arrays.asList(Pair.of("email", email), Pair.of("fName", fName), Pair.of("lName", lName), Pair.of("hashedPwd", hashedPwd), Pair.of("salt", salt));
                final String setEntries = entries.stream()
                        .filter(pair -> !"".equals(pair.value))
                        .map(pair -> format("%s = '%s'", pair.col, pair.value))
                        .collect(Collectors.joining(", "));

                stmt.execute(format("UPDATE Rideshare.Usr SET %s", setEntries));
            }
        });
    }

    public void deleteUserById(int id) throws SQLException {
        final String where = format("usrID = %d", id);
        database
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Role WHERE %s", where)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Usr WHERE %s", where)));
    }

    public void deleteUserByEmail(String email) throws SQLException {
        final String where = format("email = '%s'", email);
        database
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Role WHERE %s", where)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Usr WHERE %s", where)));
    }

    public List<User> getUsers() throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Usr");
            final List<User> users = new ArrayList<>();

            while (res.next()) {
                final User user = user(res);
                users.add(user);
            }

            return users;
        });
    }

    public void addRole(Integer id, String role) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("INSERT INTO Rideshare.Role VALUES (%d, '%s')", id, role)));
    }

    public void addRole(String email, String role) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("INSERT INTO Rideshare.Role VALUES (%d, '%s')", id(email), role)));
    }

    public void deleteRole(Integer id, String role) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("DELETE FROM Rideshare.Role WHERE UsrID = %d and value = '%s'", id, role)));
    }

    public void deleteRole(String email, String role) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("DELETE FROM Rideshare.Role WHERE UsrID = %d and value = '%s'", id(email), role)));
    }

    public boolean hasUserById(Integer id) throws SQLException {
        if (id == null) return false;
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Usr");
            while(res.next()) {
                if (id == res.getInt("usrID")) return true;
            }
            return false;
        });
    }

    public boolean hasUserByEmail(String email) throws SQLException {
        if (email == null) return false;

        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Usr");
            while(res.next()) {
                if (email.equals(res.getString("email"))) return true;
            }
            return false;
        });
    }

    public Set<String> roles(Integer id) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Role");
            final Set<String> roles = new HashSet<>();

            while (res.next()) {
                final int foundId = res.getInt("UsrID");
                if (foundId == id)
                    roles.add(res.getString("value"));
            }

            return roles;
        });
    }

    public int id(String email) throws SQLException {
        if (email == null) return -1;
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Usr");
            while(res.next()) {
                if (email.equals(res.getString("email"))) return res.getInt("usrID");
            }
            return -1;
        });
    }

    private User user(ResultSet res) throws SQLException {
        final Integer usrID = value(res, () -> res.getInt("usrID"));
        final String
                email = value(res, () -> res.getString("email")),
                fName = value(res, () -> res.getString("fName")),
                lName = value(res, () -> res.getString("lName")),
                hashedPwd = value(res, () -> res.getString("hashedPwd")),
                salt = value(res, () -> res.getString("salt"));
        return new User(usrID, email, fName, lName, hashedPwd, salt, null) {{
            setRoles(roles(this.getUsrID()));
        }};
    }

    private <T> T value(ResultSet res, Database.DataReturning<T> function) throws SQLException {
        return res.wasNull() ? null : function.process();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Getter
    private static class Pair {
        private final String col, value;
        private Pair(String col, String value) { this.col = col; this.value = value; }
        public static Pair of(String col, String value) { return new Pair(col, value); }
    }
}
