package edu.Kennesaw.ksumcspeedrun.Structures;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SRStructure {

    private Structure structure;
    private String structureName;

    Main plugin;

    public SRStructure(Main plugin, String structure) {
        this.plugin = plugin;
        setStructure(structure);
    }

    public Structure getStructure() {
        return structure;
    }

    public String getName() {
        return structureName;
    }

    public void setStructure(String structureName) {
        this.structure = getStructureFromString(structureName);
        this.structureName = structureName;
    }

    public static void getStructures(Main plugin, StructureListResultCallback structList) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            List<Structure> structures = new ArrayList<>();
            for (Field f : Structure.class.getFields()) {
                Object value = null;
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

    public static List<String> getStructureNames() {
        List<String> structures = new ArrayList<>();
        for (Field f : Structure.class.getFields()) {
            structures.add(f.getName());
        }
        return structures;
    }

    public static Structure getStructureFromString(String structure) {
        HashMap<String, Structure> structureTypes = new HashMap<>();
        for (Field f : Structure.class.getFields()) {
            Object value = null;
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

    public static void getNearestStructureToLocation(Main plugin, SRStructure structureToFind, Location locFrom, LocationResultCallback callback) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
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
            Bukkit.getScheduler().runTask(plugin, () -> {
                StructureSearchResult srs = locFrom.getWorld().locateNearestStructure(locFrom, structureToFind.getStructure(), 100, false);
                if (srs != null) {
                    Location loc = srs.getLocation();
                    if (finalAvgY != null && finalAvgY.equalsIgnoreCase("ground")) {
                        loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                    } else {
                        loc.setY(finalY);
                    }
                    callback.onResult(loc);
                } else {
                    callback.onResult(null);
                }
            });
        });
    }

    public static void getStructureRadius(Main plugin, SRStructure target, RadiusResultCallback callback) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {
            callback.onResult(plugin.getSpeedrunConfig().getInt("structureLocations." + target.getName() + ".radius"));
        });
    }

    public interface LocationResultCallback {
        void onResult(Location loc);
    }

    public interface StructureResultCallback {
        void onResult(Structure struct);
    }

    public interface StructureListResultCallback {
        void onResult(List<Structure> struct);
    }

    public interface RadiusResultCallback {
        void onResult(int i);
    }

}
