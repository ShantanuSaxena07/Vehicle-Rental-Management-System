package com.fleet.web;

import com.fleet.dao.CustomerDAO;
import com.fleet.dao.RentalDAO;
import com.fleet.dao.VehicleDAO;
import com.fleet.db.DBConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class WebServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new IndexHandler());

        // Vehicle APIs
        server.createContext("/api/vehicles", new VehiclesHandler());
        server.createContext("/api/add-vehicle", new AddVehicleHandler());
        server.createContext("/api/delete-vehicle", new DeleteVehicleHandler());

        // Customer APIs
        server.createContext("/api/customers", new CustomersHandler());
        server.createContext("/api/add-customer", new AddCustomerHandler());
        server.createContext("/api/delete-customer", new DeleteCustomerHandler());

        // Rental APIs
        server.createContext("/api/rentals", new RentalsHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/api/calculate-bill", new CalculateBillHandler());
        server.createContext("/api/return", new ReturnHandler());

        server.setExecutor(null);
        System.out.println("=======================================================");
        System.out.println(" Web Server running at: http://localhost:8080/ ");
        System.out.println("=======================================================");
        server.start();
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = getHtmlContent().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class VehiclesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT vehicle_id, model, plate_number, category, daily_rate, status FROM Vehicles";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                            "{\"id\":%d,\"model\":\"%s\",\"plate\":\"%s\",\"category\":\"%s\",\"rate\":%.2f,\"status\":\"%s\"}",
                            rs.getInt("vehicle_id"),
                            escape(rs.getString("model")),
                            escape(rs.getString("plate_number")),
                            escape(rs.getString("category")),
                            rs.getDouble("daily_rate"),
                            escape(rs.getString("status"))
                    ));
                    first = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            json.append("]");
            sendJsonResponse(exchange, json.toString());
        }
    }

    static class AddVehicleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int vehicleId = Integer.parseInt(params.get("id"));
                    String model = params.get("model");
                    String plate = params.get("plate");
                    String category = params.get("category");
                    double rate = Double.parseDouble(params.get("rate"));

                    VehicleDAO dao = new VehicleDAO();
                    boolean ok = dao.addVehicle(vehicleId, model, plate, category, rate);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    static class DeleteVehicleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int vehicleId = Integer.parseInt(params.get("vehicleId"));
                    VehicleDAO dao = new VehicleDAO();
                    boolean ok = dao.deleteVehicle(vehicleId);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    static class CustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT customer_id, name, phone, email, license_number FROM Customers";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                            "{\"id\":%d,\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"license\":\"%s\"}",
                            rs.getInt("customer_id"),
                            escape(rs.getString("name")),
                            escape(rs.getString("phone")),
                            escape(rs.getString("email")),
                            escape(rs.getString("license_number"))
                    ));
                    first = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            json.append("]");
            sendJsonResponse(exchange, json.toString());
        }
    }

    static class AddCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int customerId = Integer.parseInt(params.get("id"));
                    String name = params.get("name");
                    String phone = params.get("phone");
                    String email = params.get("email");
                    String license = params.get("license");

                    CustomerDAO dao = new CustomerDAO();
                    boolean ok = dao.addCustomer(customerId, name, phone, email, license);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    static class DeleteCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int customerId = Integer.parseInt(params.get("customerId"));
                    CustomerDAO dao = new CustomerDAO();
                    boolean ok = dao.deleteCustomer(customerId);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    // Reads only rental_days, no rental_date
    static class RentalsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            String sql = "SELECT r.rental_id, c.name AS customer_name, v.model AS vehicle_model, r.rental_days, r.rental_status " +
                         "FROM Rentals r JOIN Customers c ON r.customer_id = c.customer_id " +
                         "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " +
                         "WHERE r.rental_status = 'ACTIVE'";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                            "{\"rental_id\":%d,\"customer\":\"%s\",\"model\":\"%s\",\"days\":%d,\"status\":\"%s\"}",
                            rs.getInt("rental_id"),
                            escape(rs.getString("customer_name")),
                            escape(rs.getString("vehicle_model")),
                            rs.getInt("rental_days"),
                            escape(rs.getString("rental_status"))
                    ));
                    first = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            json.append("]");
            sendJsonResponse(exchange, json.toString());
        }
    }

    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int rentalId = Integer.parseInt(params.get("rentalId"));
                    int custId = Integer.parseInt(params.get("customerId"));
                    int vehId = Integer.parseInt(params.get("vehicleId"));
                    int days = Integer.parseInt(params.get("days"));

                    RentalDAO dao = new RentalDAO();
                    boolean ok = dao.bookVehicle(rentalId, custId, vehId, days);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    static class CalculateBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int rentId = Integer.parseInt(params.get("rentalId"));
                    RentalDAO dao = new RentalDAO();
                    RentalDAO.BillSummary bill = dao.calculateReturnBill(rentId);

                    if (bill != null) {
                        String json = String.format(
                                "{\"success\":true,\"rentalId\":%d,\"customer\":\"%s\",\"model\":\"%s\",\"rate\":%.2f,\"days\":%d,\"total\":%.2f}",
                                bill.rentalId, escape(bill.customerName), escape(bill.model), bill.dailyRate, bill.days, bill.totalBill);
                        sendJsonResponse(exchange, json);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    static class ReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange.getRequestBody());
                try {
                    int rentId = Integer.parseInt(params.get("rentalId"));
                    RentalDAO dao = new RentalDAO();
                    boolean ok = dao.returnVehicle(rentId);
                    sendJsonResponse(exchange, "{\"success\":" + ok + "}");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendJsonResponse(exchange, "{\"success\":false}");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseBody(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) body.append(line);
        Map<String, String> map = new HashMap<>();
        String[] pairs = body.toString().split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escape(String s) {
        return (s == null) ? "" : s.replace("\"", "\\\"");
    }

    private static String getHtmlContent() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Fleet Rental - Management Console</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
            body { background-color: #EFEFE9; color: #2B2B2B; padding: 40px 60px; display: flex; justify-content: center; }
            .container { width: 100%; max-width: 1140px; display: flex; flex-direction: column; gap: 24px; }
            .header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; }
            .brand { display: flex; align-items: center; gap: 14px; }
            .brand-icon { width: 42px; height: 42px; border: 1px solid #D1D1C7; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px; background: #FAF9F5; }
            .brand-title { font-size: 24px; font-weight: 700; color: #1C1C1C; }
            .brand-subtitle { font-size: 11px; text-transform: uppercase; letter-spacing: 0.8px; color: #7A7A72; margin-top: 2px; }
            .status-pill { border: 1px solid #C5C5B8; background: #FAF9F5; border-radius: 20px; padding: 6px 14px; font-size: 12px; font-weight: 600; color: #4A4A42; display: flex; align-items: center; gap: 6px; }
            .status-dot { width: 7px; height: 7px; background: #557A55; border-radius: 50%; display: inline-block; }
            .card { background: #FAF9F5; border: 1px solid #DCDCD2; border-radius: 14px; padding: 24px; }
            .card-title { font-size: 16px; font-weight: 700; margin-bottom: 18px; color: #222; }
            table { width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; }
            th { padding: 12px 14px; font-weight: 700; color: #222; border-bottom: 1px solid #2B2B2B; }
            td { padding: 12px 14px; border-bottom: 1px solid #E5E5DC; color: #3A3A3A; vertical-align: middle; }
            .badge { display: inline-block; padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 700; }
            .badge-available { border: 1px solid #2B2B2B; color: #2B2B2B; background: transparent; }
            .badge-rented, .badge-active { background: #9EAB94; color: #1E2B1A; border: 1px solid #8B9982; }
            .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
            .form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
            label { font-size: 12px; font-weight: 700; color: #2B2B2B; }
            input, select { width: 100%; border: 1px solid #D0D0C6; border-radius: 8px; padding: 9px 12px; font-size: 13px; background: #FFFFFF; color: #333; outline: none; }
            input:focus, select:focus { border-color: #7A8C74; }
            .btn { width: 100%; background: #9EAB94; border: 1px solid #899780; border-radius: 8px; padding: 10px; font-size: 13px; font-weight: 700; color: #1A2616; cursor: pointer; transition: background 0.15s ease; margin-top: 4px; }
            .btn:hover { background: #8E9B85; }
            .btn-settle { background: #2B2B2B; color: #FFF; border: none; }
            .btn-settle:hover { background: #444; }
            .btn-delete { background: #D9534F; color: #FFF; border: none; padding: 5px 12px; border-radius: 6px; font-size: 11px; font-weight: 600; cursor: pointer; }
            .btn-delete:hover { background: #C9302C; }
            .bill-box { background: #F1F1E8; border: 1px dashed #A6A696; border-radius: 8px; padding: 12px; margin-top: 12px; font-size: 12px; line-height: 1.6; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <div class="brand">
                <div class="brand-icon">&#128663;</div>
                <div>
                  <div class="brand-title">Fleet Rental</div>
                  <div class="brand-subtitle">Database Management Console</div>
                </div>
              </div>
            </div>

            <!-- Fleet Inventory -->
            <div class="card">
              <div class="card-title">Available Fleet</div>
              <table>
                <thead>
                  <tr><th>ID</th><th>Model</th><th>Plate No</th><th>Category</th><th>Rate/Day (₹)</th><th>Status</th><th>Action</th></tr>
                </thead>
                <tbody id="fleetTableBody"><tr><td colspan="7" style="text-align:center;">Loading fleet...</td></tr></tbody>
              </table>
            </div>

            <!-- Customer Directory -->
            <div class="card">
              <div class="card-title">Registered Customers</div>
              <table>
                <thead>
                  <tr><th>ID</th><th>Customer Name</th><th>Phone</th><th>Email</th><th>License Number</th><th>Action</th></tr>
                </thead>
                <tbody id="customersTableBody"><tr><td colspan="6" style="text-align:center;">Loading customers...</td></tr></tbody>
              </table>
            </div>

            <!-- 4 Actions Grid -->
            <div class="grid-4">
              <!-- Register Customer -->
              <div class="card">
                <div class="card-title">Register Customer</div>
                <div class="form-group"><label>Customer ID</label><input type="number" id="custId" placeholder="e.g. 101"></div>
                <div class="form-group"><label>Full Name</label><input type="text" id="custName" placeholder="e.g. Rahul Sharma"></div>
                <div class="form-group"><label>Phone</label><input type="text" id="custPhone" placeholder="e.g. 9876543210"></div>
                <div class="form-group"><label>Email</label><input type="email" id="custEmail" placeholder="e.g. rahul@mail.com"></div>
                <div class="form-group"><label>License Number</label><input type="text" id="custLicense" placeholder="e.g. RJ14 2021001234"></div>
                <button class="btn" onclick="addCustomer()">Add Customer</button>
              </div>

              <!-- Add Vehicle -->
              <div class="card">
                <div class="card-title">Add Vehicle</div>
                <div class="form-group"><label>Vehicle ID</label><input type="number" id="addVehId" placeholder="e.g. 201"></div>
                <div class="form-group"><label>Model</label><input type="text" id="addModel" placeholder="e.g. Thar ROXX"></div>
                <div class="form-group"><label>Plate</label><input type="text" id="addPlate" placeholder="e.g. RJ 14 AB 1234"></div>
                <div class="form-group"><label>Category</label>
                  <select id="addCategory">
                    <option value="Sedan">Sedan</option><option value="SUV">SUV</option><option value="Bike">Bike</option><option value="Hatchback">Hatchback</option>
                  </select>
                </div>
                <div class="form-group"><label>Rate (₹)</label><input type="number" id="addRate" placeholder="e.g. 2500"></div>
                <button class="btn" onclick="addVehicle()">Add to Fleet</button>
              </div>

              <!-- Book Vehicle -->
              <div class="card">
                <div class="card-title">Book Vehicle</div>
                <div class="form-group"><label>Rental ID</label><input type="number" id="bookRentId" placeholder="e.g. 301"></div>
                <div class="form-group"><label>Customer ID</label><input type="number" id="bookCustId" placeholder="e.g. 101"></div>
                <div class="form-group"><label>Vehicle ID</label><input type="number" id="bookVehId" placeholder="e.g. 201"></div>
                <div class="form-group"><label>Duration (Days)</label><input type="number" id="bookDays" placeholder="e.g. 3" min="1" value="1"></div>
                <button class="btn" onclick="bookVehicle()">Confirm Booking</button>
              </div>

              <!-- Return Vehicle -->
              <div class="card">
                <div class="card-title">Return Vehicle</div>
                <div class="form-group"><label>Rental ID</label><input type="number" id="returnRentId" placeholder="e.g. 301"></div>
                <button class="btn" onclick="calculateBill()">Calculate Bill</button>
                <div id="billPreviewArea" style="display:none;" class="bill-box">
                  <div><strong>Customer:</strong> <span id="billCust"></span></div>
                  <div><strong>Vehicle:</strong> <span id="billVeh"></span></div>
                  <div><strong>Rate/Day:</strong> ₹<span id="billRate"></span></div>
                  <div><strong>Total Days:</strong> <span id="billDays"></span></div>
                  <hr style="margin: 6px 0; border: none; border-top: 1px solid #CCC;">
                  <div style="font-size: 14px; font-weight:700;">Final Amount: ₹<span id="billTotal"></span></div>
                  <button class="btn btn-settle" onclick="confirmSettle()">Confirm & Settle</button>
                </div>
              </div>
            </div>

            <!-- Active Rentals -->
            <div class="card">
              <div class="card-title">Active Rentals</div>
              <table>
                <thead><tr><th>Rental ID</th><th>Customer Name</th><th>Vehicle Model</th><th>Duration</th><th>Status</th></tr></thead>
                <tbody id="rentalsTableBody"><tr><td colspan="5" style="text-align:center;">Loading active rentals...</td></tr></tbody>
              </table>
            </div>
          </div>

          <script>
            let currentSettleId = null;

            async function loadData() {
              const fleetRes = await fetch('/api/vehicles');
              const fleet = await fleetRes.json();
              const fleetBody = document.getElementById('fleetTableBody');
              fleetBody.innerHTML = '';
              fleet.forEach(v => {
                const badgeClass = v.status === 'AVAILABLE' ? 'badge-available' : 'badge-rented';
                fleetBody.innerHTML += `<tr>
                  <td>V-${v.id}</td>
                  <td>${v.model}</td>
                  <td>${v.plate}</td>
                  <td>${v.category}</td>
                  <td>₹${v.rate.toLocaleString()}</td>
                  <td><span class="badge ${badgeClass}">${v.status}</span></td>
                  <td><button class="btn-delete" onclick="deleteVehicle(${v.id})">Delete</button></td>
                </tr>`;
              });

              const custRes = await fetch('/api/customers');
              const customers = await custRes.json();
              const custBody = document.getElementById('customersTableBody');
              custBody.innerHTML = '';
              if (customers.length === 0) {
                custBody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#777;">No registered customers found.</td></tr>';
              } else {
                customers.forEach(c => {
                  custBody.innerHTML += `<tr>
                    <td>C-${c.id}</td>
                    <td>${c.name}</td>
                    <td>${c.phone || '-'}</td>
                    <td>${c.email || '-'}</td>
                    <td>${c.license || '-'}</td>
                    <td><button class="btn-delete" onclick="deleteCustomer(${c.id})">Delete</button></td>
                  </tr>`;
                });
              }

              const rentRes = await fetch('/api/rentals');
              const rentals = await rentRes.json();
              const rentBody = document.getElementById('rentalsTableBody');
              rentBody.innerHTML = '';
              if (rentals.length === 0) {
                rentBody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#777;">No active rentals found.</td></tr>';
              } else {
                rentals.forEach(r => {
                  rentBody.innerHTML += `<tr>
                    <td>R-${r.rental_id}</td>
                    <td>${r.customer}</td>
                    <td>${r.model}</td>
                    <td>${r.days} Day(s)</td>
                    <td><span class="badge badge-active">${r.status}</span></td>
                  </tr>`;
                });
              }
            }

            async function addCustomer() {
              const id = document.getElementById('custId').value;
              const name = document.getElementById('custName').value;
              const phone = document.getElementById('custPhone').value;
              const email = document.getElementById('custEmail').value;
              const license = document.getElementById('custLicense').value;

              if (!id || !name || !license) return alert('Customer ID, Name, and License are required!');
              const res = await fetch('/api/add-customer', {
                method: 'POST',
                body: `id=${id}&name=${encodeURIComponent(name)}&phone=${encodeURIComponent(phone)}&email=${encodeURIComponent(email)}&license=${encodeURIComponent(license)}`
              });
              const data = await res.json();
              if (data.success) {
                alert('Customer registered successfully!');
                document.getElementById('custId').value = '';
                document.getElementById('custName').value = '';
                document.getElementById('custPhone').value = '';
                document.getElementById('custEmail').value = '';
                document.getElementById('custLicense').value = '';
                loadData();
              } else {
                alert('Failed to register customer. Check duplicate ID or License.');
              }
            }

            async function addVehicle() {
              const id = document.getElementById('addVehId').value;
              const model = document.getElementById('addModel').value;
              const plate = document.getElementById('addPlate').value;
              const category = document.getElementById('addCategory').value;
              const rate = document.getElementById('addRate').value;

              if (!id || !model || !plate || !rate) return alert('Fill in all vehicle details including Vehicle ID');
              const res = await fetch('/api/add-vehicle', {
                method: 'POST',
                body: `id=${id}&model=${encodeURIComponent(model)}&plate=${encodeURIComponent(plate)}&category=${encodeURIComponent(category)}&rate=${rate}`
              });
              const data = await res.json();
              if (data.success) {
                alert('New vehicle added!');
                document.getElementById('addVehId').value = '';
                document.getElementById('addModel').value = '';
                document.getElementById('addPlate').value = '';
                document.getElementById('addRate').value = '';
                loadData();
              } else {
                alert('Could not add vehicle. Duplicate ID or Plate?');
              }
            }

            async function bookVehicle() {
              const rentalId = document.getElementById('bookRentId').value;
              const customerId = document.getElementById('bookCustId').value;
              const vehicleId = document.getElementById('bookVehId').value;
              const days = document.getElementById('bookDays').value;

              if (!rentalId || !customerId || !vehicleId || !days) return alert('Enter Rental ID, Customer ID, Vehicle ID, and Duration');
              
              const res = await fetch('/api/book', { 
                method: 'POST', 
                body: `rentalId=${rentalId}&customerId=${customerId}&vehicleId=${vehicleId}&days=${days}` 
              });
              const data = await res.json();
              if (data.success) {
                alert('Vehicle booked successfully!');
                document.getElementById('bookRentId').value = '';
                document.getElementById('bookCustId').value = '';
                document.getElementById('bookVehId').value = '';
                document.getElementById('bookDays').value = '1';
                loadData();
              } else {
                alert('Booking failed. Duplicate Rental ID, Invalid IDs, or Vehicle already rented.');
              }
            }

            async function calculateBill() {
              const rentalId = document.getElementById('returnRentId').value;
              if (!rentalId) return alert('Please enter Rental ID');

              const res = await fetch('/api/calculate-bill', {
                method: 'POST',
                body: `rentalId=${rentalId}`
              });
              const data = await res.json();
              if (data.success) {
                currentSettleId = data.rentalId;
                document.getElementById('billCust').innerText = data.customer;
                document.getElementById('billVeh').innerText = data.model;
                document.getElementById('billRate').innerText = data.rate;
                document.getElementById('billDays').innerText = data.days;
                document.getElementById('billTotal').innerText = data.total;
                document.getElementById('billPreviewArea').style.display = 'block';
              } else {
                alert('Rental ID not found or already returned.');
                document.getElementById('billPreviewArea').style.display = 'none';
              }
            }

            async function confirmSettle() {
              if (!currentSettleId) return;
              const res = await fetch('/api/return', {
                method: 'POST',
                body: `rentalId=${currentSettleId}`
              });
              const data = await res.json();
              if (data.success) {
                alert('Rental completed and settled successfully!');
                document.getElementById('billPreviewArea').style.display = 'none';
                document.getElementById('returnRentId').value = '';
                currentSettleId = null;
                loadData();
              } else {
                alert('Settlement failed.');
              }
            }

            async function deleteCustomer(id) {
              if (!confirm('Are you sure you want to delete Customer ID: ' + id + '?')) return;
              const res = await fetch('/api/delete-customer', {
                method: 'POST',
                body: `customerId=${id}`
              });
              const data = await res.json();
              if (data.success) { alert('Customer deleted!'); loadData(); }
              else { alert('Could not delete customer.'); }
            }

            async function deleteVehicle(id) {
              if (!confirm('Are you sure you want to delete Vehicle ID: ' + id + '?')) return;
              const res = await fetch('/api/delete-vehicle', {
                method: 'POST',
                body: `vehicleId=${id}`
              });
              const data = await res.json();
              if (data.success) { alert('Vehicle deleted!'); loadData(); }
              else { alert('Could not delete vehicle.'); }
            }

            loadData();
          </script>
        </body>
        </html>
        """;
    }
}