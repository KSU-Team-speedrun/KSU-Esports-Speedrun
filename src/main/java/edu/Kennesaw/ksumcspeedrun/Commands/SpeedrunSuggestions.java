package edu.Kennesaw.ksumcspeedrun.Commands;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunSuggestions {

    public static List<String> getLivingEntities() {
        List<String> livingEntityNames = new ArrayList<>();
        EntityType[] entities = EntityType.values();
        for (int i = 0; i < EntityType.values().length; i++) {
            if (entities[i].isAlive()) {
                livingEntityNames.add(entities[i].name());
            }
        }
        return livingEntityNames;
    }

}
