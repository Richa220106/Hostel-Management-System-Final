package com.hostelhub.factory;

public class MessComplaint extends Complaint {
    public MessComplaint(int studentId, String description, String priority) {
        super("Mess", studentId, "", description, priority);
    }

    public MessComplaint(int studentId, String roomNo, String description, String priority) {
        super("Mess", studentId, roomNo, description, priority);
    }
}
