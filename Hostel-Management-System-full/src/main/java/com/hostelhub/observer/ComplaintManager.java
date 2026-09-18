package com.hostelhub.observer;

import java.util.ArrayList;
import java.util.List;

public class ComplaintManager {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String message, int targetUserId) {
        for (Observer observer : observers) {
            observer.update(message, targetUserId);
        }
    }

    public void onComplaintStatusChanged(int complaintId, String newStatus, int studentId, String customMessage) {
        String msg = customMessage != null ? customMessage : ("Complaint #" + complaintId + " status changed to: " + newStatus);
        System.out.println("Subject: Notifying observers - " + msg);
        
        // Notify the student
        notifyObservers(msg, studentId);
        
        // Notify the chief warden (User ID 1)
        notifyObservers("Hostel Update: " + msg, 1);
    }

    public void onComplaintStatusChanged(int complaintId, String newStatus, int studentId) {
        onComplaintStatusChanged(complaintId, newStatus, studentId, null);
    }
}
