package me.anomalousrei.infected.util;

public enum Gamemode {
    CLASSIC,
    PVP,
    TIMED_CLASSIC,
    TIMED_PVP;

    public static String toFormat(Gamemode g) {
        if (g.equals(CLASSIC)) return "Last Human Classic Infected";
        if (g.equals(PVP)) return "Last Human Fighting Infected";
        if (g.equals(TIMED_PVP)) return "Survival Fighting Infected";
        if (g.equals(TIMED_CLASSIC)) return "Survival Classic Infected";
        return "Unspecified";
    }
}
