package me.AnomalousRei.infected.listener;

import me.AnomalousRei.infected.Infected;
import me.AnomalousRei.infected.Storage;
import me.AnomalousRei.infected.event.PlayerInfectEvent;
import me.AnomalousRei.infected.object.IPlayer;
import me.AnomalousRei.infected.util.Gamemode;
import me.AnomalousRei.infected.util.Team;
import me.AnomalousRei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

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
