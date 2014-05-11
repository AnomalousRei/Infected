package me.anomalousrei.infected.util;

public enum Team {
    OBSERVER,
    HUMAN,
    ZOMBIE;

    public Team parseTeam(String string) {
        if (string.equalsIgnoreCase("OBSERVER")) return OBSERVER;
        if (string.equalsIgnoreCase("HUMAN")) return HUMAN;
        if (string.equalsIgnoreCase("ZOMBIE")) return ZOMBIE;
        return null;
    }
}