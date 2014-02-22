package me.AnomalousRei.infected.handlers;

import me.AnomalousRei.infected.Infected;
import com.oresomecraft.campaign.database.MySQL;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHandler {

    static Infected plugin = Infected.getInstance();

    /**
     * Creates SQL Tables if they don't already exist
     */
    public static synchronized void createTables() {

        MySQL mysql = new MySQL(plugin.logger,
                "[InfectedDB] ",
                plugin.storageHostname,
                plugin.storagePort,
                plugin.storageDatabase,
                plugin.storageUsername,
                plugin.storagePassword);
        mysql.open();

        if (!mysql.checkTable(plugin.storageDatabase + "_kills")) {
            mysql.query("CREATE TABLE `" + plugin.storageDatabase + "_kills` (" +
                    "`id` INT(10) UNSIGNED NULL AUTO_INCREMENT," +
                    "`killed` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "`killer` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "`map` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "`gamemode` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "PRIMARY KEY (`id`))");
        }

        if (!mysql.checkTable(plugin.storageDatabase + "_rounds")) {
            mysql.query("CREATE TABLE `" + plugin.storageDatabase + "_rounds` (" +
                    "`id` INT(10) UNSIGNED NULL AUTO_INCREMENT," +
                    "`map` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "`winner` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "`gamemode` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci'," +
                    "PRIMARY KEY (`id`))");
        }

        mysql.close();
    }

    public static synchronized boolean userExists(String name) throws SQLException {
        MySQL mysql = new MySQL(plugin.logger,
                "[InfectedDB] ",
                plugin.storageHostname,
                plugin.storagePort,
                plugin.storageDatabase,
                plugin.storageUsername,
                plugin.storagePassword);

        mysql.open();

        ResultSet killer = mysql.query("SELECT COUNT(1) FROM " + plugin.storageDatabase + "_kills where killer = \"" + name + "\"");
        ResultSet killed = mysql.query("SELECT COUNT(1) FROM " + plugin.storageDatabase + "_kills where killed = \"" + name + "\"");
        killer.first();
        killed.first();
        if (killed.getInt(1) != 0 || killer.getInt(1) != 0) return true;
        return false;
    }

    public static synchronized void logKill(String infector, String infected, String map, String gamemode) {
        synchronized (Infected.Input) {
            Infected.Input.add("INSERT INTO `Infected`.`Infected_kills` (`id`, `killed`, `killer`, `map`, `gamemode`) VALUES (NULL, '" + infected + "', '" + infector + "', '" + map + "', '" + gamemode + "')");
        }

    }

    public static synchronized void logRound(String map, String winner, String gamemode) {
        synchronized (Infected.Input) {
            Infected.Input.add("INSERT INTO `Infected`.`Infected_rounds` (`id`, `map`, `winner`, `gamemode`) VALUES (NULL, '" + map + "', '" + winner + "', '" + gamemode + "')");
        }

    }
}
