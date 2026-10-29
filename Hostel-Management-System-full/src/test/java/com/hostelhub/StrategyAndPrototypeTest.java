package com.hostelhub;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.db.DatabaseUtil;
import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.prototype.ComplaintPrototypeRegistry;
import com.hostelhub.prototype.Prototype;
import com.hostelhub.proxy.HostelService;
import com.hostelhub.proxy.HostelServiceProxy;
import com.hostelhub.proxy.RealHostelService;
import com.hostelhub.singleton.HostelManagementSystem;
import com.hostelhub.strategy.AvailabilityBasedAssignmentStrategy;
import com.hostelhub.strategy.DepartmentBasedAssignmentStrategy;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;
import com.hostelhub.strategy.WorkloadBalancedAssignmentStrategy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StrategyAndPrototypeTest {

    private User wardenUser;
    private User studentUser1;
    private User studentUser2;
    private HostelService wardenProxy;
    private HostelService student1Proxy;
    private HostelService student2Proxy;
    private RealHostelService realService;

    @BeforeAll
    public static void initDatabase() {
        DatabaseUtil.initializeDatabase();
    }

    @BeforeEach
    public void setUp() {
        wardenUser = new User(1, "warden", "warden123", "WARDEN", "Dr. K. Sharma (Chief Warden)");
        studentUser1 = new User(2, "student1", "student123", "STUDENT", "Aarav Patel", "A-203", "+91 90118 48854", "aarav.patel@hostelhub.edu");
        studentUser2 = new User(3, "ria", "student123", "STUDENT", "Ria Sharma", "A-203", "+91 91234 56789", "ria.sharma@hostelhub.edu");

        realService = new RealHostelService(wardenUser);
        wardenProxy = new HostelServiceProxy(wardenUser);
        student1Proxy = new HostelServiceProxy(studentUser1);
        student2Proxy = new HostelServiceProxy(studentUser2);

        // Reset default strategy on central system
        HostelManagementSystem.getInstance().setTechnicianAssignmentStrategy(new DepartmentBasedAssignmentStrategy());
    }

    // =========================================================================
    // 1. Strategy Pattern Tests
    // =========================================================================

    @Test
    @DisplayName("DepartmentBasedAssignmentStrategy should assign an available technician matching the department")
    public void testDepartmentBasedAssignmentStrategySuccess() {
        TechnicianAssignmentStrategy strategy = new DepartmentBasedAssignmentStrategy();
        assertEquals("DepartmentBasedAssignmentStrategy", strategy.getStrategyName());

        Complaint complaint = new Complaint("Electricity", 2, "A-203", "Fan regulator broken", "HIGH");
        complaint.setDepartment("Electrical");

        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(1, "Raj", "Electrical", "AVAILABLE", "+91 94001 11223"));
        technicians.add(new Technician(2, "Ramesh", "Plumbing", "AVAILABLE", "+91 94002 22334"));

        Technician assigned = strategy.assignTechnician(complaint, technicians);
        assertNotNull(assigned, "Should select a matching technician");
        assertEquals(1, assigned.getId());
        assertEquals("Raj", assigned.getName());
        assertEquals("Electrical", assigned.getDepartment());
    }

    @Test
    @DisplayName("DepartmentBasedAssignmentStrategy should fallback to department technician even if busy")
    public void testDepartmentBasedAssignmentStrategyFallback() {
        TechnicianAssignmentStrategy strategy = new DepartmentBasedAssignmentStrategy();

        Complaint complaint = new Complaint("Electricity", 2, "A-203", "Power switch sparkling", "CRITICAL");
        complaint.setDepartment("Electrical");

        // Both electrical technicians are ASSIGNED (busy)
        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(10, "Karan", "Electrical", "ASSIGNED", ""));
        technicians.add(new Technician(2, "Ramesh", "Plumbing", "AVAILABLE", ""));

        Technician assigned = strategy.assignTechnician(complaint, technicians);
        assertNotNull(assigned, "Department-based strategy should fallback to department specialist");
        assertEquals("Electrical", assigned.getDepartment());
        assertEquals(10, assigned.getId());
    }

    @Test
    @DisplayName("AvailabilityBasedAssignmentStrategy should assign an eligible available technician")
    public void testAvailabilityBasedAssignmentStrategySuccess() {
        TechnicianAssignmentStrategy strategy = new AvailabilityBasedAssignmentStrategy();
        assertEquals("AvailabilityBasedAssignmentStrategy", strategy.getStrategyName());

        Complaint complaint = new Complaint("Water", 2, "A-203", "Tap leak", "MEDIUM");
        complaint.setDepartment("Plumbing");

        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(1, "Raj", "Electrical", "AVAILABLE", ""));
        technicians.add(new Technician(2, "Ramesh", "Plumbing", "AVAILABLE", ""));

        Technician assigned = strategy.assignTechnician(complaint, technicians);
        assertNotNull(assigned);
        assertEquals(2, assigned.getId());
        assertEquals("Ramesh", assigned.getName());
    }

    @Test
    @DisplayName("AvailabilityBasedAssignmentStrategy should return null when no eligible technician is available")
    public void testAvailabilityBasedAssignmentStrategyNoAvailableTechnician() {
        TechnicianAssignmentStrategy strategy = new AvailabilityBasedAssignmentStrategy();

        Complaint complaint = new Complaint("Electricity", 2, "A-203", "Socket burnt", "HIGH");
        complaint.setDepartment("Electrical");

        // Electrical technicians are busy, and no general maintenance is available
        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(1, "Raj", "Electrical", "ASSIGNED", ""));
        technicians.add(new Technician(4, "Suresh", "Maintenance", "ASSIGNED", ""));

        Technician assigned = strategy.assignTechnician(complaint, technicians);
        assertNull(assigned, "Should return null when no eligible technician is available");
    }

    @Test
    @DisplayName("Strategy should handle case when no technician belongs to requested department")
    public void testNoTechnicianInDepartment() {
        TechnicianAssignmentStrategy deptStrategy = new DepartmentBasedAssignmentStrategy();
        TechnicianAssignmentStrategy availStrategy = new AvailabilityBasedAssignmentStrategy();

        Complaint complaint = new Complaint("Carpentry", 2, "A-203", "Bed broken", "LOW");
        complaint.setDepartment("Carpentry");

        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(1, "Raj", "Electrical", "AVAILABLE", ""));
        technicians.add(new Technician(2, "Ramesh", "Plumbing", "AVAILABLE", ""));

        assertNull(deptStrategy.assignTechnician(complaint, technicians));
        assertNull(availStrategy.assignTechnician(complaint, technicians));
    }

    @Test
    @DisplayName("WorkloadBalancedAssignmentStrategy should balance assignment load")
    public void testWorkloadBalancedAssignmentStrategy() {
        TechnicianAssignmentStrategy strategy = new WorkloadBalancedAssignmentStrategy();
        assertEquals("WorkloadBalancedAssignmentStrategy", strategy.getStrategyName());

        Complaint complaint = new Complaint("Water", 2, "B-101", "Sink blockage", "MEDIUM");
        complaint.setDepartment("Plumbing");

        List<Technician> technicians = new ArrayList<>();
        technicians.add(new Technician(2, "Ramesh", "Plumbing", "AVAILABLE", ""));
        technicians.add(new Technician(12, "Vijay", "Plumbing", "AVAILABLE", ""));

        Technician chosen = strategy.assignTechnician(complaint, technicians);
        assertNotNull(chosen);
        assertEquals("Plumbing", chosen.getDepartment());
    }

    @Test
    @DisplayName("Warden should be able to switch strategies dynamically, while Student is denied")
    public void testStrategySwitchingAndSecurity() {
        // Default strategy
        TechnicianAssignmentStrategy defaultStrategy = wardenProxy.getTechnicianAssignmentStrategy();
        assertNotNull(defaultStrategy);

        // Warden changes strategy to DepartmentBasedAssignmentStrategy
        TechnicianAssignmentStrategy deptStrategy = new DepartmentBasedAssignmentStrategy();
        wardenProxy.setTechnicianAssignmentStrategy(deptStrategy);
        assertEquals("DepartmentBasedAssignmentStrategy",
                wardenProxy.getTechnicianAssignmentStrategy().getStrategyName());

        // Student attempting to change strategy must throw SecurityException
        assertThrows(SecurityException.class, () -> {
            student1Proxy.setTechnicianAssignmentStrategy(new AvailabilityBasedAssignmentStrategy());
        }, "Students must be prohibited from changing technician assignment strategy");

        // Student can query current strategy
        assertEquals("DepartmentBasedAssignmentStrategy",
                student1Proxy.getTechnicianAssignmentStrategy().getStrategyName());
    }

    // =========================================================================
    // 2. Prototype Pattern Tests
    // =========================================================================

    @Test
    @DisplayName("Complaint.clone() should deep-copy template attributes and properly reset identity/runtime fields")
    public void testComplaintDirectClone() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Complaint original = Complaint.builder()
                .id(999)
                .studentId(2)
                .studentName("Aarav Patel")
                .roomNo("A-203")
                .type("Electricity")
                .department("Electrical")
                .description("Fan regulator not functioning properly")
                .priority("HIGH")
                .status("RESOLVED")
                .technicianId(1)
                .technicianName("Raj")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Complaint clone = original.clone();

        assertNotNull(clone);
        assertNotSame(original, clone, "Clone must be a distinct object in memory");

        // Template attributes MUST be copied
        assertEquals(original.getType(), clone.getType());
        assertEquals(original.getDepartment(), clone.getDepartment());
        assertEquals(original.getDescription(), clone.getDescription());
        assertEquals(original.getPriority(), clone.getPriority());

        // Identity, technician, status, and timestamp fields MUST be reset
        assertEquals(0, clone.getId(), "Clone ID must be reset to 0 for auto-generation");
        assertEquals(0, clone.getStudentId(), "Clone student ID must be reset to 0");
        assertNull(clone.getStudentName(), "Clone student name must be reset to null");
        assertEquals("", clone.getRoomNo(), "Clone room number must be reset to empty");
        assertEquals("PENDING", clone.getStatus(), "Clone status must always be reset to PENDING");
        assertEquals(0, clone.getTechnicianId(), "Clone technician ID must be reset to 0");
        assertNull(clone.getTechnicianName(), "Clone technician name must be reset to null");
        assertNull(clone.getCreatedAt(), "Clone createdAt must be null");
        assertNull(clone.getUpdatedAt(), "Clone updatedAt must be null");
    }

    @Test
    @DisplayName("Cloned complaints must maintain completely independent mutable state")
    public void testCloneIndependentMutableState() {
        Complaint template = new Complaint("Water", 0, "", "Washroom tap dripping", "MEDIUM");
        template.setDepartment("Plumbing");

        Complaint clone1 = template.cloneForStudent(2, "A-203");
        Complaint clone2 = template.cloneForStudent(3, "B-101");

        // Mutate clone1
        clone1.setDescription("Washroom tap leaking heavily and handle loose");
        clone1.setPriority("HIGH");
        clone1.setStatus("IN_PROGRESS");

        // Verify template was NOT modified
        assertEquals("Washroom tap dripping", template.getDescription());
        assertEquals("MEDIUM", template.getPriority());
        assertEquals("PENDING", template.getStatus());

        // Verify clone2 was NOT modified by clone1 mutations
        assertEquals("Washroom tap dripping", clone2.getDescription());
        assertEquals("MEDIUM", clone2.getPriority());
        assertEquals("B-101", clone2.getRoomNo());
        assertEquals(3, clone2.getStudentId());
        assertEquals("PENDING", clone2.getStatus());
    }

    @Test
    @DisplayName("cloneWith() should specialize student, room, and append custom description")
    public void testCloneWithCustomDetails() {
        Complaint template = new Complaint("Electricity", 0, "", "Ceiling fan issue", "HIGH");
        template.setDepartment("Electrical");

        Complaint customized = template.cloneWith(2, "A-203", "Making buzzing sound at speed 3");

        assertEquals(2, customized.getStudentId());
        assertEquals("A-203", customized.getRoomNo());
        assertEquals("Ceiling fan issue - Making buzzing sound at speed 3", customized.getDescription());
        assertEquals("HIGH", customized.getPriority());
        assertEquals("PENDING", customized.getStatus());
    }

    @Test
    @DisplayName("ComplaintPrototypeRegistry should contain all required standard templates")
    public void testComplaintPrototypeRegistryPredefinedTemplates() {
        ComplaintPrototypeRegistry registry = new ComplaintPrototypeRegistry();
        assertTrue(registry.getTemplateCount() >= 10, "Registry should contain standard templates");

        String[] expectedTemplates = {
                "ELECTRICAL_FAN",
                "ELECTRICAL_LIGHT",
                "PLUMBING_LEAK",
                "PLUMBING_DRAINAGE",
                "MAINTENANCE_AC",
                "MAINTENANCE_FURNITURE",
                "MAINTENANCE_CLEANING",
                "MESS_QUALITY",
                "EMERGENCY_MEDICAL",
                "EMERGENCY_FIRE"
        };

        for (String key : expectedTemplates) {
            assertTrue(registry.hasTemplate(key), "Registry must contain template: " + key);
            Complaint clone = registry.getTemplate(key);
            assertNotNull(clone, "Cloned template must not be null: " + key);
            assertEquals(0, clone.getId());
            assertEquals("PENDING", clone.getStatus());
            assertNotNull(clone.getType());
            assertNotNull(clone.getDepartment());
            assertNotNull(clone.getDescription());
        }
    }

    @Test
    @DisplayName("ComplaintPrototypeRegistry should support custom template registration and retrieval")
    public void testComplaintPrototypeRegistryCustomRegistration() {
        ComplaintPrototypeRegistry registry = new ComplaintPrototypeRegistry();

        Complaint customTemplate = Complaint.builder()
                .type("Maintenance")
                .department("General")
                .description("Balcony pigeon netting broken")
                .priority("MEDIUM")
                .build();

        registry.registerTemplate("BALCONY_NET", customTemplate);
        assertTrue(registry.hasTemplate("BALCONY_NET"));

        Complaint clone = registry.createFromTemplate("BALCONY_NET", 2, "A-203", "Facing north courtyard");
        assertNotNull(clone);
        assertEquals(2, clone.getStudentId());
        assertEquals("A-203", clone.getRoomNo());
        assertEquals("Balcony pigeon netting broken - Facing north courtyard", clone.getDescription());
    }

    @Test
    @DisplayName("Complaint.toBuilder() and ComplaintBuilder.fromPrototype() should integrate seamlessly")
    public void testComplaintToBuilderIntegration() {
        Complaint prototype = new Complaint("Maintenance", 0, "", "Window latch stuck", "LOW");
        prototype.setDepartment("General");

        // Convert prototype to builder and customize
        Complaint customized = prototype.toBuilder()
                .studentId(2)
                .roomNo("A-203")
                .priority("MEDIUM")
                .description("Window latch stuck and glass vibrating")
                .build();

        assertNotNull(customized);
        assertEquals(2, customized.getStudentId());
        assertEquals("A-203", customized.getRoomNo());
        assertEquals("Maintenance", customized.getType());
        assertEquals("General", customized.getDepartment());
        assertEquals("Window latch stuck and glass vibrating", customized.getDescription());
        assertEquals("MEDIUM", customized.getPriority());
        assertEquals("PENDING", customized.getStatus());
    }

    // =========================================================================
    // 3. End-to-End Service and Proxy Integration Tests
    // =========================================================================

    @Test
    @DisplayName("Student should be able to create a complaint directly from a predefined template")
    public void testEndToEndCreateComplaintFromTemplate() throws Exception {
        // Use student proxy to raise complaint from template
        Complaint created = student1Proxy.createComplaintFromTemplate("PLUMBING_LEAK", "Under the bathroom sink");

        assertNotNull(created, "Created complaint from template must not be null");
        assertTrue(created.getId() > 0, "Created complaint should have database ID");
        assertEquals(studentUser1.getId(), created.getStudentId());
        assertEquals("A-203", created.getRoomNo());
        assertEquals("Water", created.getType());
        assertEquals("Plumbing", created.getDepartment());
        assertTrue(created.getDescription().contains("Water leakage"));
        assertTrue(created.getDescription().contains("Under the bathroom sink"));

        // Verify stored correctly in SQLite
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Complaints WHERE id = ?")) {
            stmt.setInt(1, created.getId());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Complaint must exist in SQLite DB");
            assertEquals("Water", rs.getString("type"));
            assertEquals("Plumbing", rs.getString("department"));
        }
    }

    @Test
    @DisplayName("Student should be able to clone their own previous complaint with a custom note")
    public void testEndToEndCloneExistingComplaint() {
        // 1. Create initial complaint
        Complaint first = student1Proxy.createComplaint(
                new Complaint("Electricity", studentUser1.getId(), "A-203", "Light bulb fused", "LOW")
        );
        assertNotNull(first);
        int firstId = first.getId();

        // 2. Clone the complaint as a recurring issue
        Complaint cloned = student1Proxy.cloneComplaint(firstId, "Replacement bulb fused again after 2 days");

        assertNotNull(cloned);
        assertNotEquals(firstId, cloned.getId(), "Cloned complaint must receive a new distinct database ID");
        assertEquals(first.getType(), cloned.getType());
        assertEquals(first.getDepartment(), cloned.getDepartment());
        assertEquals(studentUser1.getId(), cloned.getStudentId());
        assertEquals("A-203", cloned.getRoomNo());
        assertTrue(cloned.getDescription().contains("Replacement bulb fused again after 2 days"));
    }

    @Test
    @DisplayName("Proxy should prevent a student from cloning another student's complaint")
    public void testProxySecurityStudentCannotCloneOtherStudentComplaint() {
        // Student 1 creates a complaint
        Complaint s1Complaint = student1Proxy.createComplaint(
                new Complaint("Maintenance", studentUser1.getId(), "A-203", "Personal desk key lost", "LOW")
        );

        // Student 2 attempts to clone Student 1's complaint -> SecurityException
        assertThrows(SecurityException.class, () -> {
            student2Proxy.cloneComplaint(s1Complaint.getId(), "I want this too");
        }, "Students must be prohibited from cloning complaints raised by other students");

        // Warden CAN clone any complaint
        Complaint wardenClone = wardenProxy.cloneComplaint(s1Complaint.getId(), "Warden verified duplicate");
        assertNotNull(wardenClone);
    }
}
