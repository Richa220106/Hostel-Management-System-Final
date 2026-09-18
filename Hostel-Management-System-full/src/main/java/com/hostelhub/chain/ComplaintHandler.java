package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;
import com.hostelhub.db.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ComplaintHandler {
    protected ComplaintHandler nextHandler;

    public void setNextHandler(ComplaintHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void processComplaint(Complaint complaint) {
        if (canHandle(complaint)) {
            handle(complaint);
        } else if (nextHandler != null) {
            nextHandler.processComplaint(complaint);
        } else {
            System.out.println("No handler found for complaint type: " + complaint.getType());
            complaint.setDepartment("General");
            complaint.setStatus("PENDING");
        }
    }

    protected abstract boolean canHandle(Complaint complaint);
    protected abstract void handle(Complaint complaint);
    
    // Helper method to assign technician from a department
    protected void assignTechnician(Complaint complaint, String department) {
        complaint.setDepartment(department);
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Find available technician in department
            String findTechSql = "SELECT id, name FROM Technicians WHERE department = ? AND status = 'AVAILABLE' LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(findTechSql)) {
                stmt.setString(1, department);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int techId = rs.getInt("id");
                    String techName = rs.getString("name");
                    
                    complaint.setTechnicianId(techId);
                    complaint.setTechnicianName(techName);
                    complaint.setStatus("ASSIGNED");
                    
                    // Mark technician status as ASSIGNED
                    String updateTechSql = "UPDATE Technicians SET status = 'ASSIGNED' WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateTechSql)) {
                        updateStmt.setInt(1, techId);
                        updateStmt.executeUpdate();
                    }
                    System.out.println("Assigned technician " + techName + " (ID " + techId + ") from " + department + " to complaint.");
                } else {
                    // Fallback to any technician in department
                    String fallbackSql = "SELECT id, name FROM Technicians WHERE department = ? LIMIT 1";
                    try (PreparedStatement fbStmt = conn.prepareStatement(fallbackSql)) {
                        fbStmt.setString(1, department);
                        ResultSet fbRs = fbStmt.executeQuery();
                        if (fbRs.next()) {
                            int techId = fbRs.getInt("id");
                            String techName = fbRs.getString("name");
                            complaint.setTechnicianId(techId);
                            complaint.setTechnicianName(techName);
                            complaint.setStatus("ASSIGNED");
                            System.out.println("Assigned active technician " + techName + " (ID " + techId + ") from " + department + ".");
                        } else {
                            System.out.println("No technicians configured for " + department + " department.");
                            complaint.setStatus("PENDING");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
