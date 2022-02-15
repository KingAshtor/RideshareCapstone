package io.capstone.userservice.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class UserRegistry {
    private static final Pattern DB_PATTERN = Pattern.compile("(?<=database: ).+"), NAME_PATTERN = Pattern.compile("(?<=name: ).+"), PASSWORD_PATTERN = Pattern.compile("(?<=password: ).+");
    public static final String CONNECTION_STRING;
    static {
        CONNECTION_STRING = format("jdbc:sqlserver://funnyserver.database.windows.net:1433;database=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                db(), name(), password());
    }

    public static String credentials() {
        String credentials = null; try { credentials = new String(Files.readAllBytes(Paths.get("credentials.txt"))); }
        catch (IOException e) { System.out.println("Could not read credentials from credentials.txt"); System.exit(-1); }
        return credentials;
    }

    public static String db() {
        final Matcher db = DB_PATTERN.matcher(credentials());
        return db.find() ? db.group() : "";
    }

    public static String name() {
        final Matcher name = NAME_PATTERN.matcher(credentials());
        return name.find() ? name.group() : "";
    }

    public static String password() {
        final Matcher password = PASSWORD_PATTERN.matcher(credentials());
        return password.find() ? password.group() : "";
    }

    public User userByEmail(String searchEmail) throws SQLException {
        return getUser(stmt -> {
            try { return stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE email = '%s'", searchEmail)); }
            catch (SQLException ignored) {} return null;
        });
    }

    public User userById(int usrID) throws SQLException {
        return getUser(stmt -> {
            try { return stmt.executeQuery(format("SELECT * FROM Rideshare.Usr WHERE usrID = %s", usrID)); }
            catch (SQLException ignored) {} return null;
        });
    }

    public void updateUser(User user) throws SQLException {
        if (user.getUsrID() == null && user.getEmail() == null) return;

        final Connection con = DriverManager.getConnection(CONNECTION_STRING);
        final Statement stmt = con.createStatement();

        String email = user.getEmail(); email = email == null ? "" : email;
        String fName = user.getFName(); fName = fName == null ? "" : fName;
        String lName = user.getLName(); lName = lName == null ? "" : lName;
        String hashedPwd = user.getHashedPwd(); hashedPwd = hashedPwd == null ? "" : hashedPwd;
        String salt = user.getSalt(); salt = salt == null ? "" : salt;

        if ((user.getUsrID() == null
                ? userByEmail(user.getEmail())
                : userById(user.getUsrID())) == null) {
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
    }

    public void deleteUser(User user) throws SQLException {
        if (user == null) return;

        final Connection con = DriverManager.getConnection(CONNECTION_STRING);
        final Statement stmt = con.createStatement();
        final String where = user.getUsrID() == null
                ? format("email = '%s'", user.getEmail())
                : format("usrID = %s", user.getUsrID());
        stmt.execute(format("DELETE FROM Rideshare.Usr WHERE %s", where));
    }

    public List<User> getUsers() throws SQLException {
        final Connection con = DriverManager.getConnection(CONNECTION_STRING);
        final Statement statement = con.createStatement();
        final ResultSet res = statement.executeQuery("SELECT * FROM Rideshare.Usr");
        final List<User> users = new ArrayList<>();

        while (res.next()) {
            users.add(userById(res.getInt("usrID")));
        }

        return users;
    }

    private User getUser(Function<Statement, ResultSet> getResults) throws SQLException {
        final Connection con = DriverManager.getConnection(CONNECTION_STRING);
        final Statement stmt = con.createStatement();
        final ResultSet res = getResults.apply(stmt);

        Integer usrID = null;
        String email = null;
        String fName = null, lName = null;
        String hashedPwd = null, salt = null;

        while(res.next()) {
            usrID = res.getInt("usrID");
            email = res.getString("email"); email = email.length() == 0 ? null : email;
            fName = res.getString("fName"); fName = fName.length() == 0 ? null : fName;
            lName = res.getString("lName"); lName = lName.length() == 0 ? null : lName;
            hashedPwd = res.getString("hashedPwd"); hashedPwd = hashedPwd.length() == 0 ? null : hashedPwd;
            salt = res.getString("salt"); salt = salt.length() == 0 ? null : salt;
        }

        if (usrID == null) return null;

        return new User(usrID, email, fName, lName, hashedPwd, salt);
    }

    @Getter
    private static class Pair {
        private final String col, value;
        private Pair(String col, String value) { this.col = col; this.value = value; }
        public static Pair of(String col, String value) { return new Pair(col, value); }
    }
}
