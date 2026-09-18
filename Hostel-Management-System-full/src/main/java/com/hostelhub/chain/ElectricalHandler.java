package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;

public class ElectricalHandler extends ComplaintHandler {
    @Override
    protected boolean canHandle(Complaint complaint) {
        String type = complaint.getType() != null ? complaint.getType().toLowerCase() : "";
        String desc = complaint.getDescription() != null ? complaint.getDescription().toLowerCase() : "";
        return type.equals("electricity") || type.equals("electrical") || 
               desc.contains("ac") || desc.contains("air conditioner") || 
               desc.contains("electrical") || desc.contains("light") || 
               desc.contains("fan") || desc.contains("power") || desc.contains("switch");
    }

    @Override
    protected void handle(Complaint complaint) {
        System.out.println("ElectricalHandler is processing the electrical/AC complaint.");
        assignTechnician(complaint, "Electrical");
    }
}
