package me.anomalousrei.infected.object;

import me.anomalousrei.infected.Infected;
import me.anomalousrei.infected.util.Team;
import me.anomalousrei.infected.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.kitteh.tag.TagAPI;

public class IPlayer extends CraftPlayer {

    private Team team = Team.OBSERVER;

    public IPlayer(Player p) {
        super((CraftServer) Bukkit.getServer(), ((CraftPlayer) p).getHandle());
    }

    public static void craftIPlayer(Player p) {
        IPlayer pl = new IPlayer(p);
        Infected.iPlayers.put(p.getName(), pl);
    }

    public static IPlayer getIPlayer(Player p) {
        return Infected.iPlayers.get(p.getName());
    }

    public static IPlayer getIPlayer(String p) {
        return Infected.iPlayers.get(p);
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team t) {
        team = t;
        TagAPI.refreshPlayer(getPlayer());
        Utility.updateDisplayName(getPlayer());
        Utility.updateScoreboard();
    }
    // *********** Deprecated thanks to Bukkit's over-mapped thing ***********

    public double getLastDamage() {
        return super.getLastDamage();
    }

    @Deprecated
    public void setLastDamage(int amount) {
        super.damage((double) amount);
    }

    public double getMaxHealth() {
        return super.getMaxHealth();
    }

    @Deprecated
    public void setMaxHealth(int amount) {
        super.setMaxHealth((double) amount);
    }

    public double getHealth() {
        return super.getHealth();
    }

    @Deprecated
    public void setHealth(int amount) {
        setHealth((double) amount);
    }

    @Deprecated
    public void damage(int amount) {
        super.damage((double) amount);
    }

    @Deprecated
    public void damage(int amount, org.bukkit.entity.Entity entity) {
        super.damage((double) amount, entity);
    }
}
