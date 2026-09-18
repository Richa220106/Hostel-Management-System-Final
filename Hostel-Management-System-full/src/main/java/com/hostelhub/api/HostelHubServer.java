package com.hostelhub.api;

import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.factory.ComplaintFactory;
import com.hostelhub.model.Notification;
import com.hostelhub.model.Room;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.model.Visitor;
import com.hostelhub.proxy.HostelService;
import com.hostelhub.proxy.HostelServiceProxy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class HostelHubServer {

    private static final int PORT = getAvailablePort();

    private static int getAvailablePort() {
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try { return Integer.parseInt(envPort); } catch (Exception ignored) {}
        }
        return Integer.getInteger("port", 8081);
    }

    public static void main(String[] args) throws IOException {
        // Initialize DB
        DatabaseUtil.initializeDatabase();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Attach Handlers
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/users", new UsersHandler());
        server.createContext("/api/complaints", new ComplaintsHandler());
        server.createContext("/api/rooms", new RoomsHandler());
        server.createContext("/api/visitors", new VisitorsHandler());
        server.createContext("/api/technicians", new TechniciansHandler());
        server.createContext("/api/notifications", new NotificationsHandler());
        server.createContext("/api/warden/stats", new WardenStatsHandler());
        server.createContext("/api/emergency", new EmergencyHandler());

        server.start();
        System.out.println("==================================================");
        System.out.println("HostelHub Backend Server is running on port " + PORT);
        System.out.println("Central HostelHub System & Design Patterns Active");
        System.out.println("Ready to accept connections at http://localhost:" + PORT);
        System.out.println("==================================================");
    }

    // Helper to send JSON response with CORS
    public static void sendResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id, X-User-Role");

        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void handlePreflight(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id, X-User-Role");
        exchange.sendResponseHeaders(204, -1);
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        
        // Simple JSON tokenizer for key-values
        String[] pairs = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String value = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }
        return map;
    }

    // Resolve user from headers or request parameters
    public static User resolveUser(HttpExchange exchange) {
        String userIdStr = exchange.getRequestHeaders().getFirst("X-User-Id");
        String role = exchange.getRequestHeaders().getFirst("X-User-Role");

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                int id = Integer.parseInt(userIdStr);
                return getUserById(id);
            } catch (NumberFormatException ignored) {}
        }
        
        // Fallback default: student1
        return getUserById(2);
    }

    public static User getUserById(int id) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("name"),
                        rs.getString("room_no"),
                        rs.getString("contact"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Handlers

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }
            sendResponse(exchange, 200, "{\"status\":\"ok\",\"system\":\"HostelHub\",\"time\":\"" + System.currentTimeMillis() + "\"}");
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = parseSimpleJson(body);
            String username = data.get("username");
            String password = data.get("password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "{\"error\":\"Username and password required\"}");
                return;
            }

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    String roomNo = rs.getString("room_no") != null ? rs.getString("room_no") : "";
                    String contact = rs.getString("contact") != null ? rs.getString("contact") : "";
                    String email = rs.getString("email") != null ? rs.getString("email") : "";

                    String json = String.format(
                            "{\"success\":true,\"user\":{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"name\":\"%s\",\"roomNo\":\"%s\",\"contact\":\"%s\",\"email\":\"%s\"}}",
                            id, escape(username), escape(role), escape(name), escape(roomNo), escape(contact), escape(email)
                    );
                    sendResponse(exchange, 200, json);
                } else {
                    sendResponse(exchange, 401, "{\"success\":false,\"error\":\"Invalid credentials\"}");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
            }
        }
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users ORDER BY id ASC");
                 ResultSet rs = stmt.executeQuery()) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(String.format(
                            "{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"name\":\"%s\",\"roomNo\":\"%s\",\"contact\":\"%s\",\"email\":\"%s\"}",
                            rs.getInt("id"), escape(rs.getString("username")), escape(rs.getString("role")),
                            escape(rs.getString("name")), escape(rs.getString("room_no")),
                            escape(rs.getString("contact")), escape(rs.getString("email"))
                    ));
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
            }
        }
    }

    static class ComplaintsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    // List complaints or get single complaint
                    String[] parts = path.split("/");
                    if (parts.length > 3 && !parts[3].isEmpty()) {
                        int id = Integer.parseInt(parts[3]);
                        Complaint c = service.getComplaintById(id);
                        if (c != null) {
                            sendResponse(exchange, 200, complaintToJson(c));
                        } else {
                            sendResponse(exchange, 404, "{\"error\":\"Complaint not found\"}");
                        }
                    } else {
                        List<Complaint> complaints = service.getComplaints();
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < complaints.size(); i++) {
                            if (i > 0) sb.append(",");
                            sb.append(complaintToJson(complaints.get(i)));
                        }
                        sb.append("]");
                        sendResponse(exchange, 200, sb.toString());
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    // Create complaint: Student -> Factory -> Singleton -> Chain -> Observer
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);

                    String type = data.getOrDefault("type", "Maintenance");
                    String roomNo = data.getOrDefault("roomNo", user != null ? user.getRoomNo() : "A-203");
                    String description = data.getOrDefault("description", "");
                    String priority = data.getOrDefault("priority", "MEDIUM");
                    int studentId = user != null ? user.getId() : 2;

                    if (data.containsKey("studentId")) {
                        try {
                            studentId = Integer.parseInt(data.get("studentId"));
                        } catch (NumberFormatException ignored) {}
                    }

                    // FACTORY METHOD CREATES THE COMPLAINT
                    Complaint complaint = ComplaintFactory.createComplaint(type, studentId, roomNo, description, priority);
                    
                    // PROXY VALIDATES & ROUTES TO CENTRAL SYSTEM (SINGLETON) -> CHAIN OF RESPONSIBILITY -> OBSERVER
                    Complaint created = service.createComplaint(complaint);

                    sendResponse(exchange, 201, complaintToJson(created));

                } else if ("PUT".equalsIgnoreCase(method)) {
                    // Update status or assign technician (Warden only)
                    String[] parts = path.split("/");
                    if (parts.length >= 4) {
                        int complaintId = Integer.parseInt(parts[3]);
                        String body = readBody(exchange);
                        Map<String, String> data = parseSimpleJson(body);

                        if (path.endsWith("/status") || data.containsKey("status")) {
                            String status = data.get("status");
                            service.updateComplaintStatus(complaintId, status);
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Status updated to " + escape(status) + "\"}");
                        } else if (path.endsWith("/assign") || data.containsKey("technicianId")) {
                            int techId = Integer.parseInt(data.get("technicianId"));
                            service.assignTechnician(complaintId, techId);
                            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Technician assigned successfully\"}");
                        } else {
                            sendResponse(exchange, 400, "{\"error\":\"Invalid update command\"}");
                        }
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Missing complaint ID\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (SecurityException se) {
                sendResponse(exchange, 403, "{\"error\":\"" + escape(se.getMessage()) + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    static class RoomsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);
            String method = exchange.getRequestMethod();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Room> rooms = service.getRooms();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < rooms.size(); i++) {
                        if (i > 0) sb.append(",");
                        Room r = rooms.get(i);
                        sb.append(String.format(
                                "{\"roomNo\":\"%s\",\"block\":\"%s\",\"capacity\":%d,\"currentOccupancy\":%d,\"available\":%d,\"status\":\"%s\"}",
                                escape(r.getRoomNo()), escape(r.getBlock()), r.getCapacity(), r.getCurrentOccupancy(), r.getAvailable(), escape(r.getStatus())
                        ));
                    }
                    sb.append("]");
                    sendResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    Room room = new Room(
                            data.get("roomNo"),
                            data.getOrDefault("block", "Block A"),
                            Integer.parseInt(data.getOrDefault("capacity", "2")),
                            Integer.parseInt(data.getOrDefault("currentOccupancy", "0")),
                            data.getOrDefault("status", "Available")
                    );
                    service.addRoom(room);
                    sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Room added successfully\"}");
                } else if ("PUT".equalsIgnoreCase(method)) {
                    // Allocate room
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    int studentId = Integer.parseInt(data.get("studentId"));
                    String roomNo = data.get("roomNo");
                    service.allocateRoom(studentId, roomNo);
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Room allocated successfully\"}");
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                }
            } catch (SecurityException se) {
                sendResponse(exchange, 403, "{\"error\":\"" + escape(se.getMessage()) + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    static class VisitorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);
            String method = exchange.getRequestMethod();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Visitor> visitors = service.getVisitors();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < visitors.size(); i++) {
                        if (i > 0) sb.append(",");
                        Visitor v = visitors.get(i);
                        sb.append(String.format(
                                "{\"id\":%d,\"studentId\":%d,\"studentName\":\"%s\",\"roomNo\":\"%s\",\"visitorName\":\"%s\",\"contact\":\"%s\",\"visitDate\":\"%s\",\"entryTime\":\"%s\",\"exitTime\":\"%s\",\"reason\":\"%s\",\"status\":\"%s\"}",
                                v.getId(), v.getStudentId(), escape(v.getStudentName()), escape(v.getRoomNo()),
                                escape(v.getVisitorName()), escape(v.getContact()), escape(v.getVisitDate()),
                                escape(v.getEntryTime()), escape(v.getExitTime()), escape(v.getReason()), escape(v.getStatus())
                        ));
                    }
                    sb.append("]");
                    sendResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    Visitor visitor = new Visitor();
                    visitor.setStudentId(user != null ? user.getId() : 2);
                    visitor.setRoomNo(data.getOrDefault("roomNo", user != null ? user.getRoomNo() : "A-203"));
                    visitor.setVisitorName(data.get("visitorName"));
                    visitor.setContact(data.getOrDefault("contact", ""));
                    visitor.setVisitDate(data.getOrDefault("visitDate", "Today"));
                    visitor.setEntryTime(data.getOrDefault("entryTime", "10:00 AM"));
                    visitor.setExitTime(data.getOrDefault("exitTime", "04:00 PM"));
                    visitor.setReason(data.getOrDefault("reason", "Visitor"));
                    visitor.setStatus("APPROVED");

                    service.registerVisitor(visitor);
                    sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Visitor registered successfully\"}");
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String path = exchange.getRequestURI().getPath();
                    String[] parts = path.split("/");
                    int visitorId = Integer.parseInt(parts[3]);
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    service.updateVisitorStatus(visitorId, data.getOrDefault("status", "EXITED"));
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Visitor status updated\"}");
                }
            } catch (SecurityException se) {
                sendResponse(exchange, 403, "{\"error\":\"" + escape(se.getMessage()) + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    static class TechniciansHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);
            String method = exchange.getRequestMethod();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Technician> list = service.getTechnicians();
                    
                    // Also count active tasks for each technician
                    Map<Integer, Integer> workloads = getTechnicianWorkloads();

                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        Technician t = list.get(i);
                        int activeTasks = workloads.getOrDefault(t.getId(), 0);
                        sb.append(String.format(
                                "{\"id\":%d,\"name\":\"%s\",\"department\":\"%s\",\"status\":\"%s\",\"contact\":\"%s\",\"activeTasks\":%d}",
                                t.getId(), escape(t.getName()), escape(t.getDepartment()), escape(t.getStatus()), escape(t.getContact()), activeTasks
                        ));
                    }
                    sb.append("]");
                    sendResponse(exchange, 200, sb.toString());
                } else if ("POST".equalsIgnoreCase(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    Technician tech = new Technician(
                            0,
                            data.get("name"),
                            data.get("department"),
                            "AVAILABLE",
                            data.getOrDefault("contact", "")
                    );
                    service.addTechnician(tech);
                    sendResponse(exchange, 201, "{\"success\":true,\"message\":\"Technician added\"}");
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String path = exchange.getRequestURI().getPath();
                    String[] parts = path.split("/");
                    int techId = Integer.parseInt(parts[3]);
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);
                    service.updateTechnicianStatus(techId, data.get("status"));
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Technician status updated\"}");
                }
            } catch (SecurityException se) {
                sendResponse(exchange, 403, "{\"error\":\"" + escape(se.getMessage()) + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }

        private Map<Integer, Integer> getTechnicianWorkloads() {
            Map<Integer, Integer> map = new HashMap<>();
            String sql = "SELECT technician_id, COUNT(*) as cnt FROM Complaints WHERE technician_id IS NOT NULL AND status != 'RESOLVED' GROUP BY technician_id";
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("technician_id"), rs.getInt("cnt"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return map;
        }
    }

    static class NotificationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);
            String method = exchange.getRequestMethod();

            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Notification> list = service.getNotifications();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        Notification n = list.get(i);
                        sb.append(String.format(
                                "{\"id\":%d,\"userId\":%d,\"message\":\"%s\",\"type\":\"%s\",\"isRead\":%b,\"createdAt\":\"%s\"}",
                                n.getId(), n.getUserId(), escape(n.getMessage()), escape(n.getType()), n.isRead(),
                                n.getCreatedAt() != null ? n.getCreatedAt().toString() : ""
                        ));
                    }
                    sb.append("]");
                    sendResponse(exchange, 200, sb.toString());
                } else if ("PUT".equalsIgnoreCase(method)) {
                    String path = exchange.getRequestURI().getPath();
                    String[] parts = path.split("/");
                    int id = Integer.parseInt(parts[3]);
                    service.markNotificationAsRead(id);
                    sendResponse(exchange, 200, "{\"success\":true}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
            }
        }
    }

    static class WardenStatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            try (Connection conn = DatabaseUtil.getConnection()) {
                int totalRooms = 0, occupiedRooms = 0, availableRooms = 0;
                try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) as tot, SUM(CASE WHEN current_occupancy >= capacity THEN 1 ELSE 0 END) as occ FROM Rooms");
                     ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        totalRooms = rs.getInt("tot");
                        occupiedRooms = rs.getInt("occ");
                        availableRooms = totalRooms - occupiedRooms;
                    }
                }

                int activeComplaints = 0, pendingComplaints = 0, activeEmergencies = 0;
                try (PreparedStatement s = conn.prepareStatement("SELECT " +
                        "SUM(CASE WHEN status != 'RESOLVED' THEN 1 ELSE 0 END) as active, " +
                        "SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
                        "SUM(CASE WHEN type = 'Emergency' AND status != 'RESOLVED' THEN 1 ELSE 0 END) as emer " +
                        "FROM Complaints");
                     ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        activeComplaints = rs.getInt("active");
                        pendingComplaints = rs.getInt("pending");
                        activeEmergencies = rs.getInt("emer");
                    }
                }

                String json = String.format(
                        "{\"totalRooms\":%d,\"occupiedRooms\":%d,\"availableRooms\":%d,\"activeComplaints\":%d,\"pendingComplaints\":%d,\"activeEmergencies\":%d}",
                        totalRooms, occupiedRooms, availableRooms, activeComplaints, pendingComplaints, activeEmergencies
                );
                sendResponse(exchange, 200, json);
            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"error\":\"Database error\"}");
            }
        }
    }

    static class EmergencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            User user = resolveUser(exchange);
            HostelService service = new HostelServiceProxy(user);

            String body = readBody(exchange);
            Map<String, String> data = parseSimpleJson(body);

            String roomNo = data.getOrDefault("roomNo", user != null ? user.getRoomNo() : "A-203");
            String description = data.getOrDefault("description", "EMERGENCY: Immediate assistance required in Room " + roomNo);

            Complaint complaint = ComplaintFactory.createComplaint("Emergency", user != null ? user.getId() : 2, roomNo, description, "EMERGENCY");
            Complaint created = service.createComplaint(complaint);

            sendResponse(exchange, 201, complaintToJson(created));
        }
    }

    private static String complaintToJson(Complaint c) {
        return String.format(
                "{\"id\":%d,\"studentId\":%d,\"studentName\":\"%s\",\"roomNo\":\"%s\",\"type\":\"%s\",\"department\":\"%s\",\"description\":\"%s\",\"priority\":\"%s\",\"status\":\"%s\",\"technicianId\":%d,\"technicianName\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                c.getId(), c.getStudentId(), escape(c.getStudentName()), escape(c.getRoomNo()),
                escape(c.getType()), escape(c.getDepartment()), escape(c.getDescription()),
                escape(c.getPriority()), escape(c.getStatus()), c.getTechnicianId(),
                escape(c.getTechnicianName() != null ? c.getTechnicianName() : ""),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : "",
                c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : ""
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
