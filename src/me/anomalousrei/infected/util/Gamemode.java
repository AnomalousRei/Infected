package me.anomalousrei.infected.util;

public enum Gamemode {
    CLASSIC,
    PVP,
    TIMED_CLASSIC,
    TIMED_PVP;

    public static String toFormat(Gamemode gamemode) {
        if (gamemode.equals(CLASSIC)) return "Last Human Classic Infected";
        if (gamemode.equals(PVP)) return "Last Human Fighting Infected";
        if (gamemode.equals(TIMED_PVP)) return "Survival Fighting Infected";
        if (gamemode.equals(TIMED_CLASSIC)) return "Survival Classic Infected";
        return "Unspecified";
    }
}
