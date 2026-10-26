package com.hostelhub.memento;

import com.hostelhub.factory.Complaint;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memento Pattern: ComplaintHistory (Caretaker)
 * Keeps track of past ComplaintMemento snapshots and manages save/restore operations.
 * Supports both per-complaint history stacks and a global undo stack.
 */
public class ComplaintHistory {

    // Per-complaint memento history stack
    private final Map<Integer, Deque<ComplaintMemento>> complaintHistories = new ConcurrentHashMap<>();

    // Global undo history stack across all complaints
    private final Deque<ComplaintMemento> globalHistory = new ArrayDeque<>();

    public ComplaintHistory() {
    }

    /**
     * Saves a memento to history.
     */
    public synchronized void push(ComplaintMemento memento) {
        if (memento == null) return;
        complaintHistories
                .computeIfAbsent(memento.getComplaintId(), k -> new ArrayDeque<>())
                .push(memento);
        globalHistory.push(memento);
    }

    /**
     * Helper to snapshot and save a complaint's current state.
     */
    public synchronized void saveState(Complaint complaint) {
        if (complaint == null) return;
        push(complaint.saveState());
    }

    /**
     * Pops and returns the most recent saved memento for a specific complaint.
     */
    public synchronized ComplaintMemento pop(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.get(complaintId);
        if (history != null && !history.isEmpty()) {
            ComplaintMemento memento = history.pop();
            globalHistory.remove(memento);
            return memento;
        }
        return null;
    }

    /**
     * Pops and returns the most recent saved memento globally.
     */
    public synchronized ComplaintMemento pop() {
        if (!globalHistory.isEmpty()) {
            ComplaintMemento memento = globalHistory.pop();
            Deque<ComplaintMemento> history = complaintHistories.get(memento.getComplaintId());
            if (history != null) {
                history.remove(memento);
            }
            return memento;
        }
        return null;
    }

    /**
     * Peeks at the latest saved memento for a specific complaint without removing it.
     */
    public synchronized ComplaintMemento peek(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.get(complaintId);
        if (history != null && !history.isEmpty()) {
            return history.peek();
        }
        return null;
    }

    /**
     * Checks if history exists for a given complaint.
     */
    public synchronized boolean hasHistory(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.get(complaintId);
        return history != null && !history.isEmpty();
    }

    /**
     * Returns the number of saved states for a complaint.
     */
    public synchronized int getHistoryCount(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.get(complaintId);
        return history != null ? history.size() : 0;
    }

    /**
     * Returns an unmodifiable list of all mementos for a complaint (most recent first).
     */
    public synchronized List<ComplaintMemento> getHistory(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.get(complaintId);
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    /**
     * Clears history for a specific complaint.
     */
    public synchronized void clear(int complaintId) {
        Deque<ComplaintMemento> history = complaintHistories.remove(complaintId);
        if (history != null) {
            globalHistory.removeAll(history);
        }
    }

    /**
     * Clears all history.
     */
    public synchronized void clear() {
        complaintHistories.clear();
        globalHistory.clear();
    }
}
