package me.AnomalousRei.infected.handlers;

import me.AnomalousRei.infected.Infected;
import me.AnomalousRei.infected.Storage;
import me.AnomalousRei.infected.event.PlayerInfectEvent;
import me.AnomalousRei.infected.event.RoundBeginEvent;
import me.AnomalousRei.infected.event.RoundEndEvent;
import me.AnomalousRei.infected.event.RoundStartEvent;
import me.AnomalousRei.infected.object.IPlayer;
import me.AnomalousRei.infected.util.Gamemode;
import me.AnomalousRei.infected.util.Team;
import me.AnomalousRei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class RoundHandler implements Listener {

    Infected plugin;

    public RoundHandler(Infected pl) {
        plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public static BukkitTask currentTask = null;

    public static void start(String map, long ID) {
        for (IPlayer p : Infected.iPlayers.values()) {
            p.setTeam(Team.HUMAN);
        }
        Storage.roundStatus = "Starting";
        Storage.currentRound = map;
        Storage.currentCreators.clear();
        Storage.currentCreators.addAll(Storage.creators.get(map));
        Storage.roundID = ID;
        Storage.currentGamemode = Storage.gameTypes.get(map);
        Bukkit.getPluginManager().callEvent(new RoundStartEvent());
    }

    public static void beginStartingTask() {
        if (currentTask != null) return;
        currentTask = Bukkit.getScheduler().runTaskTimer(Infected.getInstance(), new Runnable() {
            int count = 20;

            public void run() {
                if (count == 20 || count == 15 || count <= 5) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Round start: " + ChatColor.RED + count);
                    Utility.updateScoreboard();
                }
                count = count - 1;
            }
        }, 20L, 20L);
        Bukkit.getScheduler().runTaskLater(Infected.getInstance(), new Runnable() {
            @Override
            public void run() {
                currentTask.cancel();
                currentTask = null;
                Bukkit.getPluginManager().callEvent(new RoundBeginEvent());
            }
        }, 420L);
    }

    public static void timedRound(Gamemode gm) {

    }

    public static void cycleToNextMap() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        final long previousMapID = Storage.roundID;
        if (Storage.rotationPoint == Storage.maxRotationPoint - 1) {
            Storage.rotationPoint = 0;
        } else {
            Storage.rotationPoint++;
        }
        Storage.gTo = Storage.rotationList.get(Storage.rotationPoint);
        final long ID = Utility.generateID();
        Storage.roundID = ID;
        Storage.currentRound = Storage.gTo;
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The round is now over and the next map is about to begin!");
        Bukkit.broadcastMessage(ChatColor.GREEN + "Type " + ChatColor.GOLD + "/g " + Storage.rotationList.get(Storage.rotationPoint) + ChatColor.GREEN + " or wait 30 seconds to be transferred");
        MapHandler.loadMap(Storage.rotationList.get(Storage.rotationPoint), ID + "");
        Bukkit.getScheduler().runTaskLater(Infected.getInstance(), new Runnable() {
            @Override
            public void run() {
                start(Storage.rotationList.get(Storage.rotationPoint), ID);
                MapHandler.restoreMap(previousMapID + "");
                Storage.gTo = "None";
            }
        }, 600L);
    }

    @EventHandler
    public void start(RoundStartEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            IPlayer.getIPlayer(p).setTeam(Team.HUMAN);
            int x = Storage.spawns.get(Storage.currentRound).getBlockX();
            int y = Storage.spawns.get(Storage.currentRound).getBlockY();
            int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
            if (!p.getWorld().getName().equals(Storage.roundID + ""))
                p.teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
            Utility.handKit(p);
            Utility.updateDisplayName(p);
        }
        beginStartingTask();
        Utility.updateScoreboard();
    }

    @EventHandler
    public void begin(RoundBeginEvent e) {
        Bukkit.broadcastMessage(ChatColor.YELLOW + "The round has started!");
        Storage.roundStatus = "Started";
        Utility.updateScoreboard();
        if (Bukkit.getOnlinePlayers().length == 0) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "There weren't enough players!");
            Bukkit.getPluginManager().callEvent(new RoundEndEvent(Team.ZOMBIE));
            return;
        }
        Player p = Bukkit.getOnlinePlayers()[new Random().nextInt(Bukkit.getOnlinePlayers().length)];
        IPlayer.getIPlayer(p).setTeam(Team.ZOMBIE);
        p.getWorld().strikeLightningEffect(p.getLocation());
        Bukkit.broadcastMessage(p.getDisplayName() + ChatColor.YELLOW + " started the infection!");
        Utility.checkZombie();
    }

    @EventHandler
    public void infect(PlayerInfectEvent e) {
        e.getInfected().getWorld().strikeLightningEffect(e.getInfected().getLocation());
        e.getInfected().setTeam(Team.ZOMBIE);
        Utility.checkZombie();
        SQLHandler.logKill(e.getInfector().getName(), e.getInfected().getName(), Storage.currentRound, Storage.currentGamemode.toString());
    }

    @EventHandler
    public void end(RoundEndEvent e) {
        if (Storage.roundStatus.equals("Cycling")) return;
        Utility.respawn();
        if (currentTask != null) currentTask.cancel();
        currentTask = null;
        Storage.roundStatus = "Cycling";
        Storage.currentCreators.clear();
        Team winner = e.getWinner();
        Gamemode gm = Storage.currentGamemode;
        if (gm == Gamemode.CLASSIC || gm == Gamemode.PVP) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "|-------------------------");
            Bukkit.broadcastMessage(ChatColor.GRAY + "| The round is over!");
            Bukkit.broadcastMessage(ChatColor.GRAY + "| " + ChatColor.DARK_RED + "THE ZOMBIES HAVE TAKEN OVER!");
            if (Utility.getHuman() != null) {
                Bukkit.broadcastMessage(ChatColor.GRAY + "| " + ChatColor.GREEN + "Congratulations on " + Utility.getHuman().getDisplayName()
                        + ChatColor.GREEN + " for being the final survivor!");
            }
            Bukkit.broadcastMessage(ChatColor.GRAY + "|-------------------------");
            SQLHandler.logRound(Storage.currentRound, "ZOMBIES", Storage.currentGamemode.toString());
        }
        if (gm == Gamemode.TIMED_CLASSIC || gm == Gamemode.TIMED_PVP) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "|-------------------------");
            Bukkit.broadcastMessage(ChatColor.GRAY + "| The round is over!");
            if (winner == Team.HUMAN) {
                Bukkit.broadcastMessage(ChatColor.GRAY + "| " + ChatColor.GREEN + "THE HUMANS SURVIVED!");
            } else {
                Bukkit.broadcastMessage(ChatColor.GRAY + "| " + ChatColor.RED + "THE ZOMBIES KILLED ALL THE HUMANS!");
            }
            Bukkit.broadcastMessage(ChatColor.GRAY + "| " + ChatColor.GREEN + "Congratulations on " + Utility.getHuman().getDisplayName()
                    + ChatColor.GREEN + " for being the final survivor!");
            Bukkit.broadcastMessage(ChatColor.GRAY + "|-------------------------");
            SQLHandler.logRound(Storage.currentRound, "ZOMBIES", Storage.currentGamemode.toString());
        }
        for (IPlayer p : Infected.iPlayers.values()) {
            p.setTeam(Team.OBSERVER);
        }
        cycleToNextMap();
    }
}
