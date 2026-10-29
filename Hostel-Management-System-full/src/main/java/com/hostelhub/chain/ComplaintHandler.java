package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import com.hostelhub.singleton.HostelManagementSystem;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;
import com.hostelhub.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class ComplaintHandler {
    protected ComplaintHandler nextHandler;
    protected TechnicianAssignmentStrategy assignmentStrategy;

    public void setNextHandler(ComplaintHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    /**
     * Strategy Pattern integration:
     * Sets the technician assignment strategy for this handler and propagates it down the chain.
     */
    public void setAssignmentStrategy(TechnicianAssignmentStrategy strategy) {
        this.assignmentStrategy = strategy;
        if (nextHandler != null) {
            nextHandler.setAssignmentStrategy(strategy);
        }
    }

    /**
     * Resolves the active technician assignment strategy, defaulting to the central system's strategy.
     */
    public TechnicianAssignmentStrategy getAssignmentStrategy() {
        if (this.assignmentStrategy != null) {
            return this.assignmentStrategy;
        }
        return HostelManagementSystem.getInstance().getTechnicianAssignmentStrategy();
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
    
    /**
     * Strategy Pattern integration:
     * Dispatches candidate technicians to the configured TechnicianAssignmentStrategy.
     * If a technician is selected, updates the technician status and sets complaint to ASSIGNED.
     * If no suitable technician is available, sets status to PENDING with no technician.
     */
    protected void assignTechnician(Complaint complaint, String department) {
        complaint.setDepartment(department);

        List<Technician> candidates = fetchCandidateTechnicians(department);
        TechnicianAssignmentStrategy strategy = getAssignmentStrategy();

        Technician selected = strategy.assignTechnician(complaint, candidates);

        if (selected != null) {
            complaint.setTechnicianId(selected.getId());
            complaint.setTechnicianName(selected.getName());
            complaint.setStatus("ASSIGNED");

            // Mark technician as ASSIGNED in database
            updateTechnicianStatusInDb(selected.getId(), "ASSIGNED");

            System.out.println("Assigned technician " + selected.getName() + " (ID " + selected.getId() +
                    ") from " + department + " to complaint using " + strategy.getStrategyName() + ".");
        } else {
            // Case 4: No suitable technician available
            complaint.setTechnicianId(0);
            complaint.setTechnicianName(null);
            complaint.setStatus("PENDING");
            System.out.println("No suitable technician available for " + department +
                    " under strategy " + strategy.getStrategyName() + ". Complaint remains PENDING.");
        }
    }

    private List<Technician> fetchCandidateTechnicians(String department) {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM Technicians WHERE department = ? ORDER BY id ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Technician(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("status"),
                        rs.getString("contact")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void updateTechnicianStatusInDb(int techId, String status) {
        String sql = "UPDATE Technicians SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, techId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
