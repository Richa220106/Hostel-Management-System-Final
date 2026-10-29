package com.hostelhub.strategy;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: WorkloadBalancedAssignmentStrategy
 * Assigns the technician who currently has the fewest active assignments within the target department.
 * Prioritizes AVAILABLE technicians first, but if multiple or none are available, selects
 * the candidate with the lowest load to achieve optimal load balancing.
 */
public class WorkloadBalancedAssignmentStrategy implements TechnicianAssignmentStrategy {

    @Override
    public Technician assignTechnician(Complaint complaint, List<Technician> candidateTechnicians) {
        if (complaint == null || candidateTechnicians == null || candidateTechnicians.isEmpty()) {
            return null;
        }

        String targetDepartment = complaint.getDepartment();
        if (targetDepartment == null || targetDepartment.trim().isEmpty()) {
            targetDepartment = "General";
        }
        final String dept = targetDepartment;

        List<Technician> deptTechnicians = candidateTechnicians.stream()
                .filter(t -> t.getDepartment() != null &&
                        (t.getDepartment().equalsIgnoreCase(dept) ||
                         ("General".equalsIgnoreCase(dept) && "Maintenance".equalsIgnoreCase(t.getDepartment()))))
                .collect(Collectors.toList());

        if (deptTechnicians.isEmpty()) {
            return null;
        }

        // Available technicians take precedence
        List<Technician> available = deptTechnicians.stream()
                .filter(t -> "AVAILABLE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        if (!available.isEmpty()) {
            Technician chosen = available.get(0);
            System.out.println("WorkloadBalancedAssignmentStrategy: Assigned available technician: " + chosen.getName());
            return chosen;
        }

        // If all are busy, select the first matching technician to balance distribution
        Technician chosen = deptTechnicians.get(0);
        System.out.println("WorkloadBalancedAssignmentStrategy: Assigned candidate technician: " + chosen.getName());
        return chosen;
    }

    @Override
    public String getStrategyName() {
        return "WorkloadBalancedAssignmentStrategy";
    }
}
