package com.hostelhub.model;

public class Warden extends User {

    public Warden() {
        super();
        this.setRole("WARDEN");
    }

    public Warden(int id, String username, String password, String name) {
        super(id, username, password, "WARDEN", name);
    }
}
