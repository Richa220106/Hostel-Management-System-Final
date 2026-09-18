package com.hostelhub;

import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.factory.ComplaintFactory;
import com.hostelhub.factory.MaintenanceComplaint;
import com.hostelhub.model.Notification;
import com.hostelhub.model.User;
import com.hostelhub.proxy.HostelService;
import com.hostelhub.proxy.HostelServiceProxy;
import com.hostelhub.singleton.HostelManagementSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HostelHubWorkflowTest {

    @BeforeAll
    public static void setup() {
        DatabaseUtil.initializeDatabase();
    }

    @Test
    public void testSingletonPattern() {
        HostelManagementSystem instance1 = HostelManagementSystem.getInstance();
        HostelManagementSystem instance2 = HostelManagementSystem.getInstance();
        assertSame(instance1, instance2, "HostelManagementSystem must be a singleton");
    }

    @Test
    public void testFactoryMethodPattern() {
        Complaint complaint = ComplaintFactory.createComplaint("Maintenance", 2, "A-203", "AC is not working in Room A-203", "HIGH");
        assertNotNull(complaint);
        assertTrue(complaint instanceof MaintenanceComplaint, "Factory should instantiate MaintenanceComplaint");
        assertEquals("Maintenance", complaint.getType());
        assertEquals("A-203", complaint.getRoomNo());
    }

    @Test
    public void testProxyRoleBasedAccessControl() {
        User student = new User(2, "student1", "student123", "STUDENT", "Aarav Patel", "A-203", "+91 90118 48854", "aarav.patel@hostelhub.edu");
        HostelService studentProxy = new HostelServiceProxy(student);

        // Student can view own profile
        assertNotNull(studentProxy.viewProfile());

        // Student attempting to update status should throw SecurityException
        assertThrows(SecurityException.class, () -> {
            studentProxy.updateComplaintStatus(1, "RESOLVED");
        }, "Student should not be allowed to update complaint status");

        // Student attempting to assign technician should throw SecurityException
        assertThrows(SecurityException.class, () -> {
            studentProxy.assignTechnician(1, 1);
        }, "Student should not be allowed to manually assign technicians");

        // Student attempting to access technician list should throw SecurityException
        assertThrows(SecurityException.class, () -> {
            studentProxy.getTechnicians();
        }, "Student should not be allowed to view technician admin list");
    }

    @Test
    public void testEndToEndACComplaintWorkflow() throws Exception {
        // User Aarav Patel reports: "AC is not working in Room A-203"
        User student = new User(2, "student1", "student123", "STUDENT", "Aarav Patel", "A-203", "+91 90118 48854", "aarav.patel@hostelhub.edu");
        HostelService studentProxy = new HostelServiceProxy(student);

        // Factory creates complaint
        Complaint inputComplaint = ComplaintFactory.createComplaint("Maintenance", student.getId(), "A-203", "AC is not working in Room A-203", "HIGH");
        
        // Proxy routes through Singleton central system -> Chain of Responsibility -> MaintenanceHandler detects AC -> routes to ElectricalHandler -> Raj assigned -> DB stored -> Observer notifies
        Complaint processed = studentProxy.createComplaint(inputComplaint);

        assertNotNull(processed);
        assertTrue(processed.getId() > 0, "Complaint should have generated database ID");
        assertEquals("Electrical", processed.getDepartment(), "Chain should have identified Electrical Department");
        assertEquals("ASSIGNED", processed.getStatus(), "Status should be ASSIGNED");
        assertTrue(processed.getTechnicianId() > 0, "Technician should be assigned");

        // Verify single record in database (no duplicates)
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Complaints WHERE id = ?")) {
            stmt.setInt(1, processed.getId());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Complaint must exist in database");
            assertEquals("A-203", rs.getString("room_no"));
            assertEquals("Electrical", rs.getString("department"));
            assertEquals("ASSIGNED", rs.getString("status"));
            assertFalse(rs.next(), "No duplicate complaint records");
        }

        // Verify Observer notification was stored in database
        List<Notification> notifications = studentProxy.getNotifications();
        assertFalse(notifications.isEmpty(), "Student must receive notification from Observer");
        boolean foundNotification = notifications.stream().anyMatch(n -> n.getMessage().contains("Complaint #" + processed.getId()));
        assertTrue(foundNotification, "Observer should have generated notification for the complaint");

        // Test Warden view and status update
        User warden = new User(1, "warden", "warden123", "WARDEN", "Chief Warden");
        HostelService wardenProxy = new HostelServiceProxy(warden);

        // Warden can see all complaints
        List<Complaint> allComplaints = wardenProxy.getComplaints();
        assertTrue(allComplaints.stream().anyMatch(c -> c.getId() == processed.getId()), "Warden dashboard must reflect the same complaint");

        // Warden updates status to IN_PROGRESS
        wardenProxy.updateComplaintStatus(processed.getId(), "IN_PROGRESS");
        Complaint updated = wardenProxy.getComplaintById(processed.getId());
        assertEquals("IN_PROGRESS", updated.getStatus(), "Status should be IN_PROGRESS");

        // Warden updates status to RESOLVED
        wardenProxy.updateComplaintStatus(processed.getId(), "RESOLVED");
        Complaint resolved = wardenProxy.getComplaintById(processed.getId());
        assertEquals("RESOLVED", resolved.getStatus(), "Status should be RESOLVED");

        System.out.println("End-to-End AC Complaint Workflow passed successfully with all 5 Design Patterns!");
    }
}
