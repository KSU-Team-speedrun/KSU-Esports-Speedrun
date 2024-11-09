package edu.Kennesaw.ksumcspeedrun.Structures;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* Spigot/Paper does not contain any default methods for getting a Structure type from a String, so we must
   do this ourselves. This class gets the Structure fields and names directly from the
   org.bukkit.generator.structure.Structure class and allows us to assign structure types from a String */
public class SRStructure {

    // Two values are assigned when an instance is created, structure and structureName.
    private Structure structure;
    private String structureName;

    // An instance of the main class is also needed so that we can access the plugin configuration file.
    Main plugin;

    // Constructor takes plugin instance and String that corresponds to structure name.
    public SRStructure(Main plugin, String structure) {

        this.plugin = plugin;
        setStructure(structure);

    }

    // Return the Structure object
    public Structure getStructure() {
        return structure;
    }

    // Return the name of the Structure object
    public String getName() {
        return structureName;
    }

    // Set the Structure object using String name that corresponds to structure
    public void setStructure(String structureName) {

        this.structure = getStructureFromString(structureName);
        this.structureName = structureName;

    }

    // Async method that returns a list of all Structures objects in the game using a callback
    @SuppressWarnings("unused")
    public static void getStructures(Main plugin, StructureListResultCallback structList) {

        plugin.runAsyncTask(() -> {
            List<Structure> structures = new ArrayList<>();
            for (Field f : Structure.class.getFields()) {
                Object value;
                try {
                    value = f.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (value instanceof Structure) {
                    structures.add((Structure) value);
                }
            }
            structList.onResult(structures);
        });

    }

    // Returns a String list of the names of all structures in the game
    public static List<String> getStructureNames() {

        List<String> structures = new ArrayList<>();
        for (Field f : Structure.class.getFields()) {
            structures.add(f.getName());
        }
        return structures;

    }

    /* Using a HashMap, we can assign each Structure Object value a String key that corresponds to its field name,
       and we can return the Structure object using its corresponding String name */
    public static Structure getStructureFromString(String structure) {
        HashMap<String, Structure> structureTypes = new HashMap<>();
        for (Field f : Structure.class.getFields()) {
            Object value;
            try {
                value = f.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (value instanceof Structure) {
                structureTypes.put(f.getName(), (Structure) value);
            }
        }
        if (structureTypes.containsKey(structure)) {
            return structureTypes.get(structure);
        }
        return null;
    }

    // Locate the nearest specified structure to a given location, return the location of the structure using callback
    public static void getNearestStructureToLocation(Main plugin, SRStructure structureToFind, Location locFrom, LocationResultCallback callback) {

        // We use an Async thread to access the config:
        plugin.runAsyncTask(() -> {

            /* Bukkit's StructureSearchResult does not return the Y-coordinate
           for specific structures being located for some reason, so the best workaround is to manually set the average
           Y-coordinate for structures in the config. By default, most structures in the config have an average
           Y-coordinate of "ground", which signifies ground level. */
            String averageY = null;
            int y = 0;
            Object averageYConfig = plugin.getSpeedrunConfig().getObject("structureLocations." + structureToFind.getName() + ".averageYCoordinate");
            if (averageYConfig instanceof String) {
                averageY = (String) averageYConfig;
            } else if (averageYConfig instanceof Integer) {
                y = (Integer) averageYConfig;
            }
            final String finalAvgY = averageY;
            final int finalY = y;

            /* Once we have this coordinate value, we go back to the main thread to do a structure search (any
               computation that interacts with players or the world must be on the main thread) */
            Bukkit.getScheduler().runTask(plugin, () -> {

                /* This StructureSearchResult instance will return not null if the specified structure is within a 100
                   block radius. */
                StructureSearchResult srs = locFrom.getWorld().locateNearestStructure(locFrom, structureToFind.getStructure(), 100, false);

                if (srs != null) {

                    // If srs is not null, then we can get the location of the structure we are trying to find
                    Location loc = srs.getLocation();

                    // We manually set the Y-coordinate to the average value specified in the Config.yml file.
                    if (finalAvgY != null && finalAvgY.toLowerCase().startsWith("ground")) {
                        String[] avgYParser = finalAvgY.split(" ");
                        if (avgYParser.length == 3) {
                            if (avgYParser[1].equalsIgnoreCase("-")) {
                                try {
                                    int adjustment = Integer.parseInt(avgYParser[2]);
                                    loc.setY(loc.getWorld().getHighestBlockYAt(loc) - adjustment);
                                } catch (NumberFormatException e) {
                                    plugin.getLogger().warning("Config error for " + structureToFind + ": "
                                    + avgYParser[2] + " must be a number. Setting avgY at ground level..");
                                    loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                                }
                            } else if (avgYParser[1].equalsIgnoreCase("+")) {
                                try {
                                    int adjustment = Integer.parseInt(avgYParser[2]);
                                    loc.setY(loc.getWorld().getHighestBlockYAt(loc) + adjustment);
                                } catch (NumberFormatException e) {
                                    plugin.getLogger().warning("Config error for " + structureToFind + ": "
                                            + avgYParser[2] + " must be a number. Setting avgY at ground level..");
                                    loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                                }
                            } else {
                                plugin.getLogger().warning("Config error for " + structureToFind + ": "
                                        + avgYParser[1] + " must be a '+' or '-'. Setting avgY at ground level..");
                                loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                            }
                        } else {
                            loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                        }
                    } else {
                        loc.setY(finalY);
                    }

                    // The location result is then called back so it can be used
                    callback.onResult(loc);

                } else {

                    // The location result is null if a structure is not found within 100 blocks of the initial location
                    callback.onResult(null);

                }
            });
        });
    }

    public static void getStructureRadius(Main plugin, SRStructure target, RadiusResultCallback callback) {
        plugin.runAsyncTask(() -> callback.onResult(plugin.getSpeedrunConfig()
                .getInt("structureLocations." + target.getName() + ".radius")));
    }

    public static void getStructureHeight(Main plugin, SRStructure target, RadiusResultCallback callback) {
        plugin.runAsyncTask(() ->callback.onResult(plugin.getSpeedrunConfig()
                .getInt("structureLocations." + target.getName() + ".height")));
    }

    public interface LocationResultCallback {
        void onResult(Location loc);
    }

    public interface StructureListResultCallback {
        void onResult(List<Structure> struct);
    }

    public interface RadiusResultCallback {
        void onResult(int i);
    }

}
