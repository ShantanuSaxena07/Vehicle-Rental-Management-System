package com.fleet.main;

import com.fleet.dao.CustomerDAO;
import com.fleet.dao.RentalDAO;
import com.fleet.dao.VehicleDAO;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VehicleDAO vehicleDAO = new VehicleDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        RentalDAO rentalDAO = new RentalDAO();

        System.out.println("=================================================");
        System.out.println("      VEHICLE RENTAL MANAGEMENT SYSTEM           ");
        System.out.println("         (Integrated with MySQL RDBMS)           ");
        System.out.println("=================================================");

        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect an Operation:");
            System.out.println("1. View Available Fleet");
            System.out.println("2. Add New Vehicle");
            System.out.println("3. Delete Vehicle");
            System.out.println("4. View All Customers");
            System.out.println("5. Register New Customer");
            System.out.println("6. Delete Customer");
            System.out.println("7. Book a Vehicle (With Rental Duration)");
            System.out.println("8. Return a Vehicle (Preview Bill -> Settle)");
            System.out.println("9. View Active Rentals");
            System.out.println("10. Exit");
            System.out.print("Enter choice (1-10): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> vehicleDAO.displayAllVehicles();

                case 2 -> {
                    try {
                        System.out.print("Enter Vehicle ID: ");
                        int vId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Model: ");
                        String model = scanner.nextLine().trim();
                        System.out.print("Enter Plate Number: ");
                        String plate = scanner.nextLine().trim();
                        System.out.print("Enter Category (Sedan/SUV/Bike/Hatchback): ");
                        String category = scanner.nextLine().trim();
                        System.out.print("Enter Daily Rate (Rs.): ");
                        double rate = Double.parseDouble(scanner.nextLine().trim());

                        if (vehicleDAO.addVehicle(vId, model, plate, category, rate)) {
                            System.out.println(">>> Vehicle added successfully! <<<");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format for ID or Rate.");
                    }
                }

                case 3 -> {
                    System.out.print("Enter Vehicle ID to delete: ");
                    try {
                        int vId = Integer.parseInt(scanner.nextLine().trim());
                        if (vehicleDAO.deleteVehicle(vId)) {
                            System.out.println(">>> Vehicle deleted successfully! <<<");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                }

                case 4 -> customerDAO.displayAllCustomers();

                case 5 -> {
                    try {
                        System.out.print("Enter Customer ID: ");
                        int cId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Customer Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Phone: ");
                        String phone = scanner.nextLine().trim();
                        System.out.print("Enter Email: ");
                        String email = scanner.nextLine().trim();
                        System.out.print("Enter Driving License Number: ");
                        String license = scanner.nextLine().trim();

                        if (customerDAO.addCustomer(cId, name, phone, email, license)) {
                            System.out.println(">>> Customer registered successfully into MySQL! <<<");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Customer ID format.");
                    }
                }

                case 6 -> {
                    System.out.print("Enter Customer ID to delete: ");
                    try {
                        int cId = Integer.parseInt(scanner.nextLine().trim());
                        if (customerDAO.deleteCustomer(cId)) {
                            System.out.println(">>> Customer deleted successfully! <<<");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                }

                case 7 -> {
                    try {
                        System.out.print("Enter Rental ID: ");
                        int rentId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Customer ID: ");
                        int custId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Vehicle ID: ");
                        int vehId = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Number of Days to Rent: ");
                        int days = Integer.parseInt(scanner.nextLine().trim());

                        rentalDAO.bookVehicle(rentId, custId, vehId, days);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID or duration format.");
                    }
                }

                case 8 -> {
                    try {
                        System.out.print("Enter Rental ID to Return: ");
                        int rentId = Integer.parseInt(scanner.nextLine().trim());

                        RentalDAO.BillSummary bill = rentalDAO.calculateReturnBill(rentId);
                        if (bill != null) {
                            System.out.println("\n---------------- BILL PREVIEW ----------------");
                            System.out.println("Customer     : " + bill.customerName);
                            System.out.println("Vehicle      : " + bill.model);
                            System.out.println("Rate / Day   : Rs. " + bill.dailyRate);
                            System.out.println("Total Days   : " + bill.days);
                            System.out.println("Total Amount : Rs. " + bill.totalBill);
                            System.out.println("----------------------------------------------");
                            System.out.print("Do you want to confirm and settle this bill? (y/n): ");
                            String confirm = scanner.nextLine().trim();

                            if (confirm.equalsIgnoreCase("y")) {
                                rentalDAO.returnVehicle(rentId);
                            } else {
                                System.out.println("Settlement cancelled. Record remains active.");
                            }
                        } else {
                            System.out.println("[Notice] Active rental record not found for ID: " + rentId);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Rental ID format.");
                    }
                }

                case 9 -> rentalDAO.displayActiveRentals();

                case 10 -> {
                    System.out.println("Exiting system.");
                    exit = true;
                }

                default -> System.out.println("Invalid choice. Select 1 to 10.");
            }
        }
        scanner.close();
    }
}