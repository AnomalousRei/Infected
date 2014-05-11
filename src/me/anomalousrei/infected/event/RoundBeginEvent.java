package me.anomalousrei.infected.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RoundBeginEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public RoundBeginEvent() {
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
