package edu.Kennesaw.ksumcspeedrun.Commands;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunSuggestions {

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

}
