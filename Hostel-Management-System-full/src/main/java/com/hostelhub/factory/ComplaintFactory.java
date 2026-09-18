package com.hostelhub.factory;

public class ComplaintFactory {

    public static Complaint createComplaint(String type, int studentId, String roomNo, String description, String priority) {
        if (type == null) {
            return null;
        }
        
        switch (type.trim().toLowerCase()) {
            case "maintenance":
                return new MaintenanceComplaint(studentId, roomNo, description, priority);
            case "electricity":
                return new ElectricityComplaint(studentId, roomNo, description, priority);
            case "water":
                return new WaterComplaint(studentId, roomNo, description, priority);
            case "mess":
                return new MessComplaint(studentId, roomNo, description, priority);
            case "emergency":
                return new EmergencyRequest(studentId, roomNo, description);
            default:
                throw new IllegalArgumentException("Unknown complaint type: " + type);
        }
    }

    public static Complaint createComplaint(String type, int studentId, String description, String priority) {
        return createComplaint(type, studentId, "", description, priority);
    }
}
