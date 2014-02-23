package me.anomalousrei.infected.listener;

import me.anomalousrei.infected.Infected;
import me.anomalousrei.infected.Storage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MapListener implements Listener {
    Infected plugin;

    public MapListener(Infected pl) {
        plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (Storage.roundStatus.equals("Cycling")) {
            e.setCancelled(true);
            return;
        }
        if (Storage.noBreak.contains(Storage.currentRound)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (Storage.roundStatus.equals("Cycling")) {
            e.setCancelled(true);
            return;
        }
        if (Storage.noPlace.contains(Storage.currentRound)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void drops(PlayerDeathEvent e) {
        if (Storage.roundStatus.equals("Cycling")) {
            e.getDrops().clear();
            return;
        }
        if (Storage.noDrops.contains(Storage.currentRound)) {
            e.getDrops().clear();
        }
    }
}
