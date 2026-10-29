package com.hostelhub.prototype;

import com.hostelhub.builder.ComplaintBuilder;
import com.hostelhub.factory.Complaint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype Pattern: ComplaintPrototypeRegistry
 * Manages reusable, predefined complaint prototypes and templates.
 * Clients retrieve cloned complaints with fresh unique identities, default PENDING status,
 * and completely independent mutable state.
 *
 * Integrates with ComplaintBuilder for elegant prototype construction.
 */
public class ComplaintPrototypeRegistry {

    private final Map<String, Complaint> prototypes = new ConcurrentHashMap<>();

    public ComplaintPrototypeRegistry() {
        loadPredefinedTemplates();
    }

    /**
     * Initializes standard hostel complaint templates using ComplaintBuilder.
     */
    private void loadPredefinedTemplates() {
        // Electrical templates
        registerTemplate("ELECTRICAL_FAN", ComplaintBuilder.builder()
                .type("Electricity")
                .department("Electrical")
                .description("Ceiling fan not working or regulator sparking")
                .priority("HIGH")
                .status("PENDING")
                .build());

        registerTemplate("ELECTRICAL_LIGHT", ComplaintBuilder.builder()
                .type("Electricity")
                .department("Electrical")
                .description("Room tube light or study lamp flickering / fused")
                .priority("MEDIUM")
                .status("PENDING")
                .build());

        // Water and Plumbing templates
        registerTemplate("PLUMBING_LEAK", ComplaintBuilder.builder()
                .type("Water")
                .department("Plumbing")
                .description("Water leakage from tap or washroom supply pipe")
                .priority("HIGH")
                .status("PENDING")
                .build());

        registerTemplate("PLUMBING_DRAINAGE", ComplaintBuilder.builder()
                .type("Water")
                .department("Plumbing")
                .description("Washbasin or shower drain clogged / overflowing")
                .priority("MEDIUM")
                .status("PENDING")
                .build());

        // Maintenance templates
        registerTemplate("MAINTENANCE_AC", ComplaintBuilder.builder()
                .type("Maintenance")
                .department("Electrical")
                .description("AC is not cooling properly or leaking water")
                .priority("HIGH")
                .status("PENDING")
                .build());

        registerTemplate("MAINTENANCE_FURNITURE", ComplaintBuilder.builder()
                .type("Maintenance")
                .department("Maintenance")
                .description("Study desk, wardrobe latch, or bed frame damaged")
                .priority("LOW")
                .status("PENDING")
                .build());

        registerTemplate("MAINTENANCE_CLEANING", ComplaintBuilder.builder()
                .type("Maintenance")
                .department("General")
                .description("Room deep cleaning and sanitation requested")
                .priority("LOW")
                .status("PENDING")
                .build());

        // Mess template
        registerTemplate("MESS_QUALITY", ComplaintBuilder.builder()
                .type("Mess")
                .department("Mess")
                .description("Food hygiene or quality concern in hostel dining mess")
                .priority("MEDIUM")
                .status("PENDING")
                .build());

        // Emergency templates
        registerTemplate("EMERGENCY_MEDICAL", ComplaintBuilder.builder()
                .type("Emergency")
                .department("Emergency")
                .description("Urgent medical emergency: immediate first aid / doctor required")
                .priority("EMERGENCY")
                .status("PENDING")
                .build());

        registerTemplate("EMERGENCY_FIRE", ComplaintBuilder.builder()
                .type("Emergency")
                .department("Emergency")
                .description("Suspected electrical short circuit or fire safety hazard")
                .priority("EMERGENCY")
                .status("PENDING")
                .build());
    }

    /**
     * Registers a new complaint prototype in the registry.
     */
    public void registerTemplate(String key, Complaint prototype) {
        if (key != null && prototype != null) {
            prototypes.put(key.toUpperCase(), prototype);
        }
    }

    /**
     * Checks if a template key is registered.
     */
    public boolean hasTemplate(String key) {
        return key != null && prototypes.containsKey(key.toUpperCase());
    }

    /**
     * Retrieves a fresh, independent clone of the requested template.
     * Guaranteed to have reset identity (id=0), default PENDING status,
     * and independent mutable state.
     */
    public Complaint getTemplate(String key) {
        if (key == null) return null;
        Complaint prototype = prototypes.get(key.toUpperCase());
        return prototype != null ? prototype.clone() : null;
    }

    /**
     * Clones the requested template and specializes it for a student, room, and optional custom note.
     */
    public Complaint createFromTemplate(String key, int studentId, String roomNo, String customDescription) {
        Complaint clone = getTemplate(key);
        if (clone == null) {
            throw new IllegalArgumentException("Unknown complaint template key: " + key);
        }
        return clone.cloneWith(studentId, roomNo, customDescription);
    }

    /**
     * Returns an unmodifiable list of all registered template keys.
     */
    public List<String> getAvailableTemplateKeys() {
        List<String> keys = new ArrayList<>(prototypes.keySet());
        Collections.sort(keys);
        return Collections.unmodifiableList(keys);
    }

    /**
     * Returns total number of registered templates.
     */
    public int getTemplateCount() {
        return prototypes.size();
    }
}
