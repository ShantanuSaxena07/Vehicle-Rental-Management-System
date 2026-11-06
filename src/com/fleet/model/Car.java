package com.fleet.model;

public class Car extends Vehicle {
    private int seatingCapacity;

    public Car(int vehicleId, String model, String plateNumber, String category, double dailyRate, String status, int seatingCapacity) {
        super(vehicleId, model, plateNumber, category, dailyRate, status);
        this.seatingCapacity = seatingCapacity;
    }

    public int getSeatingCapacity() { return seatingCapacity; }

    @Override
    public double calculateRentalCost(int days) {
        // Cars have standard base daily calculation
        return getDailyRate() * days;
    }
}