package com.fleet.dao;

import com.fleet.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RentalDAO {

    // Helper class for bill calculation preview
    public static class BillSummary {
        public int rentalId;
        public int vehicleId;
        public String model;
        public String customerName;
        public int days;
        public double dailyRate;
        public double totalBill;

        public BillSummary(int rentalId, int vehicleId, String model, String customerName,
                           int days, double dailyRate, double totalBill) {
            this.rentalId = rentalId;
            this.vehicleId = vehicleId;
            this.model = model;
            this.customerName = customerName;
            this.days = days;
            this.dailyRate = dailyRate;
            this.totalBill = totalBill;
        }
    }

    // 1. Book a vehicle: uses only rental_days (no rental_date)
    public boolean bookVehicle(int rentalId, int customerId, int vehicleId, int days) {
        String updateVehicleSql = "UPDATE Vehicles SET status = 'RENTED' WHERE vehicle_id = ? AND status = 'AVAILABLE'";
        String insertRentalSql = "INSERT INTO Rentals (rental_id, customer_id, vehicle_id, rental_days, rental_status) VALUES (?, ?, ?, ?, 'ACTIVE')";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Step A: Mark vehicle as RENTED
            try (PreparedStatement psVehicle = con.prepareStatement(updateVehicleSql)) {
                psVehicle.setInt(1, vehicleId);
                int rowsUpdated = psVehicle.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("[Notice] Vehicle is not available or does not exist.");
                    con.rollback();
                    return false;
                }
            }

            // Step B: Insert the active rental record
            try (PreparedStatement psRental = con.prepareStatement(insertRentalSql)) {
                psRental.setInt(1, rentalId);
                psRental.setInt(2, customerId);
                psRental.setInt(3, vehicleId);
                psRental.setInt(4, days <= 0 ? 1 : days);
                psRental.executeUpdate();
            }

            con.commit();
            System.out.println(">>> Booking confirmed successfully! Rental ID: " + rentalId + " for " + days + " day(s) <<<");
            return true;

        } catch (SQLException e) {
            System.err.println("[Error] Booking transaction failed: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 2. Preview the bill BEFORE finalizing the return
    public BillSummary calculateReturnBill(int rentalId) {
        String sql = "SELECT r.vehicle_id, r.rental_days, v.model, v.daily_rate, c.name AS customer_name " +
                     "FROM Rentals r " +
                     "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN Customers c ON r.customer_id = c.customer_id " +
                     "WHERE r.rental_id = ? AND r.rental_status = 'ACTIVE'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int vehicleId = rs.getInt("vehicle_id");
                    int days = rs.getInt("rental_days");
                    String model = rs.getString("model");
                    double dailyRate = rs.getDouble("daily_rate");
                    String customerName = rs.getString("customer_name");

                    double totalAmount = days * dailyRate;

                    return new BillSummary(rentalId, vehicleId, model, customerName,
                            days, dailyRate, totalAmount);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error] Failed to calculate bill: " + e.getMessage());
        }
        return null;
    }

    // 3. Confirm settlement: commits total_amount and sets status to COMPLETED
    public boolean returnVehicle(int rentalId) {
        BillSummary bill = calculateReturnBill(rentalId);
        if (bill == null) {
            System.out.println("[Notice] Active rental record not found for ID: " + rentalId);
            return false;
        }

        String updateRentalSql = "UPDATE Rentals SET total_amount = ?, rental_status = 'COMPLETED' WHERE rental_id = ?";
        String updateVehicleSql = "UPDATE Vehicles SET status = 'AVAILABLE' WHERE vehicle_id = ?";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psRental = con.prepareStatement(updateRentalSql)) {
                psRental.setDouble(1, bill.totalBill);
                psRental.setInt(2, rentalId);
                psRental.executeUpdate();
            }

            try (PreparedStatement psVehicle = con.prepareStatement(updateVehicleSql)) {
                psVehicle.setInt(1, bill.vehicleId);
                psVehicle.executeUpdate();
            }

            con.commit();
            System.out.println(">>> Vehicle returned and settled! Total Bill: Rs. " + bill.totalBill + " <<<");
            return true;

        } catch (SQLException e) {
            System.err.println("[Error] Return processing failed: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 4. View active rentals
    public void displayActiveRentals() {
        String sql = "SELECT r.rental_id, c.name AS customer_name, v.model AS vehicle_model, " +
                     "r.rental_days, r.rental_status " +
                     "FROM Rentals r " +
                     "JOIN Customers c ON r.customer_id = c.customer_id " +
                     "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "WHERE r.rental_status = 'ACTIVE'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Current Active Rentals (Multi-Table JOIN) ---");
            System.out.printf("%-10s %-20s %-18s %-8s %-10s%n", "Rental ID", "Customer", "Vehicle", "Days", "Status");
            System.out.println("-------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-10d %-20s %-18s %-8d %-10s%n",
                        rs.getInt("rental_id"),
                        rs.getString("customer_name"),
                        rs.getString("vehicle_model"),
                        rs.getInt("rental_days"),
                        rs.getString("rental_status"));
            }
            System.out.println("-------------------------------------------------------------------------\n");

        } catch (SQLException e) {
            System.err.println("[Error] Failed to fetch active rentals: " + e.getMessage());
        }
    }
}