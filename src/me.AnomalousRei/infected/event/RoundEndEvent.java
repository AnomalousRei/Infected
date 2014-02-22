package me.AnomalousRei.infected.event;

import me.AnomalousRei.infected.util.Team;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    Team winner = Team.ZOMBIE;

    public RoundEndEvent(Team t) {
        winner = t;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Team getWinner() {
        return winner;
    }
}
