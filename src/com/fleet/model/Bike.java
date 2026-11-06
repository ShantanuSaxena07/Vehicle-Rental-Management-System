package com.fleet.model;

public class Bike extends Vehicle {
    private boolean helmetIncluded;

    public Bike(int vehicleId, String model, String plateNumber, double dailyRate, String status, boolean helmetIncluded) {
        super(vehicleId, model, plateNumber, "Bike", dailyRate, status);
        this.helmetIncluded = helmetIncluded;
    }

    public boolean isHelmetIncluded() { return helmetIncluded; }

    @Override
    public double calculateRentalCost(int days) {
        // Demonstrates method overriding / polymorphism
        double total = getDailyRate() * days;
        if (helmetIncluded) {
            total += (50.0 * days); // Rs. 50/day helmet fee
        }
        return total;
    }
}