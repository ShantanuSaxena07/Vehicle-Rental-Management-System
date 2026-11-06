# Vehicle Rental Management System

A dual-tier enterprise management application built using **Object-Oriented Programming (OOP) in Java**, backed by **MySQL (RDBMS)**, and featuring both an **Interactive Console Interface** and a **Web Dashboard**.

---

## Academic Rubric Mapping

### 1. Object-Oriented Programming (OOP) Principles
* **Encapsulation:** Implemented across domain entities (`Vehicle`, `Customer`) using `private` instance variables with explicit getters, setters, and validation constraints.
* **Inheritance:** `Vehicle` serves as an abstract base class extended by specialized classes such as `Car` and `Bike`.
* **Polymorphism & Dynamic Dispatch:** `calculateRentalCost(int days)` is overridden in subclasses to apply category-specific rate structures.
* **Separation of Concerns (DAO Pattern):** Data Access Objects (`VehicleDAO`, `CustomerDAO`, `RentalDAO`) cleanly decouple business logic from raw SQL query execution.

### 2. Relational Database Management (RDBMS)
* **ACID Transactions:** `RentalDAO.java` utilizes explicit JDBC transaction management (`con.setAutoCommit(false)`, `con.commit()`, `con.rollback()`) ensuring atomic state updates when booking and settling returns.
* **Relational Integrity:** Implemented foreign key constraints (`fk_customer`, `fk_vehicle`) with `ON DELETE RESTRICT` to preserve referential consistency.
* **Multi-Table Queries:** Uses relational `INNER JOIN` operations to compile active lease summaries and calculate return bills.

---

## Architecture & Interfaces

1. **CLI Management Terminal (`Main.java`):** Menu-driven terminal suite for fleet managers.
2. **REST-backed Web Dashboard (`WebServer.java`):** Lightweight, zero-dependency embedded HTTP server serving a single-page responsive control center at `http://localhost:8080/`.

---

## Setup & Execution

Database Configuration:
Before running the application, open src/com/fleet/db/DBConnection.java and set PASSWORD to match your local MySQL root password

### Prerequisites
* Java JDK 17 or higher
* MySQL Server (v8.0+)
* MySQL Connector/J JDBC Driver (`mysql-connector-j.jar`)

### Database Setup
1. Open your MySQL client or CLI:
   ```bash
   mysql -u root -p < database/schema.sql