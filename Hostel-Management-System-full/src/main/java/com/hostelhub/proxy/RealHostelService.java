package com.hostelhub.proxy;

import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Notification;
import com.hostelhub.model.Room;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.model.Visitor;
import com.hostelhub.singleton.HostelManagementSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RealHostelService implements HostelService {
    private User currentUser;

    public RealHostelService() {}

    public RealHostelService(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public User viewProfile() {
        return currentUser;
    }

    @Override
    public Complaint createComplaint(Complaint complaint) {
        System.out.println("RealHostelService: Routing complaint to Central HostelHub System...");
        return HostelManagementSystem.getInstance().processNewComplaint(complaint);
    }

    @Override
    public List<Complaint> getComplaints() {
        List<Complaint> list = new ArrayList<>();
        boolean isWarden = currentUser != null && "WARDEN".equalsIgnoreCase(currentUser.getRole());
        
        String sql = "SELECT c.*, u.name as student_name, t.name as technician_name " +
                     "FROM Complaints c " +
                     "JOIN Users u ON c.student_id = u.id " +
                     "LEFT JOIN Technicians t ON c.technician_id = t.id ";
        
        if (!isWarden && currentUser != null) {
            sql += "WHERE c.student_id = ? ORDER BY c.id DESC";
        } else {
            sql += "ORDER BY c.id DESC";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (!isWarden && currentUser != null) {
                stmt.setInt(1, currentUser.getId());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Complaint c = new Complaint();
                c.setId(rs.getInt("id"));
                c.setStudentId(rs.getInt("student_id"));
                c.setStudentName(rs.getString("student_name"));
                c.setRoomNo(rs.getString("room_no"));
                c.setType(rs.getString("type"));
                c.setDepartment(rs.getString("department"));
                c.setDescription(rs.getString("description"));
                c.setPriority(rs.getString("priority"));
                c.setStatus(rs.getString("status"));
                c.setTechnicianId(rs.getInt("technician_id"));
                c.setTechnicianName(rs.getString("technician_name"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Complaint getComplaintById(int id) {
        String sql = "SELECT c.*, u.name as student_name, t.name as technician_name " +
                     "FROM Complaints c " +
                     "JOIN Users u ON c.student_id = u.id " +
                     "LEFT JOIN Technicians t ON c.technician_id = t.id " +
                     "WHERE c.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Complaint c = new Complaint();
                c.setId(rs.getInt("id"));
                c.setStudentId(rs.getInt("student_id"));
                c.setStudentName(rs.getString("student_name"));
                c.setRoomNo(rs.getString("room_no"));
                c.setType(rs.getString("type"));
                c.setDepartment(rs.getString("department"));
                c.setDescription(rs.getString("description"));
                c.setPriority(rs.getString("priority"));
                c.setStatus(rs.getString("status"));
                c.setTechnicianId(rs.getInt("technician_id"));
                c.setTechnicianName(rs.getString("technician_name"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setUpdatedAt(rs.getTimestamp("updated_at"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateComplaintStatus(int complaintId, String status) {
        System.out.println("RealHostelService: Updating complaint #" + complaintId + " status to " + status);
        int studentId = 0;
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Get studentId first
            String findSql = "SELECT student_id FROM Complaints WHERE id = ?";
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setInt(1, complaintId);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                }
            }

            // Update status
            String sql = "UPDATE Complaints SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setInt(2, complaintId);
                stmt.executeUpdate();
            }

            // If resolved, free up the technician
            if ("RESOLVED".equalsIgnoreCase(status)) {
                String freeTechSql = "UPDATE Technicians SET status = 'AVAILABLE' WHERE id = (SELECT technician_id FROM Complaints WHERE id = ?)";
                try (PreparedStatement freeStmt = conn.prepareStatement(freeTechSql)) {
                    freeStmt.setInt(1, complaintId);
                    freeStmt.executeUpdate();
                }
            }

            // Trigger Observer notification to student and warden
            HostelManagementSystem.getInstance().getComplaintManager().onComplaintStatusChanged(
                    complaintId, status, studentId, "Complaint #" + complaintId + " status updated to: " + status
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void assignTechnician(int complaintId, int technicianId) {
        System.out.println("RealHostelService: Assigning Technician " + technicianId + " to Complaint #" + complaintId);
        int studentId = 0;
        String techName = "Technician";
        String department = "Service";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Get tech details
            String techSql = "SELECT name, department FROM Technicians WHERE id = ?";
            try (PreparedStatement tStmt = conn.prepareStatement(techSql)) {
                tStmt.setInt(1, technicianId);
                ResultSet rs = tStmt.executeQuery();
                if (rs.next()) {
                    techName = rs.getString("name");
                    department = rs.getString("department");
                }
            }

            // Get student id
            String cSql = "SELECT student_id FROM Complaints WHERE id = ?";
            try (PreparedStatement cStmt = conn.prepareStatement(cSql)) {
                cStmt.setInt(1, complaintId);
                ResultSet rs = cStmt.executeQuery();
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                }
            }

            // Update Complaint
            String sql = "UPDATE Complaints SET technician_id = ?, department = ?, status = 'ASSIGNED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, technicianId);
                stmt.setString(2, department);
                stmt.setInt(3, complaintId);
                stmt.executeUpdate();
            }

            // Mark technician as ASSIGNED
            String updateTechSql = "UPDATE Technicians SET status = 'ASSIGNED' WHERE id = ?";
            try (PreparedStatement uStmt = conn.prepareStatement(updateTechSql)) {
                uStmt.setInt(1, technicianId);
                uStmt.executeUpdate();
            }

            // Trigger Observer notification
            HostelManagementSystem.getInstance().getComplaintManager().onComplaintStatusChanged(
                    complaintId, "ASSIGNED", studentId, "Complaint #" + complaintId + ": " + techName + " (" + department + ") assigned"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Room> getRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM Rooms ORDER BY room_no ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getString("room_no"),
                        rs.getString("block"),
                        rs.getInt("capacity"),
                        rs.getInt("current_occupancy"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    @Override
    public void allocateRoom(int studentId, String roomNo) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Update student room_no
            String userSql = "UPDATE Users SET room_no = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setString(1, roomNo);
                stmt.setInt(2, studentId);
                stmt.executeUpdate();
            }

            // Update room occupancy
            String countSql = "SELECT COUNT(*) FROM Users WHERE room_no = ?";
            int count = 0;
            try (PreparedStatement cStmt = conn.prepareStatement(countSql)) {
                cStmt.setString(1, roomNo);
                ResultSet rs = cStmt.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            }

            String roomSql = "UPDATE Rooms SET current_occupancy = ?, status = CASE WHEN current_occupancy >= capacity THEN 'Occupied' ELSE 'Available' END WHERE room_no = ?";
            try (PreparedStatement rStmt = conn.prepareStatement(roomSql)) {
                rStmt.setInt(1, count);
                rStmt.setString(2, roomNo);
                rStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addRoom(Room room) {
        String sql = "INSERT INTO Rooms (room_no, block, capacity, current_occupancy, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomNo());
            stmt.setString(2, room.getBlock());
            stmt.setInt(3, room.getCapacity());
            stmt.setInt(4, room.getCurrentOccupancy());
            stmt.setString(5, room.getStatus() != null ? room.getStatus() : "Available");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Visitor> getVisitors() {
        List<Visitor> visitors = new ArrayList<>();
        boolean isWarden = currentUser != null && "WARDEN".equalsIgnoreCase(currentUser.getRole());
        String sql = "SELECT v.*, u.name as student_name FROM Visitors v JOIN Users u ON v.student_id = u.id ";
        if (!isWarden && currentUser != null) {
            sql += "WHERE v.student_id = ? ORDER BY v.id DESC";
        } else {
            sql += "ORDER BY v.id DESC";
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (!isWarden && currentUser != null) {
                stmt.setInt(1, currentUser.getId());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Visitor v = new Visitor();
                v.setId(rs.getInt("id"));
                v.setStudentId(rs.getInt("student_id"));
                v.setStudentName(rs.getString("student_name"));
                v.setRoomNo(rs.getString("room_no"));
                v.setVisitorName(rs.getString("visitor_name"));
                v.setContact(rs.getString("contact"));
                v.setVisitDate(rs.getString("visit_date"));
                v.setEntryTime(rs.getString("entry_time"));
                v.setExitTime(rs.getString("exit_time"));
                v.setReason(rs.getString("reason"));
                v.setStatus(rs.getString("status"));
                visitors.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visitors;
    }

    @Override
    public void registerVisitor(Visitor visitor) {
        String sql = "INSERT INTO Visitors (student_id, room_no, visitor_name, contact, visit_date, entry_time, exit_time, reason, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, visitor.getStudentId());
            stmt.setString(2, visitor.getRoomNo() != null ? visitor.getRoomNo() : "");
            stmt.setString(3, visitor.getVisitorName());
            stmt.setString(4, visitor.getContact());
            stmt.setString(5, visitor.getVisitDate());
            stmt.setString(6, visitor.getEntryTime());
            stmt.setString(7, visitor.getExitTime());
            stmt.setString(8, visitor.getReason());
            stmt.setString(9, visitor.getStatus() != null ? visitor.getStatus() : "APPROVED");
            stmt.executeUpdate();

            // Notify student
            String notifySql = "INSERT INTO Notifications (user_id, message, type) VALUES (?, ?, 'VISITOR')";
            try (PreparedStatement nStmt = conn.prepareStatement(notifySql)) {
                nStmt.setInt(1, visitor.getStudentId());
                nStmt.setString(2, "Visitor entry registered for " + visitor.getVisitorName() + " on " + visitor.getVisitDate());
                nStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateVisitorStatus(int visitorId, String status) {
        String sql = "UPDATE Visitors SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, visitorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Technician> getTechnicians() {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM Technicians ORDER BY id ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    @Override
    public void addTechnician(Technician technician) {
        String sql = "INSERT INTO Technicians (name, department, status, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, technician.getName());
            stmt.setString(2, technician.getDepartment());
            stmt.setString(3, technician.getStatus() != null ? technician.getStatus() : "AVAILABLE");
            stmt.setString(4, technician.getContact());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTechnicianStatus(int technicianId, String status) {
        String sql = "UPDATE Technicians SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, technicianId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Notification> getNotifications() {
        List<Notification> list = new ArrayList<>();
        if (currentUser == null) return list;
        String sql = "SELECT * FROM Notifications WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getString("type"),
                        rs.getInt("is_read") == 1,
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void markNotificationAsRead(int notificationId) {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
