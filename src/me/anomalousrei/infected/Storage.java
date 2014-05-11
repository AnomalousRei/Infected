package me.anomalousrei.infected;

import me.anomalousrei.infected.util.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Storage {

    /* Round Attributes */
    public static String currentRound = "None";
    public static ArrayList<String> currentCreators = new ArrayList<String>();
    public static long roundID = 0;
    public static String roundStatus = "None";
    public static Gamemode currentGamemode = Gamemode.CLASSIC;
    /*------------------*/

    /* Map Attributes */
    public static ArrayList<String> noBreak = new ArrayList<String>();
    public static ArrayList<String> noPlace = new ArrayList<String>();
    public static ArrayList<String> noDrops = new ArrayList<String>();
    /*------------------*/

    /* Rotation Attributes */
    public static String gTo = "None";
    public static int rotationPoint = 0;
    public static List<String> rotationList = Infected.getInstance().getConfig().getStringList("rotation");
    public static int maxRotationPoint = rotationList.size();
    /*---------------------*/


    //Map config attributes
    public static ArrayList<String> maps = new ArrayList<String>();
    public static HashMap<String, Gamemode> gameTypes = new HashMap<String, Gamemode>();
    public static HashMap<String, Location> spawns = new HashMap<String, Location>();
    public static HashMap<String, HashMap<Integer, ItemStack>> kits = new HashMap<String, HashMap<Integer, ItemStack>>();
    public static HashMap<String, ArrayList<String>> creators = new HashMap<String, ArrayList<String>>();

    public static void registerMaps() {
        if (new File("config/").listFiles() != null && new File("config/").listFiles().length > 0) {
            for (File f : new File("config/").listFiles()) {
                if (f.getName().contains(".xml")) {
                    try {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(f);
                        doc.getDocumentElement().normalize();

                        String rootNode = doc.getDocumentElement().getNodeName();
                        NodeList rootList = doc.getElementsByTagName(rootNode);
                        String mapName = ((Element) rootList.item(0)).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();


                        NodeList nSpawn = doc.getElementsByTagName("spawn");
                        spawns.put(mapName, new Location(Bukkit.getWorld(mapName), Integer.parseInt(nSpawn.item(0).getAttributes().getNamedItem("x").getTextContent()),
                                Integer.parseInt(nSpawn.item(0).getAttributes().getNamedItem("y").getTextContent()),
                                Integer.parseInt(nSpawn.item(0).getAttributes().getNamedItem("z").getTextContent())));

                        gameTypes.put(mapName, Gamemode.valueOf(((Element) rootList.item(0)).getElementsByTagName("objective").item(0).getChildNodes().item(0).getNodeValue()));
                        maps.add(mapName);


                        NodeList creator = doc.getElementsByTagName("creators");
                        ArrayList<String> tempC = new ArrayList<String>();

                        for (int i = 0; i < creator.getLength(); i++) {
                            Node node = creator.item(i);
                            for (int j = 0; j < node.getChildNodes().getLength(); j++) {

                                Node child = node.getChildNodes().item(j);

                                if (!child.getNodeName().equals("#text")) {
                                    NamedNodeMap attributes = child.getAttributes();
                                    tempC.add(attributes.getNamedItem("name").getTextContent());
                                }
                            }
                        }
                        creators.put(mapName, tempC);
                        NodeList nKit = doc.getElementsByTagName("kit");
                        HashMap<Integer, ItemStack> kit = new HashMap<Integer, ItemStack>();

                        for (int i = 0; i < nKit.getLength(); i++) {
                            Node node = nKit.item(i);
                            for (int j = 0; j < node.getChildNodes().getLength(); j++) {

                                Node child = node.getChildNodes().item(j);

                                if (!child.getNodeName().equals("#text")) {
                                    NamedNodeMap attributes = child.getAttributes();

                                    if (child.getNodeName().equals("item")) {
                                        kit.put(Integer.parseInt(attributes.getNamedItem("slot").getTextContent()), new ItemStack(Material.matchMaterial(attributes.getNamedItem("type").getTextContent()),
                                                Integer.parseInt(attributes.getNamedItem("amount").getTextContent())));
                                    }
                                    if (child.getNodeName().equalsIgnoreCase("helmet")) {
                                        kit.put(-1, new ItemStack(Material.matchMaterial(attributes.getNamedItem("type").getTextContent()), 1));
                                    }
                                    if (child.getNodeName().equalsIgnoreCase("chestplate")) {
                                        kit.put(-2, new ItemStack(Material.matchMaterial(attributes.getNamedItem("type").getTextContent()), 1));
                                    }
                                    if (child.getNodeName().equalsIgnoreCase("leggings")) {
                                        kit.put(-3, new ItemStack(Material.matchMaterial(attributes.getNamedItem("type").getTextContent()), 1));
                                    }
                                    if (child.getNodeName().equalsIgnoreCase("boots")) {
                                        kit.put(-4, new ItemStack(Material.matchMaterial(attributes.getNamedItem("type").getTextContent()), 1));
                                    }
                                }
                            }
                        }
                        kits.put(mapName, kit);
                    } catch (Exception ex) {
                        System.out.println("An exception occured while trying to load Infection maps!");
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("No Infection maps were found!");
        }
    }

    public static String getRandomDeathMessage() {
        ArrayList<String> messages = new ArrayList<String>();
        messages.add(" fell victim to ");
        messages.add(" was torn apart by ");
        messages.add(" got shown up by ");
        messages.add(" was hunted down by ");
        messages.add(" couldn't get past ");
        return messages.get(new Random().nextInt(messages.size()));
    }
}
