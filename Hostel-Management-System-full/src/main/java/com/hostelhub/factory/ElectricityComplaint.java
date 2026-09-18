package com.hostelhub.factory;

public class ElectricityComplaint extends Complaint {
    public ElectricityComplaint(int studentId, String description, String priority) {
        super("Electricity", studentId, "", description, priority);
    }

    public ElectricityComplaint(int studentId, String roomNo, String description, String priority) {
        super("Electricity", studentId, roomNo, description, priority);
    }
}
