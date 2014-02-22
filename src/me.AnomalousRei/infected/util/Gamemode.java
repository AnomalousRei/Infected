package me.AnomalousRei.infected.util;

public enum Gamemode {
    CLASSIC,
    PVP,
    TIMED_CLASSIC,
    TIMED_PVP;

    public static String toFormat(Gamemode g) {
        if (g == CLASSIC) return "Last Human Classic Infected";
        if (g == PVP) return "Last Human Fighting Infected";
        if (g == TIMED_PVP) return "Survival Fighting Infected";
        if (g == TIMED_CLASSIC) return "Survival Classic Infected";
        return "Unspecified";
    }
}
