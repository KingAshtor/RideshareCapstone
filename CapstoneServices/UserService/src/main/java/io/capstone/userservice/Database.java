package io.capstone.userservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class Database {
    private static final Pattern DB_PATTERN = Pattern.compile("(?<=database: ).+"), NAME_PATTERN = Pattern.compile("(?<=name: ).+"), PASSWORD_PATTERN = Pattern.compile("(?<=password: ).+");
    public static final String CONNECTION_STRING;
    static {
        CONNECTION_STRING = format("jdbc:sqlserver://funnyserver.database.windows.net:1433;database=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                db(), name(), password());
    }

    public Connection connection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    public Database consumer(Connection connection, DataConsumer<Statement> consumer) throws SQLException {
        consumer.process(connection.createStatement());
        connection.close();
        return this;
    }

    public <T> T function(Connection connection, DataFunction<Statement, T> function) throws SQLException {
        final T result = function.process(connection.createStatement());
        connection.close(); return result;
    }

    private static String credentials() {
        String credentials = null; try { credentials = new String(Files.readAllBytes(Paths.get("credentials.txt"))); }
        catch (IOException e) { System.out.println("Could not read credentials from credentials.txt"); System.exit(-1); }
        return credentials;
    }

    private static String db() {
        final Matcher db = DB_PATTERN.matcher(credentials());
        return db.find() ? db.group() : "";
    }

    private static String name() {
        final Matcher name = NAME_PATTERN.matcher(credentials());
        return name.find() ? name.group() : "";
    }

    private static String password() {
        final Matcher password = PASSWORD_PATTERN.matcher(credentials());
        return password.find() ? password.group() : "";
    }

    public interface DataConsumer<T> {
        void process(T data) throws SQLException;
    }
    public interface DataBiConsumer<T1, T2> {
        void process(T1 t1, T2 t2) throws SQLException;
    }

    public interface DataFunction<T, R> {
        R process(T data) throws SQLException;
    }

    public interface DataBiFunction<T1, T2, R> {
        R process(T1 data1, T2 data2) throws SQLException;
    }

    public interface DataReturning<R> {
        R process() throws SQLException;
    }
}
