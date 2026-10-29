package com.hostelhub.mediator;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.factory.Complaint;
import com.hostelhub.memento.ComplaintHistory;
import com.hostelhub.model.Technician;
import com.hostelhub.strategy.TechnicianAssignmentStrategy;

import java.util.List;

/**
 * Mediator Pattern: HostelMediator
 *
 * Defines the contract for the central coordinator that decouples all
 * hostel subsystems from each other. Instead of components talking
 * directly to each other, they communicate through this mediator.
 *
 * Coordinates:
 *  - Complaint creation (Builder Pattern)
 *  - Technician assignment (Strategy Pattern)
 *  - Complaint state save/restore (Memento Pattern)
 *  - Complaint cloning from templates (Prototype Pattern)
 *  - Notifications (Observer Pattern)
 *  - Access control (Proxy Pattern via HostelServiceProxy)
 */
public interface HostelMediator {

    // -------------------------------------------------------
    // Complaint Lifecycle (integrates Builder + Observer)
    // -------------------------------------------------------

    /**
     * Student submits a new complaint using the fluent Builder API.
     * Mediator routes it through the Proxy -> Singleton -> Chain -> DB -> Observer.
     *
     * @param builder A configured ComplaintBuilder.
     * @return The fully processed, persisted Complaint with generated ID.
     */
    Complaint submitComplaint(ComplaintBuilder builder);

    /**
     * Student submits a raw Complaint object directly.
     *
     * @param complaint The complaint to submit.
     * @return The processed, persisted Complaint.
     */
    Complaint submitComplaint(Complaint complaint);

    /**
     * Retrieves all complaints visible to the currently logged-in user.
     * (Students see only their own; Wardens see all.)
     *
     * @return List of complaints.
     */
    List<Complaint> listComplaints();

    /**
     * Retrieves a single complaint by its ID.
     *
     * @param complaintId The complaint ID.
     * @return The Complaint, or null if not found.
     */
    Complaint getComplaint(int complaintId);

    // -------------------------------------------------------
    // Prototype Integration (creates complaint from template)
    // -------------------------------------------------------

    /**
     * Creates and submits a complaint cloned from a predefined template.
     * Integrates the Prototype Pattern with the Mediator's submission flow.
     *
     * @param templateKey  The key identifying the template (e.g., "ELECTRICAL_FAN").
     * @param customNote   Optional extra description detail.
     * @return The processed, persisted Complaint.
     */
    Complaint submitComplaintFromTemplate(String templateKey, String customNote);

    /**
     * Clones an existing complaint and submits it as a new one.
     *
     * @param complaintId  The ID of the original complaint to clone.
     * @param customNote   Optional extra description detail.
     * @return The processed, persisted cloned Complaint.
     */
    Complaint cloneAndSubmitComplaint(int complaintId, String customNote);

    /**
     * Returns all available complaint template keys from the Prototype registry.
     *
     * @return List of template keys.
     */
    List<String> getAvailableTemplates();

    // -------------------------------------------------------
    // Warden Workflow: Status Update + Technician Assignment
    // (integrates Strategy + Observer + Memento save)
    // -------------------------------------------------------

    /**
     * Warden updates the status of a complaint.
     * Mediator automatically saves a Memento snapshot BEFORE the update
     * (so it can be undone), then triggers Observer notifications.
     *
     * @param complaintId The complaint to update.
     * @param newStatus   The new status string (e.g., "IN_PROGRESS", "RESOLVED").
     */
    void updateComplaintStatus(int complaintId, String newStatus);

    /**
     * Warden manually assigns a specific technician to a complaint.
     * Mediator coordinates with Proxy (RBAC), updates DB, and notifies via Observer.
     *
     * @param complaintId  The complaint to assign.
     * @param technicianId The technician to assign.
     */
    void assignTechnician(int complaintId, int technicianId);

    /**
     * Warden sets the active technician assignment strategy.
     * Future auto-assignments will use the new strategy.
     *
     * @param strategy The TechnicianAssignmentStrategy to use.
     */
    void setAssignmentStrategy(TechnicianAssignmentStrategy strategy);

    /**
     * Returns the currently active technician assignment strategy name.
     *
     * @return Strategy name string.
     */
    String getActiveStrategyName();

    // -------------------------------------------------------
    // Memento: Restore previous complaint state
    // -------------------------------------------------------

    /**
     * Restores a complaint to its most recent saved state (undo last status change).
     * Warden-only operation.
     *
     * @param complaintId The complaint ID to restore.
     * @return The complaint after state restoration.
     */
    Complaint restoreComplaintStatus(int complaintId);

    /**
     * Returns the ComplaintHistory caretaker for inspection.
     *
     * @return The ComplaintHistory instance.
     */
    ComplaintHistory getComplaintHistory();

    // -------------------------------------------------------
    // Technician Management
    // -------------------------------------------------------

    /**
     * Returns all technicians (Warden only).
     *
     * @return List of Technician objects.
     */
    List<Technician> listTechnicians();
}
