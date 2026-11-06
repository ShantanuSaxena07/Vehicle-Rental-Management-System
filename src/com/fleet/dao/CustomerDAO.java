package com.fleet.dao;

import com.fleet.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    // 1. Display all customers in console
    public void displayAllCustomers() {
        String sql = "SELECT customer_id, name, phone, email, license_number FROM Customers";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Registered Customers ---");
            System.out.printf("%-6s %-20s %-15s %-25s %-18s%n", "ID", "Name", "Phone", "Email", "License No");
            System.out.println("-----------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-6d %-20s %-15s %-25s %-18s%n",
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("license_number"));
            }
            System.out.println("-----------------------------------------------------------------------------------------\n");

        } catch (SQLException e) {
            System.err.println("[Error] Failed to fetch customers: " + e.getMessage());
        }
    }

    // 2. Add a new customer with manual customer_id
    public boolean addCustomer(int customerId, String name, String phone, String email, String licenseNumber) {
        String sql = "INSERT INTO Customers (customer_id, name, phone, email, license_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, licenseNumber);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[Error] Failed to insert customer. Duplicate ID or License Number: " + e.getMessage());
            return false;
        }
    }

    // 3. Delete a customer
    public boolean deleteCustomer(int customerId) {
        String checkRentalSql = "SELECT COUNT(*) FROM Rentals WHERE customer_id = ? AND rental_status = 'ACTIVE'";
        String deleteSql = "DELETE FROM Customers WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement psCheck = con.prepareStatement(checkRentalSql)) {
                psCheck.setInt(1, customerId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("[Warning] Cannot delete customer: currently has an ongoing ACTIVE rental.");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, customerId);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("[Error] Failed to delete customer: " + e.getMessage());
            return false;
        }
    }
}