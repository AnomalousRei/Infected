package me.anomalousrei.infected;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import me.anomalousrei.infected.handlers.MapHandler;
import me.anomalousrei.infected.handlers.RoundHandler;
import me.anomalousrei.infected.handlers.SQLHandler;
import me.anomalousrei.infected.listener.GlobalListener;
import me.anomalousrei.infected.listener.MapListener;
import me.anomalousrei.infected.object.IPlayer;
import me.anomalousrei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class Infected extends JavaPlugin {

    public static Infected plugin;
    public static Logger logger = Logger.getLogger("Minecraft");
    public Tasks task = new Tasks(this);
    public static HashMap<String, IPlayer> iPlayers = new HashMap<String, IPlayer>();
    public static ArrayList<String> Input = new ArrayList<String>();

    //SQL stuff, notouch.
    public String storageType = null;
    public String storagePort = null;
    public String storageHostname = null;
    public String storageUsername = null;
    public String storagePassword = null;
    public String storageDatabase = null;
    public MySQL mysql;

    public void onEnable() {
        plugin = this;

        for (Player p : Bukkit.getOnlinePlayers()) {
            IPlayer.craftIPlayer(p);
        }

        Scoreboard scoreboard = Utility.scoreboard;
        Objective o = scoreboard.registerNewObjective("Player Count", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score zombies = o.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_RED + "Zombies: "));
        Score humans = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Humans: "));
        zombies.setScore(0);
        humans.setScore(0);

        new GlobalListener(this);
        new RoundHandler(this);
        new MapListener(this);

        saveDefaultConfig();
        reloadConfig();
        Storage.registerMaps();
        registerCommands();
        cacheTimer();

        final long ID = Utility.generateID();
        MapHandler.loadMap(Storage.rotationList.get(Storage.rotationPoint), ID + "");
        RoundHandler.start(Storage.rotationList.get(Storage.rotationPoint), ID);

        //SQL Stuff
        // SQL Connection
        storageType = getConfig().getString("database.type");
        storagePort = getConfig().getString("database.port");
        storageHostname = getConfig().getString("database.hostname");
        storageUsername = getConfig().getString("database.username");
        storagePassword = getConfig().getString("database.password");
        storageDatabase = getConfig().getString("database.database");

        mysql = new MySQL(logger,
                "[InfectedDB] ",
                storageHostname,
                storagePort,
                storageDatabase,
                storageUsername,
                storagePassword);
        mysql.open();
        if (mysql.checkConnection()) {
            System.out.println("MySQL connected successfully!");
            SQLHandler.createTables();
            mysql.close();
        } else {
            System.out.println("We couldn't connect to the SQL!");
            mysql.close();
        }
    }

    public void onDisable() {
        MapHandler.restoreMap(Storage.roundID + "");
    }

    public static Infected getInstance() {
        return plugin;
    }

    // This will push all cached input every 10 seconds
    public synchronized void cacheTimer() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public synchronized void run() {
                if (Input.size() <= 0) return;
                mysql.open();
                while (Input.size() > 0) {
                    String s = Input.get(0);
                    mysql.query(s);
                    Input.remove(0);
                }
                mysql.close();
            }
        }, 10 * 20L, 10 * 20L);
    }

    /**
     * *******************************************************************
     * Code to use for sk89q's command framework goes below this comment! *
     * ********************************************************************
     */

    private CommandsManager<CommandSender> commands;
    private boolean opPermissions;

    private void registerCommands() {
        final Infected plugin = this;
        // Register the commands that we want to use
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };
        commands.setInjector(new SimpleInjector(this));
        final CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(this, commands);

        cmdRegister.register(Commands.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "You need to enter a number!");
            } else {
                sender.sendMessage(ChatColor.RED + "Error occurred, contact developer.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    public boolean hasPermission(CommandSender sender, String perm) {
        if (!(sender instanceof Player)) {
            if (sender.hasPermission(perm)) {
                return ((sender.isOp() && (opPermissions || sender instanceof ConsoleCommandSender)));
            }
        }
        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
        return ((sender.isOp() && opPermissions) || sender instanceof ConsoleCommandSender || sender.hasPermission(perm));
    }

    public void checkPermission(CommandSender sender, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    public void checkPermission(CommandSender sender, World world, String perm)
            throws CommandPermissionsException {
        throw new CommandPermissionsException();
    }
}
