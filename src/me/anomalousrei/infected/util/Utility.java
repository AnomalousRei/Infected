package me.anomalousrei.infected.util;

import me.anomalousrei.infected.Infected;
import me.anomalousrei.infected.Storage;
import me.anomalousrei.infected.event.RoundEndEvent;
import me.anomalousrei.infected.object.IPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.kitteh.tag.TagAPI;

import java.util.*;

public class Utility {

    public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    public static void updateScoreboard() {
        Objective objective = scoreboard.getObjective("Player Count");
        Score zombies = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Zombies: "));
        Score humans = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Humans: "));
        zombies.setScore(zombieCount());
        humans.setScore(humanCount());
    }

    private static int zombieCount() {
        int count = 0;
        for (IPlayer iPlayer : Infected.iPlayers.values()) {
            if (iPlayer.getTeam().equals(Team.ZOMBIE)) count++;
        }
        return count;
    }

    private static int humanCount() {
        int count = 0;
        for (IPlayer iPlayer : Infected.iPlayers.values()) {
            if (iPlayer.getTeam().equals(Team.HUMAN)) count++;
        }
        return count;
    }

    public static void handKit(Player player, String map) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
        player.setFoodLevel(20);
        player.setHealth(20);

        HashMap<Integer, ItemStack> temp = Storage.kits.get(map);
        Iterator iterator = temp.entrySet().iterator();
        // Iterate through the HashMap to set the items to their slots
        while (iterator.hasNext()) {
            Map.Entry<Integer, ItemStack> it = (Map.Entry<Integer, ItemStack>) iterator.next();
            ItemStack i = it.getValue();
            int slot = it.getKey();
            if (slot == -1) {
                player.getInventory().setHelmet(i);
            } else if (slot == -2) {
                player.getInventory().setChestplate(i);
            } else if (slot == -3) {
                player.getInventory().setLeggings(i);
            } else if (slot == -4) {
                player.getInventory().setBoots(i);
            } else {
                player.getInventory().setItem(slot, i);
            }
        }
    }

    public static void handKit(Player player) {
        handKit(player, Storage.currentRound);
    }

    public static void updateDisplayName(Player player) {
        TagAPI.refreshPlayer(player);
        String name = player.getName();
        ChatColor chatColor = teamColor(IPlayer.getIPlayer(player).getTeam());

        if (player.getName().length() > 14) {
            String newTabName = player.getName().substring(0, 14);
            player.setPlayerListName(chatColor + newTabName);
        } else {
            player.setPlayerListName(chatColor + name);
        }

        player.setDisplayName(name);
        player.setDisplayName(chatColor + name + ChatColor.RESET);
        addPrefix(player);

    }

    private static void addPrefix(Player player) {

        if (player.hasPermission("infected.rank.donator") && !player.hasPermission("infected.rank.admin")) {
            player.setDisplayName(ChatColor.DARK_GREEN + "*" + player.getDisplayName());
        }

        if (player.hasPermission("infected.rank.mod") && !player.hasPermission("infected.rank.admin")) {
            player.setDisplayName(ChatColor.DARK_RED + "*" + player.getDisplayName());
        }

        if (player.hasPermission("infected.rank.admin")) {
            player.setDisplayName(ChatColor.GOLD + "*" + player.getDisplayName());
        }

        if (Storage.currentCreators.contains(player.getName())) {
            player.setDisplayName(ChatColor.BLUE + "*" + player.getDisplayName());
        }

    }

    private static ChatColor teamColor(Team team) {
        if (team.equals(Team.HUMAN)) return ChatColor.GREEN;
        if (team.equals(Team.ZOMBIE)) return ChatColor.DARK_RED;
        if (team.equals(Team.OBSERVER)) return ChatColor.AQUA;
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
            IPlayer iPlayer = players.get(new Random().nextInt(players.size()));
            Bukkit.broadcastMessage(iPlayer.getName());
            IPlayer.getIPlayer(iPlayer).setTeam(Team.ZOMBIE);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "The disease continues with " + iPlayer.getDisplayName() + ChatColor.YELLOW + "!");
        }
        checkEndRound();
    }

    public static void checkEndRound() {
        if (Storage.roundStatus.equals("Started")) {
            if (Storage.currentGamemode.equals(Gamemode.TIMED_PVP) || Storage.currentGamemode.equals(Gamemode.TIMED_CLASSIC)) {
                if (humanCount() == 0) {
                    Bukkit.getPluginManager().callEvent(new RoundEndEvent(Team.ZOMBIE));
                }
            }
            if (Storage.currentGamemode.equals(Gamemode.CLASSIC) || Storage.currentGamemode.equals(Gamemode.PVP)) {
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
        for (IPlayer iPlayer : Infected.iPlayers.values()) {
            if (iPlayer.getTeam().equals(Team.HUMAN)) return iPlayer;
        }
        return null;
    }

    public static void runEndRound() {
        // Bukkit.getScheduler().runTaskLater(Infected.getInstance(), new Runnable);
    }

    public static void respawn() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object packet = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".PacketPlayInClientCommand").newInstance();
                Class<?> enumClass = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EnumClientCommand");

                for (Object object : enumClass.getEnumConstants()) {
                    if (object.toString().equals("PERFORM_RESPAWN")) {
                        packet = packet.getClass().getConstructor(enumClass).newInstance(object);
                    }
                }

                Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
                connection.getClass().getMethod("a", packet.getClass()).invoke(connection, packet);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void teleportToSpawn(Player player) {
        int x = Storage.spawns.get(Storage.currentRound).getBlockX();
        int y = Storage.spawns.get(Storage.currentRound).getBlockY();
        int z = Storage.spawns.get(Storage.currentRound).getBlockZ();
        player.teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
    }
}
