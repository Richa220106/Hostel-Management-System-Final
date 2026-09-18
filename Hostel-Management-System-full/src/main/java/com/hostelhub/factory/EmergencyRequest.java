package com.hostelhub.factory;

public class EmergencyRequest extends Complaint {
    public EmergencyRequest(int studentId, String description) {
        super("Emergency", studentId, "", description, "EMERGENCY");
    }

    public EmergencyRequest(int studentId, String roomNo, String description) {
        super("Emergency", studentId, roomNo, description, "EMERGENCY");
    }
}
