package com.fleet.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Connection URL for MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/VehicleRentalDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    
    // Database credentials (placeholder added for GitHub safety)
    private static final String USER = "root";
    // NOTE FOR EVALUATION: Replace "YOUR_PASSWORD_HERE" with your local MySQL root password.
    private static final String PASSWORD = "YOUR_PASSWORD_HERE";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[Error] MySQL JDBC Driver jar not found in classpath: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                System.out.println(">>> SUCCESS: Connected to MySQL VehicleRentalDB! <<<");
            }
        } catch (SQLException e) {
            System.err.println("[Error] Connection failed: " + e.getMessage());
        }
    }
}