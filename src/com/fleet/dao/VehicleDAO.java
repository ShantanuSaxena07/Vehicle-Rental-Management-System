package com.fleet.dao;

import com.fleet.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleDAO {

    // 1. Fetch and print all vehicles from MySQL
    public void displayAllVehicles() {
        String sql = "SELECT vehicle_id, model, plate_number, category, daily_rate, status FROM Vehicles";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- Current Vehicle Fleet (From MySQL Database) ---");
            System.out.printf("%-5s %-18s %-16s %-12s %-10s %-10s%n", "ID", "Model", "Plate No", "Category", "Rate/Day", "Status");
            System.out.println("-------------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("vehicle_id");
                String model = rs.getString("model");
                String plate = rs.getString("plate_number");
                String category = rs.getString("category");
                double rate = rs.getDouble("daily_rate");
                String status = rs.getString("status");

                System.out.printf("%-5d %-18s %-16s %-12s Rs.%-7.2f  %-10s%n", id, model, plate, category, rate, status);
            }
            System.out.println("-------------------------------------------------------------------------\n");

        } catch (SQLException e) {
            System.err.println("[Error] Failed to fetch vehicles: " + e.getMessage());
        }
    }

    // 2. Insert a new vehicle into MySQL with manual vehicle_id
    public boolean addVehicle(int vehicleId, String model, String plateNumber, String category, double dailyRate) {
        String sql = "INSERT INTO Vehicles (vehicle_id, model, plate_number, category, daily_rate, status) VALUES (?, ?, ?, ?, ?, 'AVAILABLE')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            ps.setString(2, model);
            ps.setString(3, plateNumber);
            ps.setString(4, category);
            ps.setDouble(5, dailyRate);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[Error] Failed to insert vehicle. Duplicate ID or Plate Number: " + e.getMessage());
            return false;
        }
    }

    // 3. Delete a vehicle by ID
    public boolean deleteVehicle(int vehicleId) {
        String checkRentalSql = "SELECT COUNT(*) FROM Rentals WHERE vehicle_id = ? AND rental_status = 'ACTIVE'";
        String deleteSql = "DELETE FROM Vehicles WHERE vehicle_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement psCheck = con.prepareStatement(checkRentalSql)) {
                psCheck.setInt(1, vehicleId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("[Warning] Cannot delete vehicle: currently marked in an ACTIVE rental.");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                ps.setInt(1, vehicleId);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("[Error] Failed to delete vehicle: " + e.getMessage());
            return false;
        }
    }
}