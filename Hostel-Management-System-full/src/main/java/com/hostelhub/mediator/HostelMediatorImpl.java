package com.hostelhub.mediator;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.factory.Complaint;
import com.hostelhub.memento.ComplaintHistory;
import com.hostelhub.model.Technician;
import com.hostelhub.model.User;
import com.hostelhub.proxy.HostelService;
import com.hostelhub.proxy.HostelServiceProxy;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;

import java.util.List;

/**
 * Mediator Pattern: HostelMediatorImpl
 *
 * The concrete mediator that centralises all coordination between
 * the hostel management subsystems.  Instead of components calling
 * each other directly, they go through this single entry-point.
 *
 * What it integrates:
 *  ┌──────────────────────────────────────────────────────┐
 *  │  Builder   →  builds structured Complaint objects     │
 *  │  Prototype →  clones complaint templates              │
 *  │  Strategy  →  selects technician assignment policy    │
 *  │  Memento   →  saves/restores complaint state          │
 *  │  Observer  →  notifies student & warden on changes    │
 *  │  Proxy     →  enforces RBAC for every operation       │
 *  │  Singleton →  reaches the central HostelManagementSystem│
 *  └──────────────────────────────────────────────────────┘
 *
 * How to use:
 *   User student = ...;          // logged-in student
 *   User warden  = ...;          // logged-in warden
 *   HostelMediator studentSide = new HostelMediatorImpl(student);
 *   HostelMediator wardenSide  = new HostelMediatorImpl(warden);
 *
 *   // Student submits complaint via Builder
 *   studentSide.submitComplaint(
 *       ComplaintBuilder.builder()
 *           .type("Electricity").department("Electrical")
 *           .description("Fan not working").priority("HIGH")
 *   );
 *
 *   // Warden changes strategy and updates status
 *   wardenSide.setAssignmentStrategy(new AvailabilityBasedAssignmentStrategy());
 *   wardenSide.updateComplaintStatus(complaintId, "IN_PROGRESS");
 *
 *   // Warden undoes last status change
 *   wardenSide.restoreComplaintStatus(complaintId);
 */
public class HostelMediatorImpl implements HostelMediator {

    /**
     * The HostelServiceProxy is the single gateway to all real operations.
     * It enforces Role-Based Access Control (RBAC) transparently.
     * The mediator holds one proxy per logged-in user so that RBAC rules
     * always match the actual actor performing each action.
     */
    private final HostelService service;

    /**
     * The User on whose behalf this mediator is operating.
     * Exposed so callers can inspect role/identity if needed.
     */
    private final User currentUser;

    /**
     * Constructs a mediator for a specific logged-in user.
     * The HostelServiceProxy is created here, wiring RBAC automatically.
     *
     * @param currentUser The authenticated user (Student or Warden).
     */
    public HostelMediatorImpl(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("HostelMediatorImpl: currentUser must not be null.");
        }
        this.currentUser = currentUser;
        // Proxy wires RBAC — mediator delegates all real work through it
        this.service = new HostelServiceProxy(currentUser);
    }

    // -------------------------------------------------------
    // Complaint Lifecycle
    // -------------------------------------------------------

    /**
     * Submits a complaint built with the Builder pattern.
     *
     * Flow:
     *   ComplaintBuilder.build()
     *     → HostelServiceProxy.createComplaint(Complaint)   [RBAC check]
     *       → RealHostelService.createComplaint(Complaint)
     *         → HostelManagementSystem.processNewComplaint() [Singleton]
     *           → Chain of Responsibility routes & assigns technician
     *           → Persisted to DB
     *           → ComplaintManager.onComplaintStatusChanged() [Observer]
     *             → UserNotifier writes Notification rows
     */
    @Override
    public Complaint submitComplaint(ComplaintBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Mediator: ComplaintBuilder must not be null.");
        }
        System.out.println("[Mediator] " + currentUser.getName()
                + " submitting complaint via Builder...");
        Complaint result = service.createComplaint(builder);
        System.out.println("[Mediator] Complaint #" + result.getId()
                + " created successfully [Status: " + result.getStatus() + "]");
        return result;
    }

    /**
     * Submits a raw Complaint object.
     *
     * Flow: Same as above but skips the builder.build() step.
     */
    @Override
    public Complaint submitComplaint(Complaint complaint) {
        if (complaint == null) {
            throw new IllegalArgumentException("Mediator: Complaint must not be null.");
        }
        System.out.println("[Mediator] " + currentUser.getName()
                + " submitting complaint [Type: " + complaint.getType() + "]...");
        Complaint result = service.createComplaint(complaint);
        System.out.println("[Mediator] Complaint #" + result.getId()
                + " created successfully [Status: " + result.getStatus() + "]");
        return result;
    }

    /** Returns complaints scoped to the current user's role. */
    @Override
    public List<Complaint> listComplaints() {
        System.out.println("[Mediator] " + currentUser.getName() + " listing complaints...");
        return service.getComplaints();
    }

    /** Returns a single complaint by ID (proxy enforces student-scoping). */
    @Override
    public Complaint getComplaint(int complaintId) {
        return service.getComplaintById(complaintId);
    }

    // -------------------------------------------------------
    // Prototype Integration
    // -------------------------------------------------------

    /**
     * Clones a predefined complaint template and submits it.
     *
     * Flow:
     *   PrototypeRegistry.createFromTemplate(key, studentId, roomNo, note)
     *     → Complaint.clone()   [Prototype Pattern]
     *     → submitComplaint(clone)  [→ Builder/Observer/DB]
     */
    @Override
    public Complaint submitComplaintFromTemplate(String templateKey, String customNote) {
        if (templateKey == null || templateKey.isBlank()) {
            throw new IllegalArgumentException("Mediator: templateKey must not be null/blank.");
        }
        System.out.println("[Mediator] " + currentUser.getName()
                + " creating complaint from template: " + templateKey);
        Complaint result = service.createComplaintFromTemplate(templateKey, customNote);
        System.out.println("[Mediator] Complaint #" + result.getId()
                + " created from template [Status: " + result.getStatus() + "]");
        return result;
    }

    /**
     * Clones an existing complaint by ID and submits as a new one.
     *
     * Flow:
     *   getComplaintById(id)  → Complaint.cloneWith(studentId, roomNo, note)
     *     → submitComplaint(clone)
     */
    @Override
    public Complaint cloneAndSubmitComplaint(int complaintId, String customNote) {
        System.out.println("[Mediator] " + currentUser.getName()
                + " cloning Complaint #" + complaintId + "...");
        Complaint result = service.cloneComplaint(complaintId, customNote);
        System.out.println("[Mediator] Cloned Complaint #" + result.getId()
                + " submitted [Status: " + result.getStatus() + "]");
        return result;
    }

    /** Returns all registered template keys from the Prototype registry. */
    @Override
    public List<String> getAvailableTemplates() {
        return service.getAvailableComplaintTemplates();
    }

    // -------------------------------------------------------
    // Warden Workflow: Status Update + Technician Assignment
    // -------------------------------------------------------

    /**
     * Warden updates a complaint's status.
     *
     * Mediator coordinates:
     *   1. Proxy enforces Warden-only RBAC.
     *   2. RealHostelService saves a Memento BEFORE updating (undo support).
     *   3. DB is updated.
     *   4. Observer notifies student + warden.
     *
     * The Memento save happens transparently inside RealHostelService.updateComplaintStatus().
     * The mediator makes this invisible to the caller — they just call updateComplaintStatus().
     */
    @Override
    public void updateComplaintStatus(int complaintId, String newStatus) {
        System.out.println("[Mediator] Warden " + currentUser.getName()
                + " updating Complaint #" + complaintId + " → " + newStatus);
        service.updateComplaintStatus(complaintId, newStatus);
        System.out.println("[Mediator] Status update complete. Previous state saved to Memento history.");
    }

    /**
     * Warden manually assigns a technician to a complaint.
     *
     * Mediator coordinates:
     *   1. Proxy enforces Warden-only RBAC.
     *   2. DB updated (complaint + technician status).
     *   3. Observer notifies student + warden.
     */
    @Override
    public void assignTechnician(int complaintId, int technicianId) {
        System.out.println("[Mediator] Warden " + currentUser.getName()
                + " manually assigning Technician #" + technicianId
                + " to Complaint #" + complaintId);
        service.assignTechnician(complaintId, technicianId);
        System.out.println("[Mediator] Technician assigned successfully.");
    }

    /**
     * Changes the active technician assignment strategy at runtime.
     *
     * Mediator coordinates:
     *   1. Proxy enforces Warden-only RBAC.
     *   2. HostelManagementSystem (Singleton) propagates the new strategy
     *      to the entire Chain of Responsibility.
     */
    @Override
    public void setAssignmentStrategy(TechnicianAssignmentStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Mediator: Strategy must not be null.");
        }
        System.out.println("[Mediator] Warden " + currentUser.getName()
                + " switching assignment strategy to: " + strategy.getStrategyName());
        service.setTechnicianAssignmentStrategy(strategy);
        System.out.println("[Mediator] Strategy updated successfully.");
    }

    /** Returns the name of the currently active assignment strategy. */
    @Override
    public String getActiveStrategyName() {
        TechnicianAssignmentStrategy strategy = service.getTechnicianAssignmentStrategy();
        return strategy != null ? strategy.getStrategyName() : "None";
    }

    // -------------------------------------------------------
    // Memento: Restore previous complaint state
    // -------------------------------------------------------

    /**
     * Restores a complaint to its most recently saved Memento state (undo).
     *
     * Mediator coordinates:
     *   1. Proxy enforces Warden-only RBAC.
     *   2. ComplaintHistory.pop(complaintId) retrieves the last snapshot.
     *   3. DB is restored to the snapshot's status/technicianId/department.
     *   4. Observer notifies student + warden about the restoration.
     */
    @Override
    public Complaint restoreComplaintStatus(int complaintId) {
        System.out.println("[Mediator] Warden " + currentUser.getName()
                + " restoring Complaint #" + complaintId + " to previous state...");
        Complaint restored = service.restoreComplaintStatus(complaintId);
        if (restored != null) {
            System.out.println("[Mediator] Complaint #" + complaintId
                    + " restored → Status: " + restored.getStatus());
        } else {
            System.out.println("[Mediator] No saved state found for Complaint #" + complaintId);
        }
        return restored;
    }

    /**
     * Exposes the ComplaintHistory caretaker for programmatic inspection.
     * (Warden-only via Proxy RBAC.)
     */
    @Override
    public ComplaintHistory getComplaintHistory() {
        return service.getComplaintHistory();
    }

    // -------------------------------------------------------
    // Technician Management
    // -------------------------------------------------------

    /**
     * Returns all technicians (Warden only, enforced by Proxy).
     */
    @Override
    public List<Technician> listTechnicians() {
        System.out.println("[Mediator] Warden " + currentUser.getName()
                + " listing all technicians...");
        return service.getTechnicians();
    }

    // -------------------------------------------------------
    // Utility
    // -------------------------------------------------------

    /**
     * Returns the user this mediator is acting on behalf of.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the underlying HostelService (Proxy) for advanced use cases.
     * Use sparingly — prefer mediator methods for coordinated workflows.
     */
    public HostelService getService() {
        return service;
    }
}
