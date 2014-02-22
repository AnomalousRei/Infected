package me.AnomalousRei.infected.util;

public enum Team {
    OBSERVER,
    HUMAN,
    ZOMBIE;

    public Team parseTeam(String s){
        if(s.equalsIgnoreCase("OBSERVER")) return OBSERVER;
        if(s.equalsIgnoreCase("HUMAN")) return HUMAN;
        if(s.equalsIgnoreCase("ZOMBIE")) return ZOMBIE;
        return null;
    }
}
