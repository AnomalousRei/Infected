package me.anomalousrei.infected.event;

import me.anomalousrei.infected.object.IPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerInfectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    IPlayer infector = null;
    IPlayer infected = null;

    public PlayerInfectEvent(IPlayer infector, IPlayer infected) {
        this.infector = infector;
        this.infected = infected;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public IPlayer getInfector() {
        return infector;
    }

    public IPlayer getInfected() {
        return infected;
    }
}
