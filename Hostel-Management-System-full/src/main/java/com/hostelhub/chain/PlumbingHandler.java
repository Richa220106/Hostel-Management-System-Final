package com.hostelhub.chain;

import com.hostelhub.factory.Complaint;

public class PlumbingHandler extends ComplaintHandler {
    @Override
    protected boolean canHandle(Complaint complaint) {
        String type = complaint.getType().toLowerCase();
        String desc = complaint.getDescription().toLowerCase();
        return type.equals("water") || desc.contains("water") || desc.contains("leak") || desc.contains("pipe");
    }

    @Override
    protected void handle(Complaint complaint) {
        System.out.println("PlumbingHandler is processing the plumbing/water complaint.");
        assignTechnician(complaint, "Plumbing");
    }
}
