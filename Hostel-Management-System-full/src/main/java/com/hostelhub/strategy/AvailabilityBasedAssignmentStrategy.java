package com.hostelhub.strategy;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import java.util.List;

/**
 * Strategy Pattern: AvailabilityBasedAssignmentStrategy
 * Assigns an eligible technician only if their current status is strictly 'AVAILABLE'.
 * Unlike department-fallback strategies, this strategy refuses to overload busy technicians.
 * If all matching technicians are already assigned or none exist, it cleanly returns null,
 * allowing the complaint to remain in PENDING status until a technician is freed.
 */
public class AvailabilityBasedAssignmentStrategy implements TechnicianAssignmentStrategy {

    @Override
    public Technician assignTechnician(Complaint complaint, List<Technician> candidateTechnicians) {
        if (complaint == null || candidateTechnicians == null || candidateTechnicians.isEmpty()) {
            return null;
        }

        String targetDepartment = complaint.getDepartment();
        if (targetDepartment == null || targetDepartment.trim().isEmpty()) {
            targetDepartment = "General";
        }

        // 1. Find strictly AVAILABLE technician in matching department
        for (Technician tech : candidateTechnicians) {
            boolean deptMatches = tech.getDepartment() != null &&
                    (tech.getDepartment().equalsIgnoreCase(targetDepartment) ||
                     ("General".equalsIgnoreCase(targetDepartment) && "Maintenance".equalsIgnoreCase(tech.getDepartment())));

            if (deptMatches && "AVAILABLE".equalsIgnoreCase(tech.getStatus())) {
                System.out.println("AvailabilityBasedAssignmentStrategy: Assigned available technician: " +
                        tech.getName() + " (" + tech.getDepartment() + ")");
                return tech;
            }
        }

        // 2. If no available technician in target department, check for general maintenance availability
        if (!"General".equalsIgnoreCase(targetDepartment) && !"Maintenance".equalsIgnoreCase(targetDepartment)) {
            for (Technician tech : candidateTechnicians) {
                if ("Maintenance".equalsIgnoreCase(tech.getDepartment()) && "AVAILABLE".equalsIgnoreCase(tech.getStatus())) {
                    System.out.println("AvailabilityBasedAssignmentStrategy: Assigned available general maintenance technician as cross-department backup: " +
                            tech.getName());
                    return tech;
                }
            }
        }

        // 3. No eligible technician is currently available
        System.out.println("AvailabilityBasedAssignmentStrategy: No available technician for department: " +
                targetDepartment + ". Assignment deferred.");
        return null;
    }

    @Override
    public String getStrategyName() {
        return "AvailabilityBasedAssignmentStrategy";
    }
}
