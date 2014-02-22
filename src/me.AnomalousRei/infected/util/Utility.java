package me.AnomalousRei.infected.util;

import me.AnomalousRei.infected.Infected;
import me.AnomalousRei.infected.Storage;
import me.AnomalousRei.infected.event.RoundEndEvent;
import me.AnomalousRei.infected.object.IPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.kitteh.tag.TagAPI;

import java.util.*;

public class Utility {

    public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    public static void updateScoreboard() {
        Objective o = scoreboard.getObjective("Player Count");
        Score zombies = o.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Zombies: "));
        Score humans = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Humans: "));
        zombies.setScore(zombieCount());
        humans.setScore(humanCount());
    }

    private static int zombieCount() {
        int count = 0;
        for (IPlayer p : Infected.iPlayers.values()) {
            if (p.getTeam() == Team.ZOMBIE) count++;
        }
        return count;
    }

    private static int humanCount() {
        int count = 0;
        for (IPlayer p : Infected.iPlayers.values()) {
            if (p.getTeam() == Team.HUMAN) count++;
        }
        return count;
    }

    public static void handKit(Player p, String map) {
        p.getInventory().clear();
        p.setGameMode(GameMode.SURVIVAL);
        p.getInventory().setHelmet(new ItemStack(Material.AIR));
        p.getInventory().setChestplate(new ItemStack(Material.AIR));
        p.getInventory().setLeggings(new ItemStack(Material.AIR));
        p.getInventory().setBoots(new ItemStack(Material.AIR));
        p.setFoodLevel(20);
        p.setHealth(20);
        HashMap<Integer, ItemStack> temp = Storage.kits.get(map);
        Iterator iterator = temp.entrySet().iterator();
        /* iterate through the hashmap to set the stuff to their slots */
        while (iterator.hasNext()) {
            Map.Entry<Integer, ItemStack> it = (Map.Entry<Integer, ItemStack>) iterator.next();
            ItemStack i = it.getValue();
            int slot = it.getKey();
            if (slot == -1) {
                p.getInventory().setHelmet(i);
            } else if (slot == -2) {
                p.getInventory().setChestplate(i);
            } else if (slot == -3) {
                p.getInventory().setLeggings(i);
            } else if (slot == -4) {
                p.getInventory().setBoots(i);
            } else {
                p.getInventory().setItem(slot, i);
            }
        }
    }

    public static void handKit(Player p) {
        handKit(p, Storage.currentRound);
    }

    public static void updateDisplayName(Player p) {
        TagAPI.refreshPlayer(p);
        String name = p.getName();
        ChatColor c = teamColor(IPlayer.getIPlayer(p).getTeam());

        if (p.getName().length() > 14) {
            String newTabName = p.getName().substring(0, 14);
            p.setPlayerListName(c + newTabName);
        } else {
            p.setPlayerListName(c + name);
        }

        p.setDisplayName(name);
        p.setDisplayName(c + name + ChatColor.RESET);
        addPrefix(p);

    }

    private static void addPrefix(Player p) {

        if (p.hasPermission("infected.rank.donator") && !p.hasPermission("infected.rank.admin")) {
            p.setDisplayName(ChatColor.DARK_GREEN + "*" + p.getDisplayName());
        }

        if (p.hasPermission("infected.rank.mod") && !p.hasPermission("infected.rank.admin")) {
            p.setDisplayName(ChatColor.DARK_RED + "*" + p.getDisplayName());
        }

        if (p.hasPermission("infected.rank.admin")) {
            p.setDisplayName(ChatColor.GOLD + "*" + p.getDisplayName());
        }

        if (Storage.currentCreators.contains(p.getName())) {
            p.setDisplayName(ChatColor.BLUE + "*" + p.getDisplayName());
        }

    }

    private static ChatColor teamColor(Team t) {
        if (t.equals(Team.HUMAN)) return ChatColor.GREEN;
        if (t.equals(Team.ZOMBIE)) return ChatColor.DARK_RED;
        if (t.equals(Team.OBSERVER)) return ChatColor.AQUA;
        return ChatColor.WHITE;
    }

    public static String sentenceFormat(ArrayList<String> array) {
        String format = "";
        if (array.size() == 1) return array.get(0);
        int i = 1;
        while (i <= array.size()) {
            if (i == array.size()) {
                format = format + " and " + array.get(i - 1);
            } else if (i == 1) {
                format = array.get(0);
            } else {
                format = format + ", " + array.get(i - 1);
            }
            i++;
        }
        return format;
    }

    public static long generateID() {
        return new Random().nextInt(90000) + 10000;
    }

    public static void checkZombie() {
        if (zombieCount() == 0 && humanCount() > 0) {
            ArrayList<IPlayer> players = new ArrayList<IPlayer>();
            players.addAll(Infected.iPlayers.values());
            IPlayer p = players.get(new Random().nextInt(players.size()));
            Bukkit.broadcastMessage(p.getName());
            IPlayer.getIPlayer(p).setTeam(Team.ZOMBIE);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The disease continues with " + p.getDisplayName() + ChatColor.YELLOW + "!");
        }
        checkEndRound();
    }

    public static void checkEndRound() {
        if (Storage.roundStatus.equals("Started")) {
            if (Storage.currentGamemode == Gamemode.TIMED_PVP || Storage.currentGamemode == Gamemode.TIMED_CLASSIC) {
                if (humanCount() == 0) {
                    Bukkit.getPluginManager().callEvent(new RoundEndEvent(Team.ZOMBIE));
                }
            }
            if (Storage.currentGamemode == Gamemode.CLASSIC || Storage.currentGamemode == Gamemode.PVP) {
                if (humanCount() == 1) {
                    Bukkit.getPluginManager().callEvent(new RoundEndEvent(Team.HUMAN));
                }
                if (humanCount() == 0) {
                    Bukkit.getPluginManager().callEvent(new RoundEndEvent(Team.ZOMBIE));
                }
            }
        }
    }

    public static IPlayer getHuman() {
        for (IPlayer p : Infected.iPlayers.values()) {
            if (p.getTeam() == Team.HUMAN) return p;
        }
        return null;
    }

    public static void runEndRound() {
        /*Bukkit.getScheduler().runTaskLater(Infected.getInstance(), new Runnable);*/
    }

    public static void respawn() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                Object nmsPlayer = p.getClass().getMethod("getHandle").invoke(p);
                Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").newInstance();
                Class<?> enumClass = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");

                for (Object ob : enumClass.getEnumConstants()) {
                    if (ob.toString().equals("PERFORM_RESPAWN")) {
                        packet = packet.getClass().getConstructor(enumClass).newInstance(ob);
                    }
                }

                Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
                con.getClass().getMethod("a", packet.getClass()).invoke(con, packet);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void teleportToSpawn(Player p) {
        int x = Storage.spawns.get(Storage.currentRound).getBlockX();
        int y = Storage.spawns.get(Storage.currentRound).getBlockY();
        int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
        p.teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
    }
}
