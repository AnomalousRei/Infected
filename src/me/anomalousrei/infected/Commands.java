package me.anomalousrei.infected;

import com.oresomecraft.campaign.database.MySQL;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import me.anomalousrei.infected.handlers.SQLHandler;
import me.anomalousrei.infected.util.Gamemode;
import me.anomalousrei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class Commands {
    Infected plugin;

    public Commands(Infected pl) {
        plugin = pl;
    }

    @Command(aliases = {"goto", "g"},
            desc = "Go to the next map",
            usage = "<map>",
            min = 1,
            max = 1)
    public void g(CommandContext args, CommandSender sender) {
        String map = args.getString(0);
        if (Storage.gTo.equals("None")) {
            sender.sendMessage(ChatColor.RED + "You cannot goto a map at the moment.");
            return;
        }
        if (matchMap(map).equals("None")) {
            sender.sendMessage(ChatColor.RED + "That map doesn't exist!");
            return;
        }
        if (!matchMap(map).equals(Storage.gTo)) {
            sender.sendMessage(ChatColor.RED + "That map isn't available at the moment.");
            return;
        }
        Player p = (Player) sender;
        int x = Storage.spawns.get(Storage.gTo).getBlockX();
        int y = Storage.spawns.get(Storage.gTo).getBlockY();
        int z = Storage.spawns.get(Storage.gTo).getBlockZ();
        if (!p.getWorld().getName().equals(Storage.roundID + "")) {
            p.teleport(new Location(Bukkit.getWorld(Storage.roundID + ""), x, y, z));
            Utility.handKit(p);
        }
    }

    @Command(aliases = {"stats", "kills", "statistics"},
            desc = "View a player's stats",
            usage = "<player>",
            min = 0,
            max = 1)
    public synchronized void stats(final CommandContext args, final CommandSender sender) throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String player = sender.getName();
                    if (args.argsLength() == 1) {
                        player = args.getString(0);
                    }
                    if (!SQLHandler.userExists(player)) {
                        sender.sendMessage(ChatColor.RED + "That user isn't in the database!");
                        return;
                    }

                    MySQL mysql = new MySQL(plugin.logger,
                            "[OnslaughtDB] ",
                            plugin.storageHostname,
                            plugin.storagePort,
                            plugin.storageDatabase,
                            plugin.storageUsername,
                            plugin.storagePassword);

                    mysql.open();
                    ResultSet rsk = mysql.query("SELECT COUNT(killer) FROM " + plugin.storageDatabase + "_kills WHERE killer='" + player + "'");
                    rsk.first();
                    int kills = rsk.getInt(1);

                    ResultSet rsd = mysql.query("SELECT COUNT(killer) FROM " + plugin.storageDatabase + "_kills WHERE killed='" + player + "'");
                    rsd.first();
                    int deaths = rsd.getInt(1);

                    sender.sendMessage(ChatColor.GRAY + "|-------------------------");
                    sender.sendMessage(ChatColor.GRAY + "| Infected stats for " + ChatColor.AQUA + player);
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.GREEN + "Kills: " + ChatColor.GRAY + kills);
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "Deaths: " + ChatColor.GRAY + deaths);
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.BLUE + "KD: " + ChatColor.GRAY + toKD(kills, deaths));
                    sender.sendMessage(ChatColor.GRAY + "|-------------------------");
                    mysql.close();
                } catch (SQLException e) {
                }
            }
        });
    }

    @Command(aliases = {"rotation", "rot"},
            desc = "View the current rotation",
            min = 0,
            max = 0)
    public void rotation(CommandContext args, CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "|-------- " + ChatColor.DARK_GRAY + "Rotation" + ChatColor.GRAY + "-------");
        Iterator iterator = Storage.rotationList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            i++;
            String color = "";
            if (i == Storage.rotationPoint + 1) color = ChatColor.GOLD + "";
            ArrayList<String> creators = Storage.creators.get(s);
            sender.sendMessage(ChatColor.GRAY + "| " + color + i + ". " + ChatColor.GREEN + s + " (" +
                    ChatColor.AQUA + ChatColor.ITALIC + "by " + Utility.sentenceFormat(creators) + ChatColor.RESET + ChatColor.GREEN + ")");
        }
        sender.sendMessage(ChatColor.GRAY + "|-----------------------");
    }

    @Command(aliases = {"map", "mapinfo"},
            desc = "View information on a map",
            usage = "<map>",
            min = 0,
            max = 1)
    public void map(final CommandContext args, final CommandSender sender) throws SQLException {
        Bukkit.getScheduler().runTaskAsynchronously(Infected.getInstance(), new Runnable() {
            public void run() {
                String map;
                if (args.argsLength() == 1) {
                    map = matchMap(args.getJoinedStrings(0));
                } else {
                    map = Storage.currentRound;
                }
                if (map.equals("None")) {
                    sender.sendMessage(ChatColor.RED + "That map doesn't exist!");
                    return;
                }


                MySQL mysql = new MySQL(plugin.logger,
                        "[OnslaughtDB] ",
                        plugin.storageHostname,
                        plugin.storagePort,
                        plugin.storageDatabase,
                        plugin.storageUsername,
                        plugin.storagePassword);

                mysql.open();

                ResultSet rsd = mysql.query("SELECT COUNT(map) FROM " + plugin.storageDatabase + "_rounds WHERE map='" + map + "'");
                try {
                    rsd.first();
                    int playcount = rsd.getInt(1);

                    ArrayList<String> creators = Storage.creators.get(map);
                    sender.sendMessage(ChatColor.GRAY + "|-------------------------");
                    sender.sendMessage(ChatColor.GRAY + "| Information on " + ChatColor.AQUA + map);
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.GREEN + "Made by: " + ChatColor.BLUE + ChatColor.ITALIC + Utility.sentenceFormat(creators));
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.DARK_AQUA + "It is a " + ChatColor.LIGHT_PURPLE +
                            ChatColor.ITALIC + Gamemode.toFormat(Storage.gameTypes.get(map)) + ChatColor.RESET + ChatColor.DARK_AQUA + " type map");
                    sender.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "Times played: " + ChatColor.GRAY + playcount);
                    sender.sendMessage(ChatColor.GRAY + "|-------------------------");
                } catch (SQLException e) {

                }
            }
        });
    }

    private String matchMap(String map) {
        for (String s : Storage.maps) {
            if (s.toLowerCase().startsWith(map.toLowerCase())) {
                return s;
            }
        }
        return "None";
    }

    private double toKD(int kills, int deaths) {
        if (deaths == 0) return kills;
        if (kills == 0 && deaths > 0) return 0;
        double kd = kills / deaths;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(kd));
    }
}
