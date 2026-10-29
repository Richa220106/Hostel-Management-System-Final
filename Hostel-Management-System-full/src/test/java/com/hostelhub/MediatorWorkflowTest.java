package com.hostelhub;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.mediator.HostelMediator;
import com.hostelhub.mediator.HostelMediatorImpl;
import com.hostelhub.memento.ComplaintHistory;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.strategy.AvailabilityBasedAssignmentStrategy;
import com.hostelhub.strategy.DepartmentBasedAssignmentStrategy;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mediator Pattern — End-to-End Integration Tests (Person 3)
 *
 * Verifies that HostelMediatorImpl correctly coordinates:
 *   - Builder Pattern   : complaint construction via fluent API
 *   - Prototype Pattern : complaint creation from predefined templates
 *   - Strategy Pattern  : runtime-switchable technician assignment
 *   - Memento Pattern   : save and restore complaint state
 *   - Observer Pattern  : notifications fired on every state change
 *   - Proxy Pattern     : RBAC enforced transparently through the mediator
 *
 * Test users (match the seeded DB):
 *   Warden  → id=1, role=WARDEN
 *   Student → id=2, role=STUDENT
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MediatorWorkflowTest {

    // ---------- shared test fixtures ----------
    private static User student;
    private static User warden;
    private static HostelMediator studentMediator;
    private static HostelMediator wardenMediator;

    /** ID of the complaint created in the student-submit test, reused later. */
    private static int createdComplaintId;

    @BeforeAll
    static void setup() {
        DatabaseUtil.initializeDatabase();

        student = new User(2, "student1", "student123", "STUDENT",
                "Aarav Patel", "A-203", "+91 90118 48854", "aarav.patel@hostelhub.edu");

        warden = new User(1, "warden", "warden123", "WARDEN",
                "Chief Warden", null, null, null);

        studentMediator = new HostelMediatorImpl(student);
        wardenMediator  = new HostelMediatorImpl(warden);
    }

    // =============================================================
    // 1. Mediator Construction & Basic Sanity
    // =============================================================

    @Test
    @Order(1)
    @DisplayName("Mediator instantiates correctly for Student and Warden")
    void testMediatorInstantiation() {
        assertNotNull(studentMediator, "Student mediator must not be null");
        assertNotNull(wardenMediator,  "Warden mediator must not be null");

        // Verify the mediator exposes the correct user
        assertEquals("Aarav Patel",
                ((HostelMediatorImpl) studentMediator).getCurrentUser().getName());
        assertEquals("Chief Warden",
                ((HostelMediatorImpl) wardenMediator).getCurrentUser().getName());
    }

    @Test
    @Order(2)
    @DisplayName("Mediator rejects null user at construction time")
    void testMediatorRejectsNullUser() {
        assertThrows(IllegalArgumentException.class,
                () -> new HostelMediatorImpl(null),
                "Mediator must throw if user is null");
    }

    // =============================================================
    // 2. Student Workflow — Submit via Builder
    // =============================================================

    @Test
    @Order(3)
    @DisplayName("Student submits complaint via Builder through Mediator")
    void testStudentSubmitComplaintViaBuilder() {
        ComplaintBuilder builder = ComplaintBuilder.builder()
                .studentId(student.getId())
                .roomNo(student.getRoomNo())
                .type("Electricity")
                .department("Electrical")
                .description("Fan not working in room A-203 - mediator builder test")
                .priority("HIGH");

        Complaint result = studentMediator.submitComplaint(builder);

        assertNotNull(result, "Result must not be null");
        assertTrue(result.getId() > 0, "Complaint must have a DB-generated ID");
        assertEquals("Electrical", result.getDepartment(),
                "Department should be Electrical");
        assertEquals("ASSIGNED", result.getStatus(),
                "Chain-of-Responsibility should auto-assign a technician");
        assertTrue(result.getTechnicianId() > 0,
                "A technician must be assigned automatically");

        // Store for later tests
        createdComplaintId = result.getId();
        System.out.println("[Test] Complaint #" + createdComplaintId + " created via Mediator+Builder.");
    }

    @Test
    @Order(4)
    @DisplayName("Student can list their own complaints through Mediator")
    void testStudentListComplaints() {
        List<Complaint> complaints = studentMediator.listComplaints();

        assertNotNull(complaints, "Complaints list must not be null");
        assertFalse(complaints.isEmpty(), "Student must have at least one complaint");

        // Verify student only sees their own complaints
        boolean allOwnedByStudent = complaints.stream()
                .allMatch(c -> c.getStudentId() == student.getId());
        assertTrue(allOwnedByStudent,
                "Student must only see their own complaints via Mediator");
    }

    @Test
    @Order(5)
    @DisplayName("Student can retrieve a specific complaint by ID through Mediator")
    void testStudentGetComplaintById() {
        assumeComplaintCreated();
        Complaint found = studentMediator.getComplaint(createdComplaintId);

        assertNotNull(found, "Complaint must be found");
        assertEquals(createdComplaintId, found.getId());
        assertEquals(student.getId(), found.getStudentId());
    }

    // =============================================================
    // 3. Prototype Pattern — Submit from Template via Mediator
    // =============================================================

    @Test
    @Order(6)
    @DisplayName("Mediator creates complaint from Prototype template")
    void testSubmitComplaintFromTemplate() {
        List<String> templates = studentMediator.getAvailableTemplates();
        assertFalse(templates.isEmpty(), "Prototype registry must have templates");

        // Use the PLUMBING_LEAK template
        Complaint fromTemplate = studentMediator.submitComplaintFromTemplate(
                "PLUMBING_LEAK", "Leaking tap in bathroom - mediator prototype test");

        assertNotNull(fromTemplate, "Complaint from template must not be null");
        assertTrue(fromTemplate.getId() > 0, "Must have a DB-generated ID");
        assertEquals("Water", fromTemplate.getType(),
                "Prototype template type should be Water");
        assertEquals("Plumbing", fromTemplate.getDepartment(),
                "Prototype template department should be Plumbing");
        assertEquals("PENDING", fromTemplate.getStatus(),
                "Template clone should start as PENDING if no plumber available, " +
                        "or ASSIGNED if one is found");
        // Status is PENDING or ASSIGNED depending on available technicians
        assertTrue("PENDING".equals(fromTemplate.getStatus())
                        || "ASSIGNED".equals(fromTemplate.getStatus()),
                "Status must be PENDING or ASSIGNED");

        System.out.println("[Test] Template complaint #" + fromTemplate.getId()
                + " created [" + fromTemplate.getStatus() + "]");
    }

    @Test
    @Order(7)
    @DisplayName("Mediator rejects unknown Prototype template key")
    void testUnknownTemplateKeyThrows() {
        assertThrows(Exception.class,
                () -> studentMediator.submitComplaintFromTemplate("NONEXISTENT_KEY", null),
                "Unknown template key must throw an exception");
    }

    // =============================================================
    // 4. Prototype Pattern — Clone existing complaint via Mediator
    // =============================================================

    @Test
    @Order(8)
    @DisplayName("Student clones own complaint through Mediator")
    void testCloneComplaintViaMediator() {
        assumeComplaintCreated();

        Complaint cloned = studentMediator.cloneAndSubmitComplaint(
                createdComplaintId, "Same issue recurring - cloned via mediator");

        assertNotNull(cloned, "Cloned complaint must not be null");
        assertTrue(cloned.getId() > 0, "Cloned complaint must have a new DB ID");
        assertNotEquals(createdComplaintId, cloned.getId(),
                "Cloned complaint must have a different ID from the original");
        assertEquals(student.getId(), cloned.getStudentId(),
                "Cloned complaint must belong to the student");

        System.out.println("[Test] Complaint #" + createdComplaintId
                + " cloned → new Complaint #" + cloned.getId());
    }

    // =============================================================
    // 5. Strategy Pattern — Switch via Mediator (Warden only)
    // =============================================================

    @Test
    @Order(9)
    @DisplayName("Warden can switch assignment strategy through Mediator")
    void testWardenSwitchesStrategy() {
        // Default strategy is DepartmentBased
        String before = wardenMediator.getActiveStrategyName();
        System.out.println("[Test] Strategy before: " + before);

        // Switch to AvailabilityBased
        wardenMediator.setAssignmentStrategy(new AvailabilityBasedAssignmentStrategy());
        String after = wardenMediator.getActiveStrategyName();
        System.out.println("[Test] Strategy after: " + after);

        assertEquals("AvailabilityBasedAssignmentStrategy", after,
                "Strategy should have switched to AvailabilityBased");

        // Switch back to Department-based for subsequent tests
        wardenMediator.setAssignmentStrategy(new DepartmentBasedAssignmentStrategy());
    }

    @Test
    @Order(10)
    @DisplayName("Student cannot switch strategy — Proxy blocks with SecurityException")
    void testStudentCannotSwitchStrategy() {
        assertThrows(SecurityException.class,
                () -> studentMediator.setAssignmentStrategy(new AvailabilityBasedAssignmentStrategy()),
                "Student must NOT be able to switch strategy — Proxy must block");
    }

    // =============================================================
    // 6. Warden Workflow — Status Update + Memento Save
    // =============================================================

    @Test
    @Order(11)
    @DisplayName("Warden updates complaint status through Mediator (Memento saves before update)")
    void testWardenUpdateComplaintStatus() {
        assumeComplaintCreated();

        // Update status to IN_PROGRESS — Memento snapshot saved inside RealHostelService
        wardenMediator.updateComplaintStatus(createdComplaintId, "IN_PROGRESS");

        Complaint updated = wardenMediator.getComplaint(createdComplaintId);
        assertNotNull(updated, "Updated complaint must exist");
        assertEquals("IN_PROGRESS", updated.getStatus(),
                "Status must be IN_PROGRESS after warden update");

        System.out.println("[Test] Complaint #" + createdComplaintId
                + " → IN_PROGRESS (Memento snapshot saved)");
    }

    @Test
    @Order(12)
    @DisplayName("Student cannot update complaint status — Proxy blocks with SecurityException")
    void testStudentCannotUpdateStatus() {
        assumeComplaintCreated();
        assertThrows(SecurityException.class,
                () -> studentMediator.updateComplaintStatus(createdComplaintId, "RESOLVED"),
                "Student must NOT be allowed to change complaint status");
    }

    // =============================================================
    // 7. Memento Pattern — Restore via Mediator
    // =============================================================

    @Test
    @Order(13)
    @DisplayName("Warden restores complaint to previous state via Mediator (Memento undo)")
    void testWardenRestoresComplaintState() {
        assumeComplaintCreated();

        // First move it to RESOLVED (another snapshot is saved)
        wardenMediator.updateComplaintStatus(createdComplaintId, "RESOLVED");
        Complaint resolved = wardenMediator.getComplaint(createdComplaintId);
        assertEquals("RESOLVED", resolved.getStatus(), "Status should be RESOLVED");

        // Now restore — should go back to IN_PROGRESS (last saved memento)
        Complaint restored = wardenMediator.restoreComplaintStatus(createdComplaintId);
        assertNotNull(restored, "Restored complaint must not be null");

        // The restored status must NOT be RESOLVED
        assertNotEquals("RESOLVED", restored.getStatus(),
                "After restore, status must revert to previous saved state");

        System.out.println("[Test] Complaint #" + createdComplaintId
                + " restored → " + restored.getStatus());
    }

    @Test
    @Order(14)
    @DisplayName("Student cannot restore complaint status — Proxy blocks with SecurityException")
    void testStudentCannotRestoreStatus() {
        assumeComplaintCreated();
        assertThrows(SecurityException.class,
                () -> studentMediator.restoreComplaintStatus(createdComplaintId),
                "Student must NOT be allowed to restore complaint status");
    }

    @Test
    @Order(15)
    @DisplayName("Warden can inspect ComplaintHistory (Memento caretaker) via Mediator")
    void testWardenCanInspectComplaintHistory() {
        assumeComplaintCreated();
        ComplaintHistory history = wardenMediator.getComplaintHistory();

        assertNotNull(history, "ComplaintHistory must not be null");
        // We have done multiple updates in prior tests so there should be history
        // (some may have been popped by restore, but at least the structure exists)
        System.out.println("[Test] History count for complaint #" + createdComplaintId
                + ": " + history.getHistoryCount(createdComplaintId));
    }

    @Test
    @Order(16)
    @DisplayName("Student cannot access ComplaintHistory — Proxy blocks with SecurityException")
    void testStudentCannotAccessHistory() {
        assertThrows(SecurityException.class,
                () -> studentMediator.getComplaintHistory(),
                "Student must NOT be allowed to access complaint history");
    }

    // =============================================================
    // 8. Warden — Manual Technician Assignment via Mediator
    // =============================================================

    @Test
    @Order(17)
    @DisplayName("Warden lists technicians and manually assigns one via Mediator")
    void testWardenAssignsTechnician() {
        assumeComplaintCreated();

        List<Technician> technicians = wardenMediator.listTechnicians();
        assertNotNull(technicians, "Technician list must not be null");
        assertFalse(technicians.isEmpty(), "There must be at least one technician in the DB");

        // Pick the first available technician
        Technician available = technicians.stream()
                .filter(t -> "AVAILABLE".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElse(technicians.get(0));

        // Move complaint back to PENDING first so we can assign
        wardenMediator.updateComplaintStatus(createdComplaintId, "PENDING");

        // Manually assign via mediator
        wardenMediator.assignTechnician(createdComplaintId, available.getId());

        Complaint assigned = wardenMediator.getComplaint(createdComplaintId);
        assertNotNull(assigned, "Complaint must exist after assignment");
        assertEquals("ASSIGNED", assigned.getStatus(),
                "Complaint status must be ASSIGNED after manual assignment");
        assertEquals(available.getId(), assigned.getTechnicianId(),
                "Assigned technician ID must match");

        System.out.println("[Test] Technician " + available.getName()
                + " manually assigned to Complaint #" + createdComplaintId + " via Mediator");
    }

    @Test
    @Order(18)
    @DisplayName("Student cannot assign technicians — Proxy blocks with SecurityException")
    void testStudentCannotAssignTechnician() {
        assumeComplaintCreated();
        assertThrows(SecurityException.class,
                () -> studentMediator.assignTechnician(createdComplaintId, 1),
                "Student must NOT be allowed to assign technicians");
    }

    @Test
    @Order(19)
    @DisplayName("Student cannot list technicians — Proxy blocks with SecurityException")
    void testStudentCannotListTechnicians() {
        assertThrows(SecurityException.class,
                () -> studentMediator.listTechnicians(),
                "Student must NOT be allowed to list technicians");
    }

    // =============================================================
    // 9. Full End-to-End Student → Warden Workflow
    // =============================================================

    @Test
    @Order(20)
    @DisplayName("Full end-to-end: Student files → Warden manages → Resolved via Mediator")
    void testFullEndToEndWorkflow() {
        System.out.println("\n[E2E] ===== Full Mediator Workflow Test =====");

        // --- Student side ---
        ComplaintBuilder builder = ComplaintBuilder.builder()
                .studentId(student.getId())
                .roomNo(student.getRoomNo())
                .type("Water")
                .department("Plumbing")
                .description("Water pipe burst near washroom - E2E mediator test")
                .priority("HIGH");

        Complaint created = studentMediator.submitComplaint(builder);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        System.out.println("[E2E] Student submitted Complaint #" + created.getId()
                + " [" + created.getStatus() + "]");

        // --- Warden side: set strategy ---
        wardenMediator.setAssignmentStrategy(new DepartmentBasedAssignmentStrategy());
        System.out.println("[E2E] Warden set strategy: " + wardenMediator.getActiveStrategyName());

        // --- Warden: move to IN_PROGRESS (Memento snapshot saved automatically) ---
        wardenMediator.updateComplaintStatus(created.getId(), "IN_PROGRESS");
        Complaint inProgress = wardenMediator.getComplaint(created.getId());
        assertEquals("IN_PROGRESS", inProgress.getStatus());
        System.out.println("[E2E] Warden set complaint → IN_PROGRESS");

        // --- Warden: resolve ---
        wardenMediator.updateComplaintStatus(created.getId(), "RESOLVED");
        Complaint resolved = wardenMediator.getComplaint(created.getId());
        assertEquals("RESOLVED", resolved.getStatus());
        System.out.println("[E2E] Warden resolved complaint");

        // --- Warden: undo (restore via Memento) ---
        Complaint undone = wardenMediator.restoreComplaintStatus(created.getId());
        assertNotNull(undone);
        assertNotEquals("RESOLVED", undone.getStatus(),
                "After undo, complaint must no longer be RESOLVED");
        System.out.println("[E2E] Warden undid resolution → " + undone.getStatus());

        System.out.println("[E2E] ===== Full Mediator Workflow Test PASSED =====\n");
    }

    // =============================================================
    // Helper
    // =============================================================

    /** Skips a test with a clear message if no complaint was created yet. */
    private static void assumeComplaintCreated() {
        if (createdComplaintId <= 0) {
            // If complaint creation test failed, skip dependent tests
            Assumptions.assumeTrue(createdComplaintId > 0,
                    "Skipping: complaint was not created in an earlier test.");
        }
    }
}
