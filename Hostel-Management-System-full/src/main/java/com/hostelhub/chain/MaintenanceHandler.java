package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;

public class MaintenanceHandler extends ComplaintHandler {
    @Override
    protected boolean canHandle(Complaint complaint) {
        if (complaint.getType().equalsIgnoreCase("Maintenance")) {
            String desc = complaint.getDescription() != null ? complaint.getDescription().toLowerCase() : "";
            if (desc.contains("ac") || desc.contains("air conditioner") || desc.contains("electrical") || desc.contains("light") || desc.contains("fan") || desc.contains("power") || desc.contains("switch")) {
                System.out.println("MaintenanceHandler: Routing AC/Electrical issue to ElectricalHandler...");
                return false; // Route to ElectricalHandler
            }
            if (desc.contains("water") || desc.contains("leak") || desc.contains("pipe") || desc.contains("drain") || desc.contains("tap") || desc.contains("flush")) {
                System.out.println("MaintenanceHandler: Routing Plumbing issue to PlumbingHandler...");
                return false; // Route to PlumbingHandler
            }
            return true;
        }
        return false;
    }

    @Override
    protected void handle(Complaint complaint) {
        System.out.println("MaintenanceHandler is processing general maintenance complaint.");
        assignTechnician(complaint, "Maintenance");
    }
}
