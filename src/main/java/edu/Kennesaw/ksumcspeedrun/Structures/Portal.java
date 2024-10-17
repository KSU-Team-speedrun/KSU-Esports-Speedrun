package edu.Kennesaw.ksumcspeedrun.Structures;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @param portalType Private variable for Portal instances
 */ // We create a Portal object to define different portals based on their dimension from AND to
public record Portal(edu.Kennesaw.ksumcspeedrun.Structures.Portal.PortalType portalType) {

    /* e.g., WORLD_TO_NETHER is classic nether portal, NETHER_TO_WORLD is nether return portal, END_TO_END is a portal
       from the main end island to the outer islands (or back), END_TO_WORLD is the end return portal, etc.*/
    public enum PortalType {
        WORLD_TO_NETHER, NETHER_TO_WORLD, WORLD_TO_END,
        END_TO_END, END_TO_WORLD
    }

    // Returns a list of PortalTypes
    public static List<PortalType> getPortalTypes() {
        return new ArrayList<>(Arrays.asList(PortalType.values()));
    }

    // Returns a list of PortalType names
    public static List<String> getPortalTypeNames() {
        List<String> portalNames = new ArrayList<>();
        for (PortalType p : getPortalTypes()) {
            portalNames.add(p.name());
        }
        return portalNames;
    }

    // Portal can be initialized using PortalType value

    public World.Environment getFrom() {
        if (portalType.equals(PortalType.WORLD_TO_NETHER) || portalType.equals(PortalType.WORLD_TO_END)) {
            return World.Environment.NORMAL;
        }
        if (portalType.equals(PortalType.NETHER_TO_WORLD)) {
            return World.Environment.NETHER;
        }
        if (portalType.equals(PortalType.END_TO_WORLD) || portalType.equals(PortalType.END_TO_END)) {
            return World.Environment.THE_END;
        }
        return null;
    }

    public World.Environment getTo() {
        if (portalType.equals(PortalType.NETHER_TO_WORLD) || portalType.equals(PortalType.END_TO_WORLD)) {
            return World.Environment.NORMAL;
        }
        if (portalType.equals(PortalType.WORLD_TO_NETHER)) {
            return World.Environment.NETHER;
        }
        if (portalType.equals(PortalType.WORLD_TO_END) || portalType.equals(PortalType.END_TO_END)) {
            return World.Environment.THE_END;
        }
        return null;
    }

}
