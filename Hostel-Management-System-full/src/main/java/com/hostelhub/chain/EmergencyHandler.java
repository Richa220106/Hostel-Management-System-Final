package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;

public class EmergencyHandler extends ComplaintHandler {
    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getType().equalsIgnoreCase("Emergency") || 
               complaint.getPriority().equalsIgnoreCase("EMERGENCY") ||
               complaint.getPriority().equalsIgnoreCase("CRITICAL");
    }

    @Override
    protected void handle(Complaint complaint) {
        System.out.println("!!! EmergencyHandler is processing an EMERGENCY request !!!");
        complaint.setDepartment("Security");
        assignTechnician(complaint, "Security");
    }
}
