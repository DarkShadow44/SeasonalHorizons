package com.darkshadow44.seasonalhorizons.season;

public enum MainSeason {
    SPRING("spring"),
    SUMMER("summer"),
    AUTUMN("autumn"),
    WINTER("winter");

    private final String id;

    MainSeason(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
