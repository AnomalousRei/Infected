package me.AnomalousRei.infected.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public RoundStartEvent() {
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
