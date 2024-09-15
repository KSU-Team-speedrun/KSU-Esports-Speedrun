package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunSuggestions {

    // Returns a list of the names of all Living Entity types in the game
    public static List<String> getLivingEntities() {

        List<String> livingEntityNames = new ArrayList<>();
        EntityType[] entities = EntityType.values();
        for (EntityType entity : entities) {
            if (entity.isAlive()) {
                livingEntityNames.add(entity.name());
            }
        }
        return livingEntityNames;

    }

    // Returns a list of the names of all valid location types: biomes, structures, & portals
    public static List<String> getLocationNames() {

        List<String> locationNames = new ArrayList<>();
        Biome[] biomes = Biome.values();
        for (Biome b : biomes) {
            locationNames.add(b.name());
        }
        List<String> structures = SRStructure.getStructureNames();
        locationNames.addAll(structures);
        locationNames.addAll(Portal.getPortalTypeNames());
        return locationNames;

    }

    // Returns a list of the names of every block in the game
    public static List<String> getBlockNames() {
        List<String> blockNames = new ArrayList<>();
        Material[] materials = Material.values();
        for (Material m : materials) {
            if (m.isBlock() && !m.isLegacy()) {
                blockNames.add(m.name());
            }
        }
        return blockNames;
    }

    // Returns a list of the names of every item in the game
    public static List<String> getItemNames() {

        List<String> itemNames = new ArrayList<>();
        Material[] materials = Material.values();
        for (Material m : materials) {
            if (!m.isLegacy()) {
                itemNames.add(m.name());
            }
        }
        return itemNames;

    }

}
