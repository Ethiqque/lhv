package com.danielpyld.lhv.entity;

import java.time.Instant;

public abstract class Event implements Comparable<Event> {
    private final Instant timestamp;

    public Event(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Event other) {
        return this.timestamp.compareTo(other.timestamp);
    }
}
