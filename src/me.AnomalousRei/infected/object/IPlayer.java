package me.AnomalousRei.infected.object;

import me.AnomalousRei.infected.Infected;
import me.AnomalousRei.infected.util.Team;
import me.AnomalousRei.infected.util.Utility;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kitteh.tag.TagAPI;

public class IPlayer extends CraftPlayer {

    public IPlayer(Player p) {
        super((CraftServer) Bukkit.getServer(), ((CraftPlayer) p).getHandle());
    }

    private Team team = Team.OBSERVER;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team t) {
        team = t;
        TagAPI.refreshPlayer(getPlayer());
        Utility.updateDisplayName(getPlayer());
        Utility.updateScoreboard();
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
    // *********** Deprecated thanks to Bukkit's over-mapped thing ***********

    public double getLastDamage() {
        return super.getLastDamage();
    }

    public double getMaxHealth() {
        return super.getMaxHealth();
    }

    public double getHealth() {
        return super.getHealth();
    }

    @Deprecated
    public void setMaxHealth(int amount) {
        super.setMaxHealth((double) amount);
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

    @Deprecated
    public void setLastDamage(int amount) {
        super.damage((double) amount);
    }
}
