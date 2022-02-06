package io.capstone.userservice.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class UserRegistry {
    private static final Pattern DB_PATTERN = Pattern.compile("(?<=database: ).+"), NAME_PATTERN = Pattern.compile("(?<=name: ).+"), PASSWORD_PATTERN = Pattern.compile("(?<=password: ).+");
    private static final String CONNECTION_STRING;
    static {
        String credentials = null; try { credentials = new String(Files.readAllBytes(Paths.get("credentials.txt"))); }
        catch (IOException e) { System.out.println("Could not read credentials from credentials.txt"); System.exit(-1); }
        final Matcher db = DB_PATTERN.matcher(credentials);
        final Matcher name = NAME_PATTERN.matcher(credentials);
        final Matcher password = PASSWORD_PATTERN.matcher(credentials);

        CONNECTION_STRING = format("jdbc:sqlserver://masonsql.database.windows.net:1433;database=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                db.find() ? db.group() : "",
                name.find() ? name.group() : "",
                password.find() ? password.group() : ""
        );
    }

    private final Map<String, String> salts = new HashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();

    public UserRegistry() throws SQLException {
        final Connection con = DriverManager.getConnection(CONNECTION_STRING);
        final Statement stmt = con.createStatement();
        final ResultSet res = stmt.executeQuery("SELECT * FROM RideshareRecords");

        String name;
        while(res.next()) {
            name = res.getString("name");
            users.put(name, new User(name, res.getString("pwd")));
            salts.put(name, res.getString("salt"));
        }

        con.close();
    }

    public boolean newUser(User user) throws SQLException {
        final boolean result = users.putIfAbsent(user.getName(), user) == null;

        if (result) {
            final Connection con = DriverManager.getConnection(CONNECTION_STRING);
            final Statement stmt = con.createStatement();
            stmt.execute(String.format("UPDATE RideshareRecords " +
                    "SET pwd = '%s' " +
                    "WHERE name = '%s'", user.getPwd(), user.getName()));
        }

        return result;
    }

    public boolean delUser(String name) throws SQLException {
        final User originalUser = users.get(name);
        boolean result = users.remove(name) != null;

        if (result) {
            final Connection con = DriverManager.getConnection(CONNECTION_STRING);
            final Statement stmt = con.createStatement();
            result = delSalt(name);

            if (!result) {
                users.put(name, originalUser);
                return false;
            }

            stmt.execute(String.format("DELETE FROM RideshareRecords " +
                    "WHERE name = '%s'", name));
        }

        return result;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public String genSalt(String name) throws SQLException {
        final String salt = UUID.randomUUID().toString();
        final boolean result = salts.putIfAbsent(name, salt) == null;

        if (result) {
            final Connection con = DriverManager.getConnection(CONNECTION_STRING);
            final Statement stmt = con.createStatement();
            stmt.execute(String.format("INSERT INTO RideshareRecords " +
                    "VALUES('%s', '', '%s', '', '')", name, salt));
        }

        return result ? salt : null;
    }

    public boolean delSalt(String name) {
        return salts.remove(name) != null;
    }

    public Map<String, String> getSalts() {
        return salts;
    }
}
