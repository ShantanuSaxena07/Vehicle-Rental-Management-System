package com.fleet.model;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String email;
    private String licenseNumber;

    public Customer(int customerId, String name, String phone, String email, String licenseNumber) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getLicenseNumber() { return licenseNumber; }

    @Override
    public String toString() {
        return String.format("Customer #%d: %s | Phone: %s | License: %s",
                customerId, name, phone, licenseNumber);
    }
}