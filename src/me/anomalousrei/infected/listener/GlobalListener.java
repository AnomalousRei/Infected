package me.anomalousrei.infected.listener;

import me.anomalousrei.infected.Infected;
import me.anomalousrei.infected.Storage;
import me.anomalousrei.infected.event.PlayerInfectEvent;
import me.anomalousrei.infected.handlers.SQLHandler;
import me.anomalousrei.infected.object.IPlayer;
import me.anomalousrei.infected.util.Gamemode;
import me.anomalousrei.infected.util.Team;
import me.anomalousrei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class GlobalListener implements Listener {
    Infected plugin;

    public GlobalListener(Infected pl) {
        plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        IPlayer.craftIPlayer(event.getPlayer());
        event.getPlayer().setScoreboard(Utility.scoreboard);
        if (Storage.roundStatus.equals("Starting")) {
            IPlayer.getIPlayer(event.getPlayer()).setTeam(Team.HUMAN);
        } else if (Storage.roundStatus.equals("Started")) {
            IPlayer.getIPlayer(event.getPlayer()).setTeam(Team.ZOMBIE);
        }
        if (Storage.spawns.size() > 0) {
            int x = Storage.spawns.get(Storage.currentRound).getBlockX();
            int y = Storage.spawns.get(Storage.currentRound).getBlockY();
            int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
            event.getPlayer().teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
            Utility.handKit(event.getPlayer());
            Utility.updateDisplayName(event.getPlayer());
            Utility.updateScoreboard();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Infected.iPlayers.remove(event.getPlayer().getName());
        if (Storage.roundStatus.equals("Started")) Utility.checkZombie();
        Utility.updateScoreboard();
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if (Storage.roundStatus.equals("Cycling")) return;
        if (Storage.currentGamemode.equals(Gamemode.CLASSIC) || Storage.currentGamemode.equals(Gamemode.TIMED_CLASSIC))
            event.setCancelled(true);
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (IPlayer.getIPlayer((Player) event.getEntity()).getTeam().equals(IPlayer.getIPlayer((Player) event.getDamager()).getTeam()))
                event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (IPlayer.getIPlayer((Player) event.getEntity()).getTeam().equals(IPlayer.getIPlayer((Player) arrow.getShooter()).getTeam()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        if (!Storage.roundStatus.equals("Cycling")) {
            Player p = event.getPlayer();
            int x = Storage.spawns.get(Storage.currentRound).getBlockX();
            int y = Storage.spawns.get(Storage.currentRound).getBlockY();
            int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
            event.setRespawnLocation(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
            Utility.handKit(p);
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        if (Storage.roundStatus.equals("Cycling")) {
            event.getEntity().setHealth(20);
            return;
        }
        try {
            if (event.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                if (event.getEntity().getKiller() != null) {
                    event.setDeathMessage(event.getEntity().getDisplayName() + ChatColor.YELLOW + Storage.getRandomDeathMessage() + event.getEntity().getKiller().getDisplayName());
                }
            }
        } catch (NullPointerException ex) {
            event.setDeathMessage(event.getEntity().getDisplayName() + ChatColor.YELLOW + " died");
        }
        if (IPlayer.getIPlayer(event.getEntity()).getTeam().equals(Team.HUMAN))
            Bukkit.getPluginManager().callEvent(new PlayerInfectEvent(IPlayer.getIPlayer(event.getEntity().getKiller()), IPlayer.getIPlayer(event.getEntity())));
        Utility.updateDisplayName(event.getEntity());
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        if (event.getTo().getBlockY() <= 0) {
            Utility.teleportToSpawn(event.getPlayer());
            if (IPlayer.getIPlayer(event.getPlayer()).getTeam().equals(Team.HUMAN) && Storage.roundStatus.equals("Started")) {
                event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
                IPlayer.getIPlayer(event.getPlayer()).setTeam(Team.ZOMBIE);
                Utility.checkZombie();
                SQLHandler.logKill("VOID", event.getPlayer().getName(), Storage.currentRound, Storage.currentGamemode.toString());
            } else if (Storage.roundStatus.equals("Cycling")) {
                Utility.handKit(event.getPlayer());
            }
        }
        if (Storage.roundStatus.equals("Cycling")) return;
        if (Storage.currentGamemode.equals(Gamemode.PVP) || Storage.currentGamemode.equals(Gamemode.TIMED_PVP)) return;
        if (Storage.roundStatus.equals("Started") && IPlayer.getIPlayer(event.getPlayer().getName()).getTeam().equals(Team.ZOMBIE))
            for (Entity entity : event.getPlayer().getNearbyEntities(0.5, 0.5, 0.5)) {
                if (entity instanceof Player) {
                    Player p = (Player) entity;
                    if (IPlayer.getIPlayer(p.getName()).getTeam().equals(Team.HUMAN)) {
                        Bukkit.getPluginManager().callEvent(new PlayerInfectEvent(IPlayer.getIPlayer(event.getPlayer().getName()), IPlayer.getIPlayer(p.getName())));
                        Bukkit.broadcastMessage(p.getDisplayName() + ChatColor.YELLOW + Storage.getRandomDeathMessage() + event.getPlayer().getDisplayName());
                    }
                }
            }
    }

    @EventHandler
    public void tag(PlayerReceiveNameTagEvent event) {
        if (IPlayer.getIPlayer(event.getNamedPlayer()).getTeam().equals(Team.HUMAN)) {
            event.setTag(ChatColor.GREEN + event.getNamedPlayer().getName());
        } else if (IPlayer.getIPlayer(event.getNamedPlayer()).getTeam().equals(Team.ZOMBIE)) {
            event.setTag(ChatColor.DARK_RED + event.getNamedPlayer().getName());
        } else if (IPlayer.getIPlayer(event.getNamedPlayer()).getTeam().equals(Team.OBSERVER)) {
            event.setTag(ChatColor.AQUA + event.getNamedPlayer().getName());
        }
    }
}
