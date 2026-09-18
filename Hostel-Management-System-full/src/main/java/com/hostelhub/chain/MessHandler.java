package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;

public class MessHandler extends ComplaintHandler {
    @Override
    protected boolean canHandle(Complaint complaint) {
        return complaint.getType().equalsIgnoreCase("Mess");
    }

    @Override
    protected void handle(Complaint complaint) {
        System.out.println("MessHandler is processing the mess complaint.");
        assignTechnician(complaint, "Mess");
    }
}
