-- Database Initialization Script
CREATE DATABASE IF NOT EXISTS VehicleRentalDB;
USE VehicleRentalDB;

-- 1. Vehicles Table
CREATE TABLE IF NOT EXISTS Vehicles (
    vehicle_id INT PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    plate_number VARCHAR(30) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    daily_rate DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'RENTED') DEFAULT 'AVAILABLE'
);

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS Customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    license_number VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Rentals Table (Foreign Keys & Constraints)
CREATE TABLE IF NOT EXISTS Rentals (
    rental_id INT PRIMARY KEY,
    customer_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    rental_days INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    rental_status ENUM('ACTIVE', 'COMPLETED') DEFAULT 'ACTIVE',
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_vehicle FOREIGN KEY (vehicle_id) REFERENCES Vehicles(vehicle_id) ON DELETE RESTRICT
);

-- Initial Mock Data
INSERT INTO Vehicles (vehicle_id, model, plate_number, category, daily_rate, status) VALUES
(101, 'Hyundai Creta', 'RJ-14-AB-1001', 'SUV', 2800.00, 'AVAILABLE'),
(102, 'Honda City', 'RJ-14-CD-2002', 'Sedan', 2200.00, 'AVAILABLE'),
(103, 'Royal Enfield Hunter 350', 'RJ-14-EF-3003', 'Bike', 900.00, 'AVAILABLE')
ON DUPLICATE KEY UPDATE vehicle_id = vehicle_id;

INSERT INTO Customers (customer_id, name, phone, email, license_number) VALUES
(501, 'Archit Dada', '9876543210', 'archit@example.com', 'DL-RJ-2022-0045'),
(502, 'Shantanu', '9812345678', 'shantanu@example.com', 'DL-RJ-2023-0112')
ON DUPLICATE KEY UPDATE customer_id = customer_id;