package com.hostelhub.factory;

public class MaintenanceComplaint extends Complaint {
    public MaintenanceComplaint(int studentId, String description, String priority) {
        super("Maintenance", studentId, "", description, priority);
    }

    public MaintenanceComplaint(int studentId, String roomNo, String description, String priority) {
        super("Maintenance", studentId, roomNo, description, priority);
    }
}
