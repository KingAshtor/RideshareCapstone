package io.capstone.userservice.user;

import io.capstone.userservice.Database;
import io.capstone.userservice.ride.Address;
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

@RequiredArgsConstructor
public class UserRegistry {
    private final Database database;

    public User userByEmail(String email, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE email = '%s'", email));
            if (res.next()) return user(res, getAddress);
            else return null;
        });
    }

    public User userById(int usrID, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE usrID = %d", usrID));
            if (res.next()) return user(res, getAddress);
            else return null;
        });
    }

    public void updateUser(User user, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        if (user.getUsrID() == null && user.getEmail() == null) return;

        database.consumer(database.connection(), stmt -> {
            final String
                    email = nullToEmpty(user.getEmail()),
                    fName = nullToEmpty(user.getFName()),
                    lName = nullToEmpty(user.getLName()),
                    hashedPwd = nullToEmpty(user.getHashedPwd()),
                    salt = nullToEmpty(user.getSalt());
            final Address address = user.getAddress();
            final int addressID = address == null ? -1 : address.getAddressID();
            if (!hasUserById(user.getUsrID())
                    && !hasUserByEmail(user.getEmail())) {
                stmt.execute(format("INSERT INTO Rideshare.Usr VALUES ('%s', '%s', '%s', '%s', '%s', 0, 0, 0, %d)", email, fName, lName, hashedPwd, salt, addressID));
            } else {
                if (user.getUsrID() == null) {
                    final User byEmail = userByEmail(user.getEmail(), getAddress);
                    if (byEmail == null) return;
                    user.setUsrID(byEmail.getUsrID());
                }
                final List<Pair<?>> entries = Arrays.asList(Pair.of("email", email), Pair.of("fName", fName), Pair.of("lName", lName), Pair.of("hashedPwd", hashedPwd), Pair.of("salt", salt), Pair.of("homeAddress", addressID));
                final String setEntries = entries.stream()
                        .filter(pair -> !"".equals(pair.value))
                        .map(pair -> format("%s = %s", pair.col, pair.value instanceof String ? "'" + pair.value + "'" : pair.value))
                        .collect(Collectors.joining(", "));

                stmt.execute(format("UPDATE Rideshare.Usr SET %s", setEntries));
            }
        });
    }

    public void deleteUserById(int id, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        final String where = format("usrID = %d", id);
        database
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Role WHERE %s", where)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Usr WHERE %s", where)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Address WHERE addressID = %d", userById(id, getAddress).getAddress().getAddressID())));
    }

    public void deleteUserByEmail(String email, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        int id = id(email);
        database
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Role WHERE UsrID = %d", id)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Usr WHERE usrID = %d", id)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.Address WHERE addressID = %d", userById(id, getAddress).getAddress().getAddressID())));
    }

    public List<User> getUsers(Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet res = stmt.executeQuery("SELECT * FROM Rideshare.Usr");
            final List<User> users = new ArrayList<>();

            while (res.next()) {
                final User user = user(res, getAddress);
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

    private User user(ResultSet res, Database.DataFunction<Integer, Address> getAddress) throws SQLException {
        final Integer usrID = value(res, () -> res.getInt("usrID"));
        final String
                email = emptyToNull(value(res, () -> res.getString("email"))),
                fName = emptyToNull(value(res, () -> res.getString("fName"))),
                lName = emptyToNull(value(res, () -> res.getString("lName"))),
                hashedPwd = emptyToNull(value(res, () -> res.getString("hashedPwd"))),
                salt = emptyToNull(value(res, () -> res.getString("salt")));
        return new User(usrID, email, fName, lName, hashedPwd, salt, null, res.getInt("homeAddress"), getAddress) {{
            setRoles(roles(this.getUsrID()));
        }};
    }

    private <T> T value(ResultSet res, Database.DataReturning<T> function) throws SQLException {
        final T result = function.process();
        return res.wasNull() ? null : result;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return Objects.equals(value, "") ? null : value;
    }

    @Getter
    private static class Pair<V> {
        private final String col;
        private final V value;
        private Pair(String col, V value) { this.col = col; this.value = value; }
        public static <V> Pair<V> of(String col, V value) { return new Pair<>(col, value); }
    }
}
