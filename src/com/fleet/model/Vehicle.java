package com.fleet.model;

public abstract class Vehicle {
    private int vehicleId;
    private String model;
    private String plateNumber;
    private String category;
    private double dailyRate;
    private String status;

    public Vehicle(int vehicleId, String model, String plateNumber, String category, double dailyRate, String status) {
        this.vehicleId = vehicleId;
        this.model = model;
        this.plateNumber = plateNumber;
        this.category = category;
        this.dailyRate = dailyRate;
        this.status = status;
    }

    // Getters and Setters (Encapsulation)
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Polymorphic method to be overridden by subclasses
    public abstract double calculateRentalCost(int days);

    @Override
    public String toString() {
        return String.format("[%s] ID: %d | %s | Plate: %s | Rate: Rs. %.2f/day | Status: %s",
                category, vehicleId, model, plateNumber, dailyRate, status);
    }
}