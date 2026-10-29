package com.hostelhub.singleton;

import com.hostelhub.chain.*;
import com.hostelhub.factory.Complaint;
import com.hostelhub.observer.ComplaintManager;
import com.hostelhub.observer.UserNotifier;
import com.hostelhub.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import com.hostelhub.prototype.ComplaintPrototypeRegistry;
import com.hostelhub.strategy.AvailabilityBasedAssignmentStrategy;
import com.hostelhub.strategy.DepartmentBasedAssignmentStrategy;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;

public class HostelManagementSystem {
    private static HostelManagementSystem instance;
    private ComplaintHandler complaintChain;
    private ComplaintManager complaintManager;

    // Strategy Pattern: Configurable technician assignment strategy
    private TechnicianAssignmentStrategy technicianAssignmentStrategy;

    // Prototype Pattern: Central registry of complaint prototypes
    private final ComplaintPrototypeRegistry prototypeRegistry;

    private HostelManagementSystem() {
        this.technicianAssignmentStrategy = new DepartmentBasedAssignmentStrategy();
        this.prototypeRegistry = new ComplaintPrototypeRegistry();

        // Initialize Chain of Responsibility
        ComplaintHandler emergency = new EmergencyHandler();
        ComplaintHandler maintenance = new MaintenanceHandler();
        ComplaintHandler electrical = new ElectricalHandler();
        ComplaintHandler plumbing = new PlumbingHandler();
        ComplaintHandler mess = new MessHandler();

        // Build chain: Emergency -> Maintenance -> Electrical -> Plumbing -> Mess
        emergency.setNextHandler(maintenance);
        maintenance.setNextHandler(electrical);
        electrical.setNextHandler(plumbing);
        plumbing.setNextHandler(mess);
        
        // Propagate active strategy to chain
        emergency.setAssignmentStrategy(this.technicianAssignmentStrategy);

        this.complaintChain = emergency;

        // Initialize Observer Subject and attach observer
        this.complaintManager = new ComplaintManager();
        this.complaintManager.addObserver(new UserNotifier());
    }

    public static synchronized HostelManagementSystem getInstance() {
        if (instance == null) {
            instance = new HostelManagementSystem();
        }
        return instance;
    }

    public TechnicianAssignmentStrategy getTechnicianAssignmentStrategy() {
        return technicianAssignmentStrategy;
    }

    public void setTechnicianAssignmentStrategy(TechnicianAssignmentStrategy strategy) {
        if (strategy != null) {
            this.technicianAssignmentStrategy = strategy;
            if (this.complaintChain != null) {
                this.complaintChain.setAssignmentStrategy(strategy);
            }
            System.out.println("HostelManagementSystem: Technician assignment strategy updated to " + strategy.getStrategyName());
        }
    }

    public ComplaintPrototypeRegistry getPrototypeRegistry() {
        return prototypeRegistry;
    }

    public ComplaintManager getComplaintManager() {
        return complaintManager;
    }

    public Complaint processNewComplaint(Complaint complaint) {
        System.out.println("HostelManagementSystem: Received new complaint: " + complaint.getType() + 
                " for Room " + complaint.getRoomNo() + " from Student ID " + complaint.getStudentId());
        
        // 1. Let the Chain of Responsibility route the complaint and assign technician
        complaintChain.processComplaint(complaint);
        
        // 2. Save ONE complaint record to Database (no duplicates)
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO Complaints (student_id, room_no, type, department, description, priority, status, technician_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, complaint.getStudentId());
                stmt.setString(2, complaint.getRoomNo() != null ? complaint.getRoomNo() : "");
                stmt.setString(3, complaint.getType());
                stmt.setString(4, complaint.getDepartment() != null ? complaint.getDepartment() : "General");
                stmt.setString(5, complaint.getDescription());
                stmt.setString(6, complaint.getPriority());
                stmt.setString(7, complaint.getStatus());
                
                if (complaint.getTechnicianId() > 0) {
                    stmt.setInt(8, complaint.getTechnicianId());
                } else {
                    stmt.setNull(8, java.sql.Types.INTEGER);
                }
                
                stmt.executeUpdate();
                
                // Retrieve generated complaint ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int complaintId = rs.getInt(1);
                    complaint.setId(complaintId);
                    
                    // Fetch technician name if assigned
                    if (complaint.getTechnicianId() > 0 && complaint.getTechnicianName() == null) {
                        String techSql = "SELECT name FROM Technicians WHERE id = ?";
                        try (PreparedStatement techStmt = conn.prepareStatement(techSql)) {
                            techStmt.setInt(1, complaint.getTechnicianId());
                            ResultSet techRs = techStmt.executeQuery();
                            if (techRs.next()) {
                                complaint.setTechnicianName(techRs.getString("name"));
                            }
                        }
                    }
                    
                    // 3. Notify Observers (Student and Warden)
                    String notifyMessage = "Complaint #" + complaintId + " (" + complaint.getType() + " - " + 
                            complaint.getDepartment() + ") is now " + complaint.getStatus() + 
                            (complaint.getTechnicianName() != null ? ": Technician " + complaint.getTechnicianName() + " assigned" : "");
                    complaintManager.onComplaintStatusChanged(complaintId, complaint.getStatus(), complaint.getStudentId(), notifyMessage);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return complaint;
    }
}
