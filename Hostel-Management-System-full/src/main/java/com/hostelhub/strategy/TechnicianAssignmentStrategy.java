package com.hostelhub.strategy;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import java.util.List;

/**
 * Strategy Pattern: TechnicianAssignmentStrategy
 * Defines the family of algorithms for assigning technicians to complaints.
 * Concrete strategies encapsulate distinct assignment policies and can be
 * switched dynamically at runtime without affecting client code.
 */
public interface TechnicianAssignmentStrategy {

    /**
     * Selects and assigns an appropriate technician for the given complaint
     * from the candidate list.
     *
     * @param complaint The complaint requiring technician assignment.
     * @param candidateTechnicians The candidate technicians available for assignment.
     * @return The chosen Technician, or null if no suitable technician is available.
     */
    Technician assignTechnician(Complaint complaint, List<Technician> candidateTechnicians);

    /**
     * Returns a descriptive name for this assignment strategy.
     */
    String getStrategyName();
}
