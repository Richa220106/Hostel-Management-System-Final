package com.hostelhub.model;

public class Technician {
    private int id;
    private String name;
    private String department;
    private String status; // AVAILABLE, ASSIGNED
    private String contact;

    public Technician() {}

    public Technician(int id, String name, String department, String status) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.status = status;
        this.contact = "";
    }

    public Technician(int id, String name, String department, String status, String contact) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.status = status;
        this.contact = contact;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
