package com.hostelhub.factory;

public class WaterComplaint extends Complaint {
    public WaterComplaint(int studentId, String description, String priority) {
        super("Water", studentId, "", description, priority);
    }

    public WaterComplaint(int studentId, String roomNo, String description, String priority) {
        super("Water", studentId, roomNo, description, priority);
    }
}
