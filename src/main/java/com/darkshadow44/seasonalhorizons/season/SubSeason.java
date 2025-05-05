package com.darkshadow44.seasonalhorizons.season;

public enum SubSeason {
    EARLY("early"),
    MID("mid"),
    LATE("late");

    private final String id;

    SubSeason(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
