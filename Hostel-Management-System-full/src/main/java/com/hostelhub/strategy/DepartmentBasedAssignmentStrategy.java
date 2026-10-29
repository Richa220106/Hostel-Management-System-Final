package com.hostelhub.strategy;

import com.hostelhub.factory.Complaint;
import com.hostelhub.model.Technician;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Pattern: DepartmentBasedAssignmentStrategy
 * Assigns a suitable technician strictly according to the complaint's department.
 * Prioritizes available technicians within that department, but can fall back to
 * a department specialist even if busy to ensure domain expertise.
 * If no technician belongs to the required department, returns null.
 */
public class DepartmentBasedAssignmentStrategy implements TechnicianAssignmentStrategy {

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

        // Filter candidate technicians matching the department
        List<Technician> deptTechnicians = candidateTechnicians.stream()
                .filter(t -> t.getDepartment() != null &&
                        (t.getDepartment().equalsIgnoreCase(dept) ||
                         ("General".equalsIgnoreCase(dept) && "Maintenance".equalsIgnoreCase(t.getDepartment()))))
                .collect(Collectors.toList());

        if (deptTechnicians.isEmpty()) {
            System.out.println("DepartmentBasedAssignmentStrategy: No technicians found for department: " + dept);
            return null;
        }

        // 1. Prefer an AVAILABLE technician in that department
        for (Technician tech : deptTechnicians) {
            if ("AVAILABLE".equalsIgnoreCase(tech.getStatus())) {
                System.out.println("DepartmentBasedAssignmentStrategy: Assigned available department technician: " +
                        tech.getName() + " (" + tech.getDepartment() + ")");
                return tech;
            }
        }

        // 2. Fallback: assign first matching technician in that department
        Technician fallback = deptTechnicians.get(0);
        System.out.println("DepartmentBasedAssignmentStrategy: Falling back to active department technician: " +
                fallback.getName() + " (" + fallback.getDepartment() + ")");
        return fallback;
    }

    @Override
    public String getStrategyName() {
        return "DepartmentBasedAssignmentStrategy";
    }
}
