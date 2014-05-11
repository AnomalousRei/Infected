package me.anomalousrei.infected.handlers;

import me.anomalousrei.infected.NullChunkGenerator;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.event.world.WorldUnloadEvent;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

public abstract class MapHandler {

    /**
     * Load's a world
     *
     * @param map World to load
     */
    public static void loadMap(String map, String ID) {

        File worldToCopy = new File("maps/" + map.toLowerCase());
        try {
            copyFolder(worldToCopy, new File(Bukkit.getWorldContainer().getAbsolutePath() + "/" + ID));
        } catch (IOException ex) {
            System.out.println("An error occurred while attempting to load a map!");
            ex.printStackTrace();
        }

        Bukkit.createWorld(new WorldCreator(ID).generator(new NullChunkGenerator()));
    }

    public static boolean restoreMap(String map) {
        if (Bukkit.getWorld(map) != null) forceUnloadWorld(Bukkit.getWorld(map));
        return deleteFolder(new File(map));
    }

    /**
     * Deletes a directory
     *
     * @param folder Directory to delete
     * @return true if directory was successfully deleted
     */
    private static boolean deleteFolder(File folder) {
        return (!folder.exists() || folder.delete());
    }

    /**
     * Force unloads a world
     *
     * @param world World to force unload
     */
    public static void forceUnloadWorld(org.bukkit.World world) {

        CraftServer server = (CraftServer) Bukkit.getServer();
        CraftWorld craftWorld = (CraftWorld) world;

        WorldUnloadEvent e = new WorldUnloadEvent(world);
        Bukkit.getPluginManager().callEvent(e);

        try {
            Field field = server.getClass().getDeclaredField("worlds"); // Get worlds ArrayList
            field.setAccessible(true); // Make it accessible
            @SuppressWarnings("unchecked")
            Map<String, World> worlds = (Map<String, World>) field.get(server);
            worlds.remove(world.getName().toLowerCase()); // Remove world from worlds list
            field.setAccessible(false); // Make it private again
        } catch (Exception ex) {
            System.out.println("An error occurred while trying to Force Unload a world!");
            ex.printStackTrace();
        }

        MinecraftServer minecraftServer = getMinecraftServer();
        minecraftServer.worlds.remove(minecraftServer.worlds.indexOf(craftWorld.getHandle())); // Remove WorldServer

    }

    /**
     * Gets the MinecraftServer instance
     *
     * @return a MinecraftServer instance
     */
    protected static MinecraftServer getMinecraftServer() {
        CraftServer server = (CraftServer) Bukkit.getServer();
        return server.getServer();
    }

    /**
     * Copy Map folder into main server directory for loading
     *
     * @param src         Source directory
     * @param destination Destination directory
     * @throws IOException Thrown if an error occurs while trying to copy
     */
    public static void copyFolder(File src, File destination) throws IOException {

        if (src.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(destination, file);
                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}