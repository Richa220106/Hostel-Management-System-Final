package com.hostelhub.model;

public class Room {
    private String roomNo;
    private String block;
    private int capacity;
    private int currentOccupancy;
    private String status;

    public Room() {}

    public Room(String roomNo, String block, int capacity, int currentOccupancy, String status) {
        this.roomNo = roomNo;
        this.block = block;
        this.capacity = capacity;
        this.currentOccupancy = currentOccupancy;
        this.status = status;
    }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAvailable() {
        return Math.max(0, capacity - currentOccupancy);
    }
    
    public boolean isFull() {
        return currentOccupancy >= capacity;
    }
}
