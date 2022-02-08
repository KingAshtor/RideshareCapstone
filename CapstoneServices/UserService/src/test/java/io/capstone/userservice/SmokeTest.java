package io.capstone.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.capstone.userservice.user.User;
import io.capstone.userservice.user.UserRegistry;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.sql.*;

@SpringBootTest
public class SmokeTest {
    @Autowired
    private Controller controller;

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    public static void dbConnection() {
        String SQL = "select * from RideshareProfile";
        User usr = new User();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("ASHTON'S DB CONNECTION STRING");

            Statement stmt = conn.createStatement();
            ResultSet records = stmt.executeQuery(SQL);
            
            while(records.next()) {
                usr.setName(records.getString("first"));
                usr.setPwd(records.getString("pwd"));
                System.out.println(usr);
            }

        } catch(Exception e) {
            System.out.println("SOMETHING IS WRONG");
        }
    }
}
