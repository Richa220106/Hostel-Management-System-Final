package com.hostelhub.model;

public class Student extends User {

    public Student() {
        super();
        this.setRole("STUDENT");
    }

    public Student(int id, String username, String password, String name, String roomNo) {
        super(id, username, password, "STUDENT", name, roomNo, "", "");
    }

    public Student(int id, String username, String password, String name, String roomNo, String contact, String email) {
        super(id, username, password, "STUDENT", name, roomNo, contact, email);
    }
}
