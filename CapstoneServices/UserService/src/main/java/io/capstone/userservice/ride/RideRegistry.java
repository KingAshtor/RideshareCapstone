package io.capstone.userservice.ride;

import io.capstone.userservice.Database;
import io.capstone.userservice.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;

public class RideRegistry {
    public static final int
            FALSE_BIT = 0, TRUE_BIT = 1;
    private final Database database;
    private int addrCount, routeCount, rideCount;

    private int identity(String table) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet set = stmt.executeQuery(format("SELECT IDENT_CURRENT('%s') AS max", table));
            set.next(); return set.getInt("max");
        });
    }

    public RideRegistry(Database database) throws SQLException {
        this.database = database;
        addrCount = identity("Rideshare.Address");
        routeCount = identity("Rideshare.Route");
        rideCount = identity("Rideshare.RideReceipt");
    }

    public int createAddress(String line1, String line2, String city, String state, String zip) throws SQLException {
        final Map<String, String> data = new LinkedHashMap<>() {{
            put("line1", length(line1, 32)); put("line2", length(line2, 32));
            put("city", length(city, 32)); put("state", length(state, 2)); put("zip", length(zip, 5));
        }};
        database.consumer(database.connection(), stmt -> stmt.execute(format("INSERT INTO Rideshare.Address VALUES('%s', '%s', '%s', '%s', '%s')", data.values().toArray())));
        return ++addrCount;
    }

    public void deleteAddress(int id) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("DELETE FROM Rideshare.Address WHERE addressID = %d", id)));
    }

    public Address getAddress(int id) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.Address WHERE addressID = %d", id));
            if (!set.next()) return null;
            return new Address(id, set.getString("line1"), set.getString("line2"),
                    set.getString("city"), set.getString("state"), set.getString("zip"));
        });
    }

    public int createRoute(int from, int to, int driver) throws SQLException {
        System.out.println("==============");
        System.out.println("create route");
        System.out.println("driver: " + driver);
        final Map<String, Object> data = new LinkedHashMap<>() {{
            put("startAddressID", from); put("endAddressID", to);
            put("driverID", driver);
            put("gasPrice", 0.0);
            put("cost", 0.0);
            put("repeated", FALSE_BIT);
        }};
        database.consumer(database.connection(), stmt -> stmt.execute(format("INSERT INTO Rideshare.Route VALUES(%d, %d, %d, %f, %f, %d)", data.values().toArray())));
        return ++routeCount;
    }

    public void deleteRoute(int id) throws SQLException {
        database.consumer(database.connection(), stmt -> {
            final List<Integer> addresses = new ArrayList<>();
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.Route WHERE routeID = %d", id));
            while(set.next()) {
                addresses.add(set.getInt("startAddressID"));
                addresses.add(set.getInt("endAddressID"));
            }
            stmt.execute(format("DELETE FROM Rideshare.Route WHERE routeID = %d", id));
            for (Integer address : addresses) stmt.execute(format("DELETE FROM Rideshare.Address WHERE addressID = %d", address));
        });
    }

    public Route getRoute(int id, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.Route WHERE routeID = %d", id));
            if (!set.next()) return null;
            return new Route(id, getAddress(set.getInt("startAddressID")), getAddress(set.getInt("endAddressID")), byId.process(set.getInt("driverID"), this::getAddress), set.getInt("repeated") == TRUE_BIT);
        });
    }

    public List<Route> getRoutesByUserId(int id, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Route> routes = new ArrayList<>();
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.Route WHERE driverID = %d", id));
            while(set.next()) {
                routes.add(new Route(set.getInt("routeID"), getAddress(set.getInt("startAddressID")), getAddress(set.getInt("endAddressID")), byId.process(id, this::getAddress), set.getInt("repeated") == TRUE_BIT));

            }
            return routes;
        });
    }

    public List<Route> getRoutesByUserEmail(String email, Database.DataBiFunction<String, Database.DataFunction<Integer, Address>, User> byEmail) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Route> routes = new ArrayList<>();
            final User user = byEmail.process(email, this::getAddress);
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.Route WHERE driverID = %d", user.getUsrID()));
            while(set.next())
                routes.add(new Route(set.getInt("routeID"), getAddress(set.getInt("startAddressID")), getAddress(set.getInt("endAddressID")), user, set.getInt("repeated") == TRUE_BIT));
            return routes;
        });
    }

    public void setRouteRecurring(int id, boolean status) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("UPDATE Rideshare.Route SET repeated = %d WHERE routeID = %d", status ? TRUE_BIT : FALSE_BIT, id)));
    }

    public int createRide(int route, int rider, Timestamp date) throws SQLException {
        final Map<String, Object> data = new LinkedHashMap<>() {{
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            put("routeID", route); put("riderID", rider);
            put("rideDateTime", date.toLocalDateTime().format(formatter));
            put("completed", FALSE_BIT);
            put("accepted", FALSE_BIT);
            put("started", FALSE_BIT);
        }};
        database.consumer(database.connection(), stmt -> stmt.execute(format("INSERT INTO Rideshare.RideReceipt VALUES(%d, %d, '%s', 0, %d, %d, %d)", data.values().toArray())));
        return ++rideCount;
    }

    public void deleteRide(int id) throws SQLException {
        database
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.RideTransaction WHERE rideID = %d", id)))
                .consumer(database.connection(), stmt -> stmt.execute(format("DELETE FROM Rideshare.RideReceipt WHERE rideID = %d", id)));
    }

    public void completeRide(int id) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("UPDATE Rideshare.RideReceipt SET completed = %d WHERE rideID = %d", TRUE_BIT, id)));
    }

    public void acceptRide(int id) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("UPDATE Rideshare.RideReceipt SET accepted = %d WHERE rideID = %d", TRUE_BIT, id)));
    }

    public void startRide(int id) throws SQLException {
        database.consumer(database.connection(), stmt ->
                stmt.execute(format("UPDATE Rideshare.RideReceipt SET started = %d WHERE rideID = %d", TRUE_BIT, id)));
    }

    public Ride getRide(int id, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.RideReceipt WHERE rideID = %d", id));
            if (!set.next()) return null;
            return new Ride(id, getRoute(set.getInt("routeID"), byId), byId.process(set.getInt("riderID"), this::getAddress), Timestamp.valueOf(set.getString("rideDateTime")), set.getInt("completed") == TRUE_BIT, set.getInt("accepted") == TRUE_BIT, set.getInt("started") == TRUE_BIT);
        });
    }

    public List<Ride> getRidesByRiderId(int id, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Ride> rides = new ArrayList<>();
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.RideReceipt WHERE riderID = %d", id));
            while(set.next())
                rides.add(new Ride(set.getInt("rideID"), getRoute(set.getInt("routeID"), byId), byId.process(set.getInt("riderID"), this::getAddress), Timestamp.valueOf(set.getString("rideDateTime")), set.getInt("completed") == TRUE_BIT, set.getInt("accepted") == TRUE_BIT, set.getInt("started") == TRUE_BIT));
            return rides;
        });
    }

    public List<Ride> getRidesByRiderEmail(String email, Database.DataBiFunction<String, Database.DataFunction<Integer, Address>, User> byEmail, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Ride> rides = new ArrayList<>();
            final User user = byEmail.process(email, this::getAddress);
            final ResultSet set = stmt.executeQuery(format("SELECT * FROM Rideshare.RideReceipt WHERE riderID = %d", user.getUsrID()));
            while(set.next())
                rides.add(new Ride(set.getInt("rideID"), getRoute(set.getInt("routeID"), byId), user, Timestamp.valueOf(set.getString("rideDateTime")), set.getInt("completed") == TRUE_BIT, set.getInt("accepted") == TRUE_BIT, set.getInt("started") == TRUE_BIT));
            return rides;
        });
    }

    public List<Ride> getRidesByDriverId(int id, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Ride> rides = new ArrayList<>();
            final ResultSet set = stmt.executeQuery(format("SELECT receipt.* FROM Rideshare.RideReceipt AS receipt " +
                    "JOIN Rideshare.Route AS route ON receipt.routeID = route.routeID " +
                    "WHERE route.driverID = %d", id));
            while(set.next())
                rides.add(new Ride(set.getInt("rideID"), getRoute(set.getInt("routeID"), byId), byId.process(set.getInt("riderID"), this::getAddress), Timestamp.valueOf(set.getString("rideDateTime")), set.getInt("completed") == TRUE_BIT, set.getInt("accepted") == TRUE_BIT, set.getInt("started") == TRUE_BIT));
            return rides;
        });
    }

    public List<Ride> getRidesByDriverEmail(String email, Database.DataBiFunction<String, Database.DataFunction<Integer, Address>, User> byEmail, Database.DataBiFunction<Integer, Database.DataFunction<Integer, Address>, User> byId) throws SQLException {
        return database.function(database.connection(), stmt -> {
            final List<Ride> rides = new ArrayList<>();
            final User user = byEmail.process(email, this::getAddress);
            final ResultSet set = stmt.executeQuery(format("SELECT receipt.* FROM Rideshare.RideReceipt AS receipt " +
                    "JOIN Rideshare.Route AS route ON receipt.routeID = route.routeID " +
                    "WHERE route.driverID = %d", user.getUsrID()));
            while(set.next())
                rides.add(new Ride(set.getInt("rideID"), getRoute(set.getInt("routeID"), byId), byId.process(set.getInt("riderID"), this::getAddress), Timestamp.valueOf(set.getString("rideDateTime")), set.getInt("completed") == TRUE_BIT, set.getInt("accepted") == TRUE_BIT, set.getInt("started") == TRUE_BIT));
            return rides;
        });
    }

    private String length(String origin, int length) {
        origin = nullToEmpty(origin);
        if (origin.length() > length) return origin.substring(0, length);
        return origin;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return Objects.equals(value, "") ? null : value;
    }
}
