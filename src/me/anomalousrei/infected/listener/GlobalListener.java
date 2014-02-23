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
    public void join(PlayerJoinEvent e) {
        IPlayer.craftIPlayer(e.getPlayer());
        e.getPlayer().setScoreboard(Utility.scoreboard);
        if (Storage.roundStatus.equals("Starting")) {
            IPlayer.getIPlayer(e.getPlayer()).setTeam(Team.HUMAN);
        } else if (Storage.roundStatus.equals("Started")) {
            IPlayer.getIPlayer(e.getPlayer()).setTeam(Team.ZOMBIE);
        }
        int x = Storage.spawns.get(Storage.currentRound).getBlockX();
        int y = Storage.spawns.get(Storage.currentRound).getBlockY();
        int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
        e.getPlayer().teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
        Utility.handKit(e.getPlayer());
        Utility.updateDisplayName(e.getPlayer());
        Utility.updateScoreboard();
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        Infected.iPlayers.remove(e.getPlayer().getName());
        if (Storage.roundStatus.equals("Started")) Utility.checkZombie();
        Utility.updateScoreboard();
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        if (Storage.roundStatus.equals("Cycling")) return;
        if (Storage.currentGamemode.equals(Gamemode.CLASSIC) || Storage.currentGamemode.equals(Gamemode.TIMED_CLASSIC))
            e.setCancelled(true);
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            Player damager = (Player) e.getDamager();
            IPlayer idm = IPlayer.getIPlayer(damaged);
            IPlayer idmgr = IPlayer.getIPlayer(damager);
            if (idm.getTeam().equals(idmgr.getTeam())) e.setCancelled(true);
        }
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
            Player damaged = (Player) e.getEntity();
            Arrow a = (Arrow) e.getDamager();
            Player damager = (Player) a.getShooter();
            IPlayer idm = IPlayer.getIPlayer(damaged);
            IPlayer idmgr = IPlayer.getIPlayer(damager);
            if (idm.getTeam().equals(idmgr.getTeam())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent e) {
        if (!Storage.roundStatus.equals("Cycling")) {
            Player p = e.getPlayer();
            int x = Storage.spawns.get(Storage.currentRound).getBlockX();
            int y = Storage.spawns.get(Storage.currentRound).getBlockY();
            int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
            e.setRespawnLocation(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
            Utility.handKit(p);
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent e) {
        if (Storage.roundStatus.equals("Cycling")) {
            e.getEntity().setHealth(20);
            return;
        }
        try {
            if (e.getEntity().getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                if (e.getEntity().getKiller() != null) {
                    e.setDeathMessage(e.getEntity().getDisplayName() + ChatColor.YELLOW + Storage.randomDeathMessage() + e.getEntity().getKiller().getDisplayName());
                }
            }
        } catch (NullPointerException ex) {
            e.setDeathMessage(e.getEntity().getDisplayName() + ChatColor.YELLOW + " died");
        }
        if (IPlayer.getIPlayer(e.getEntity()).getTeam().equals(Team.HUMAN))
            Bukkit.getPluginManager().callEvent(new PlayerInfectEvent(IPlayer.getIPlayer(e.getEntity().getKiller()), IPlayer.getIPlayer(e.getEntity())));
        Utility.updateDisplayName(e.getEntity());
    }

    @EventHandler
    public void move(PlayerMoveEvent e) {
        if (e.getTo().getBlockY() <= 0) {
            Utility.teleportToSpawn(e.getPlayer());
            if (IPlayer.getIPlayer(e.getPlayer()).getTeam().equals(Team.HUMAN) && Storage.roundStatus.equals("Started")) {
                e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
                IPlayer.getIPlayer(e.getPlayer()).setTeam(Team.ZOMBIE);
                Utility.checkZombie();
                SQLHandler.logKill("VOID", e.getPlayer().getName(), Storage.currentRound, Storage.currentGamemode.toString());
            } else if (Storage.roundStatus.equals("Cycling")) {
                Utility.handKit(e.getPlayer());
            }
        }
        if (Storage.roundStatus.equals("Cycling")) return;
        if (Storage.currentGamemode.equals(Gamemode.PVP) || Storage.currentGamemode.equals(Gamemode.TIMED_PVP)) return;
        if (Storage.roundStatus.equals("Started") && IPlayer.getIPlayer(e.getPlayer().getName()).getTeam().equals(Team.ZOMBIE))
            for (Entity en : e.getPlayer().getNearbyEntities(0.5, 0.5, 0.5)) {
                if (en instanceof Player) {
                    Player p = (Player) en;
                    if (IPlayer.getIPlayer(p.getName()).getTeam().equals(Team.HUMAN)) {
                        Bukkit.getPluginManager().callEvent(new PlayerInfectEvent(IPlayer.getIPlayer(e.getPlayer().getName()), IPlayer.getIPlayer(p.getName())));
                        Bukkit.broadcastMessage(p.getDisplayName() + ChatColor.YELLOW + Storage.randomDeathMessage() + e.getPlayer().getDisplayName());
                    }
                }
            }
    }

    @EventHandler
    public void tag(PlayerReceiveNameTagEvent e) {
        if (IPlayer.getIPlayer(e.getNamedPlayer()).getTeam().equals(Team.HUMAN)) {
            e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
        } else if (IPlayer.getIPlayer(e.getNamedPlayer()).getTeam().equals(Team.ZOMBIE)) {
            e.setTag(ChatColor.DARK_RED + e.getNamedPlayer().getName());
        } else if (IPlayer.getIPlayer(e.getNamedPlayer()).getTeam().equals(Team.OBSERVER)) {
            e.setTag(ChatColor.AQUA + e.getNamedPlayer().getName());
        }
    }
}
