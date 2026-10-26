package com.hostelhub;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.memento.ComplaintHistory;
import com.hostelhub.memento.ComplaintMemento;
import com.hostelhub.model.Notification;
import com.hostelhub.model.User;
import com.hostelhub.proxy.HostelService;
import com.hostelhub.proxy.HostelServiceProxy;
import com.hostelhub.proxy.RealHostelService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComplaintBuilderAndMementoTest {

    private User wardenUser;
    private User studentUser;
    private RealHostelService realService;
    private HostelService wardenProxy;
    private HostelService studentProxy;

    @BeforeAll
    public static void initDatabase() {
        DatabaseUtil.initializeDatabase();
    }

    @BeforeEach
    public void setUp() {
        wardenUser = new User(1, "warden", "warden123", "WARDEN", "Dr. K. Sharma (Chief Warden)");
        studentUser = new User(2, "student1", "student123", "STUDENT", "Aarav Patel", "A-203", "+91 90118 48854", "aarav.patel@hostelhub.edu");

        realService = new RealHostelService(wardenUser);
        wardenProxy = new HostelServiceProxy(wardenUser);
        studentProxy = new HostelServiceProxy(studentUser);
    }

    // ==========================================
    // 1. ComplaintBuilder Tests
    // ==========================================

    @Test
    @DisplayName("ComplaintBuilder should construct a Complaint with all custom attributes")
    public void testComplaintBuilderAllFields() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Complaint complaint = ComplaintBuilder.builder()
                .id(101)
                .studentId(2)
                .studentName("Aarav Patel")
                .roomNo("A-203")
                .type("Electricity")
                .department("Electrical")
                .description("Ceiling fan regulator sparking")
                .priority("HIGH")
                .status("PENDING")
                .technicianId(1)
                .technicianName("Raj")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertNotNull(complaint, "Built complaint must not be null");
        assertEquals(101, complaint.getId());
        assertEquals(2, complaint.getStudentId());
        assertEquals("Aarav Patel", complaint.getStudentName());
        assertEquals("A-203", complaint.getRoomNo());
        assertEquals("Electricity", complaint.getType());
        assertEquals("Electrical", complaint.getDepartment());
        assertEquals("Ceiling fan regulator sparking", complaint.getDescription());
        assertEquals("HIGH", complaint.getPriority());
        assertEquals("PENDING", complaint.getStatus());
        assertEquals(1, complaint.getTechnicianId());
        assertEquals("Raj", complaint.getTechnicianName());
        assertEquals(now, complaint.getCreatedAt());
        assertEquals(now, complaint.getUpdatedAt());
    }

    @Test
    @DisplayName("ComplaintBuilder defaults should provide sane initial state")
    public void testComplaintBuilderDefaults() {
        Complaint complaint = new ComplaintBuilder()
                .studentId(2)
                .description("General cleaning needed")
                .build();

        assertEquals(2, complaint.getStudentId());
        assertEquals("General cleaning needed", complaint.getDescription());
        assertEquals("PENDING", complaint.getStatus(), "Default status should be PENDING");
        assertEquals("Maintenance", complaint.getType(), "Default type should be Maintenance");
        assertEquals("MEDIUM", complaint.getPriority(), "Default priority should be MEDIUM");
    }

    @Test
    @DisplayName("Complaint.builder() static method should instantiate ComplaintBuilder")
    public void testComplaintStaticBuilder() {
        Complaint complaint = Complaint.builder()
                .type("Water")
                .roomNo("B-102")
                .description("Tap leaking in washroom")
                .priority("MEDIUM")
                .build();

        assertNotNull(complaint);
        assertEquals("Water", complaint.getType());
        assertEquals("B-102", complaint.getRoomNo());
        assertEquals("Tap leaking in washroom", complaint.getDescription());
    }

    // ==========================================
    // 2. ComplaintMemento Originator Tests
    // ==========================================

    @Test
    @DisplayName("Complaint should create an accurate memento snapshot and restore from it")
    public void testComplaintSaveAndRestoreState() {
        Complaint complaint = Complaint.builder()
                .id(42)
                .studentId(2)
                .roomNo("A-203")
                .type("Maintenance")
                .department("Electrical")
                .description("AC issue")
                .priority("HIGH")
                .status("ASSIGNED")
                .technicianId(1)
                .technicianName("Raj")
                .build();

        // Save initial snapshot
        ComplaintMemento memento = complaint.saveState();
        assertNotNull(memento);
        assertEquals(42, memento.getComplaintId());
        assertEquals("ASSIGNED", memento.getStatus());
        assertEquals(1, memento.getTechnicianId());
        assertEquals("Raj", memento.getTechnicianName());

        // Mutate state to IN_PROGRESS
        complaint.setStatus("IN_PROGRESS");
        assertEquals("IN_PROGRESS", complaint.getStatus());

        // Mutate state to RESOLVED
        complaint.setStatus("RESOLVED");
        assertEquals("RESOLVED", complaint.getStatus());

        // Restore back to original snapshot
        complaint.restoreState(memento);
        assertEquals("ASSIGNED", complaint.getStatus(), "Status should be restored to ASSIGNED");
        assertEquals(1, complaint.getTechnicianId());
        assertEquals("Raj", complaint.getTechnicianName());
        assertEquals("Electrical", complaint.getDepartment());
    }

    // ==========================================
    // 3. ComplaintHistory Caretaker Tests
    // ==========================================

    @Test
    @DisplayName("ComplaintHistory should manage multi-step undo operations per complaint")
    public void testComplaintHistoryMultiStepUndo() {
        ComplaintHistory history = new ComplaintHistory();

        Complaint complaint = Complaint.builder()
                .id(88)
                .status("PENDING")
                .department("General")
                .build();

        // 1. Snapshot PENDING before moving to ASSIGNED
        history.saveState(complaint);

        // 2. State changes to ASSIGNED
        complaint.setStatus("ASSIGNED");
        complaint.setTechnicianId(1);
        complaint.setTechnicianName("Raj");
        complaint.setDepartment("Electrical");
        history.saveState(complaint);

        // 3. State changes to IN_PROGRESS
        complaint.setStatus("IN_PROGRESS");
        history.saveState(complaint);

        // 4. State changes to RESOLVED
        complaint.setStatus("RESOLVED");

        assertEquals(3, history.getHistoryCount(88), "Should have 3 saved states in history");
        assertTrue(history.hasHistory(88));

        // Undo 1: Revert from RESOLVED -> IN_PROGRESS
        ComplaintMemento memento1 = history.pop(88);
        assertNotNull(memento1);
        complaint.restoreState(memento1);
        assertEquals("IN_PROGRESS", complaint.getStatus());

        // Undo 2: Revert from IN_PROGRESS -> ASSIGNED
        ComplaintMemento memento2 = history.pop(88);
        assertNotNull(memento2);
        complaint.restoreState(memento2);
        assertEquals("ASSIGNED", complaint.getStatus());
        assertEquals(1, complaint.getTechnicianId());

        // Undo 3: Revert from ASSIGNED -> PENDING
        ComplaintMemento memento3 = history.pop(88);
        assertNotNull(memento3);
        complaint.restoreState(memento3);
        assertEquals("PENDING", complaint.getStatus());

        // No more history
        assertFalse(history.hasHistory(88));
        assertNull(history.pop(88));
    }

    @Test
    @DisplayName("ComplaintHistory should maintain isolation between different complaints")
    public void testComplaintHistoryIsolation() {
        ComplaintHistory history = new ComplaintHistory();

        Complaint complaintA = Complaint.builder().id(10).status("PENDING").build();
        Complaint complaintB = Complaint.builder().id(20).status("IN_PROGRESS").build();

        history.saveState(complaintA);
        history.saveState(complaintB);

        assertEquals(1, history.getHistoryCount(10));
        assertEquals(1, history.getHistoryCount(20));

        ComplaintMemento popA = history.pop(10);
        assertNotNull(popA);
        assertEquals(10, popA.getComplaintId());
        assertEquals("PENDING", popA.getStatus());

        // Complaint B still has its state
        assertTrue(history.hasHistory(20));
        assertEquals(1, history.getHistoryCount(20));
        assertEquals("IN_PROGRESS", history.peek(20).getStatus());
    }

    // ==========================================
    // 4. Integration with RealHostelService & Proxy
    // ==========================================

    @Test
    @DisplayName("End-to-end integration: Create via builder, update status, and restore via memento")
    public void testCreateViaBuilderAndUpdateAndRestore() throws Exception {
        // Step 1: Create complaint using ComplaintBuilder via Proxy
        Complaint input = Complaint.builder()
                .type("Electricity")
                .studentId(studentUser.getId())
                .roomNo("A-203")
                .description("Study table lamp socket has loose contact")
                .priority("HIGH")
                .build();

        Complaint created = studentProxy.createComplaint(input);
        assertNotNull(created);
        int complaintId = created.getId();
        assertTrue(complaintId > 0, "Generated database id must be > 0");

        String initialStatus = created.getStatus();

        // Step 2: Warden updates status to IN_PROGRESS
        wardenProxy.updateComplaintStatus(complaintId, "IN_PROGRESS");
        Complaint inProgress = wardenProxy.getComplaintById(complaintId);
        assertEquals("IN_PROGRESS", inProgress.getStatus());

        // Verify history has recorded the previous state
        ComplaintHistory history = wardenProxy.getComplaintHistory();
        assertTrue(history.hasHistory(complaintId), "History must contain the previous state");
        assertEquals(initialStatus, history.peek(complaintId).getStatus());

        // Step 3: Warden updates status to RESOLVED
        wardenProxy.updateComplaintStatus(complaintId, "RESOLVED");
        Complaint resolved = wardenProxy.getComplaintById(complaintId);
        assertEquals("RESOLVED", resolved.getStatus());

        // Step 4: Warden restores complaint status back to IN_PROGRESS
        Complaint restored1 = wardenProxy.restoreComplaintStatus(complaintId);
        assertNotNull(restored1, "Restored complaint must not be null");
        assertEquals("IN_PROGRESS", restored1.getStatus(), "Status should be restored to IN_PROGRESS");

        // Verify database reflects restored state
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT status FROM Complaints WHERE id = ?")) {
            stmt.setInt(1, complaintId);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("IN_PROGRESS", rs.getString("status"), "Database must persist restored status");
        }

        // Step 5: Warden restores once more back to initial status (ASSIGNED or PENDING)
        Complaint restored2 = wardenProxy.restoreComplaintStatus(complaintId);
        assertNotNull(restored2);
        assertEquals(initialStatus, restored2.getStatus(), "Status should be restored to initial status");

        // Verify observer notification for restoration was generated
        List<Notification> notifications = studentProxy.getNotifications();
        boolean foundRestoreNotification = notifications.stream()
                .anyMatch(n -> n.getMessage().contains("Complaint #" + complaintId + " status restored to"));
        assertTrue(foundRestoreNotification, "Observer notification must be generated for status restoration");
    }

    @Test
    @DisplayName("Security check: Students should not be allowed to restore complaint status")
    public void testStudentCannotRestoreComplaintStatus() {
        // Create complaint
        Complaint input = Complaint.builder()
                .type("Maintenance")
                .studentId(studentUser.getId())
                .roomNo("A-203")
                .description("Window latch loose")
                .priority("LOW")
                .build();

        Complaint created = studentProxy.createComplaint(input);
        int complaintId = created.getId();

        // Warden updates status so a memento exists in history
        wardenProxy.updateComplaintStatus(complaintId, "IN_PROGRESS");

        // Student attempts to call restoreComplaintStatus -> SecurityException
        assertThrows(SecurityException.class, () -> {
            studentProxy.restoreComplaintStatus(complaintId);
        }, "Students must be denied from restoring complaint status");
    }

    @Test
    @DisplayName("RealHostelService overloaded createComplaint(ComplaintBuilder) creates complaint correctly")
    public void testOverloadedCreateComplaintWithBuilder() {
        ComplaintBuilder builder = Complaint.builder()
                .type("Water")
                .studentId(studentUser.getId())
                .roomNo("A-203")
                .description("Low water pressure in shower")
                .priority("MEDIUM");

        Complaint created = realService.createComplaint(builder);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("Water", created.getType());
        assertEquals("A-203", created.getRoomNo());
    }
}
