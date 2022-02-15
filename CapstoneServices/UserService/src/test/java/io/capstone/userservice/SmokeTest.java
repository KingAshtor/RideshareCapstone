package io.capstone.userservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SmokeTest {
    @Autowired
    private Controller controller;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public void testDb() {
        final String connect = "jdbc:sqlserver://funnyserver.database.windows.net:1433;database=Premiere;user=AshtonSisson@funnyserver;password=S3cureP@$$w0rd;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

        try {
            Connection con = DriverManager.getConnection(connect);
            con.close();
        } catch (SQLException e) {
            Assertions.fail("Unable to connect to db: \n" + e.getMessage());
        }
    }
}
