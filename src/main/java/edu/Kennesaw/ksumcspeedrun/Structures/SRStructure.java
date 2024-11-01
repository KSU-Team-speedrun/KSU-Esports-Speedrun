package edu.Kennesaw.ksumcspeedrun.Structures;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
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
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

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
                    if (finalAvgY != null && finalAvgY.equalsIgnoreCase("ground")) {
                        loc.setY(loc.getWorld().getHighestBlockYAt(loc));
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
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> callback.onResult(plugin.getSpeedrunConfig()
                .getInt("structureLocations." + target.getName() + ".radius")));
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
