package com.hostelhub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    
    private static final String URL = "jdbc:sqlite:hostelhub.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Create Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " + // STUDENT, WARDEN
                    "name TEXT NOT NULL, " +
                    "room_no TEXT, " +
                    "contact TEXT, " +
                    "email TEXT" +
                    ")");

            // Create Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Rooms (" +
                    "room_no TEXT PRIMARY KEY, " +
                    "block TEXT NOT NULL, " +
                    "capacity INTEGER NOT NULL, " +
                    "current_occupancy INTEGER DEFAULT 0, " +
                    "status TEXT DEFAULT 'Available'" +
                    ")");

            // Create Technicians Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Technicians (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "department TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'AVAILABLE', " + // AVAILABLE, ASSIGNED
                    "contact TEXT" +
                    ")");

            // Create Complaints Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Complaints (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "room_no TEXT, " +
                    "type TEXT NOT NULL, " + // Maintenance, Electricity, Water, Mess, Emergency
                    "department TEXT, " +
                    "description TEXT NOT NULL, " +
                    "priority TEXT NOT NULL, " + // LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY
                    "status TEXT DEFAULT 'PENDING', " + // PENDING, ASSIGNED, IN_PROGRESS, RESOLVED
                    "technician_id INTEGER, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES Users(id), " +
                    "FOREIGN KEY(technician_id) REFERENCES Technicians(id)" +
                    ")");

            // Create Visitors Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Visitors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "room_no TEXT, " +
                    "visitor_name TEXT NOT NULL, " +
                    "contact TEXT, " +
                    "visit_date TEXT, " +
                    "entry_time TEXT, " +
                    "exit_time TEXT, " +
                    "reason TEXT, " +
                    "status TEXT DEFAULT 'APPROVED', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES Users(id)" +
                    ")");

            // Create Notifications Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Notifications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "type TEXT DEFAULT 'COMPLAINT', " +
                    "is_read INTEGER DEFAULT 0, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(user_id) REFERENCES Users(id)" +
                    ")");

            // Insert default data if Users table is empty
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
            if (rs.next() && rs.getInt(1) == 0) {
                // Add default Warden
                stmt.execute("INSERT INTO Users (username, password, role, name, room_no, contact, email) VALUES " +
                        "('warden', 'warden123', 'WARDEN', 'Dr. K. Sharma (Chief Warden)', 'Admin-01', '+91 98765 43210', 'warden@hostelhub.edu')");
                
                // Add default Students (Student ID 2 will be Aarav Patel)
                stmt.execute("INSERT INTO Users (username, password, role, name, room_no, contact, email) VALUES " +
                        "('student1', 'student123', 'STUDENT', 'Aarav Patel', 'A-203', '+91 90118 48854', 'aarav.patel@hostelhub.edu')");
                stmt.execute("INSERT INTO Users (username, password, role, name, room_no, contact, email) VALUES " +
                        "('ria', 'student123', 'STUDENT', 'Ria Sharma', 'A-203', '+91 91234 56789', 'ria.sharma@hostelhub.edu')");
                stmt.execute("INSERT INTO Users (username, password, role, name, room_no, contact, email) VALUES " +
                        "('ananya', 'student123', 'STUDENT', 'Ananya Verma', 'B-101', '+91 92345 67890', 'ananya.v@hostelhub.edu')");
                
                // Add default Rooms
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('A-201', 'Block A', 2, 2, 'Occupied')");
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('A-202', 'Block A', 2, 1, 'Available')");
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('A-203', 'Block A', 2, 2, 'Occupied')");
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('B-101', 'Block B', 1, 1, 'Occupied')");
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('B-102', 'Block B', 3, 2, 'Available')");
                stmt.execute("INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES ('C-205', 'Block C', 2, 1, 'Available')");
                
                // Add default Technicians across departments
                stmt.execute("INSERT INTO Technicians (name, department, status, contact) VALUES ('Raj', 'Electrical', 'AVAILABLE', '+91 94001 11223')");
                stmt.execute("INSERT INTO Technicians (name, department, status, contact) VALUES ('Ramesh', 'Plumbing', 'AVAILABLE', '+91 94002 22334')");
                stmt.execute("INSERT INTO Technicians (name, department, status, contact) VALUES ('Amit', 'Mess', 'AVAILABLE', '+91 94003 33445')");
                stmt.execute("INSERT INTO Technicians (name, department, status, contact) VALUES ('Suresh', 'Maintenance', 'AVAILABLE', '+91 94004 44556')");
                stmt.execute("INSERT INTO Technicians (name, department, status, contact) VALUES ('Vikram', 'Security', 'AVAILABLE', '+91 94005 55667')");

                // Add default seed complaints
                stmt.execute("INSERT INTO Complaints (student_id, room_no, type, department, description, priority, status, technician_id) VALUES " +
                        "(4, 'B-101', 'Water', 'Plumbing', 'Water leakage from washroom pipe', 'MEDIUM', 'PENDING', NULL)");
                stmt.execute("INSERT INTO Complaints (student_id, room_no, type, department, description, priority, status, technician_id) VALUES " +
                        "(3, 'C-205', 'Mess', 'Mess', 'Quality of dinner food was cold', 'LOW', 'ASSIGNED', 3)");

                // Add default visitor entry
                stmt.execute("INSERT INTO Visitors (student_id, room_no, visitor_name, contact, visit_date, entry_time, exit_time, reason, status) VALUES " +
                        "(2, 'A-203', 'Karan Patel', '+91 98221 00000', '2026-09-15', '10:30 AM', '05:00 PM', 'Family Visit', 'APPROVED')");

                // Add initial notifications for Aarav (id 2) and Warden (id 1)
                stmt.execute("INSERT INTO Notifications (user_id, message, type) VALUES (2, 'Welcome to HostelHub! Your room is confirmed as A-203 (Block A).', 'GENERAL')");
                stmt.execute("INSERT INTO Notifications (user_id, message, type) VALUES (2, 'Your visitor entry for Karan Patel is approved', 'VISITOR')");
                stmt.execute("INSERT INTO Notifications (user_id, message, type) VALUES (2, 'Maintenance complaint #11 resolved', 'COMPLAINT')");
                stmt.execute("INSERT INTO Notifications (user_id, message, type) VALUES (1, 'HostelHub Central System operational. All services online.', 'GENERAL')");
            }

            System.out.println("Database initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
