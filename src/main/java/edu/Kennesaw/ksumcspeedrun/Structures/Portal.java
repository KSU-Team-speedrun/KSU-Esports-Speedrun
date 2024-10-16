package edu.Kennesaw.ksumcspeedrun.Structures;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidPortalException;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// We create a Portal object to define different portals based on their dimension from AND to
public class Portal {

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

    // Private variable for Portal instances
    private PortalType portalType;

    // Portal can be initialized using PortalType value
    public Portal(PortalType portalType) {
        this.portalType = portalType;
    }

    // Portal can also be initialized using dimension to and dimension from
    public Portal(World.Environment dimension1, World.Environment dimension2) throws InvalidPortalException {

        if (dimension1.equals(World.Environment.NORMAL)) {
            if (dimension2.equals(World.Environment.NETHER)) {
                this.portalType = PortalType.WORLD_TO_NETHER;
            } else if (dimension2.equals(World.Environment.THE_END)) {
                this.portalType = PortalType.WORLD_TO_END;
            } else {

                /* InvalidPortalException will be thrown if a portal is initialized that doesn't exist (e.g., from END
                   to NETHER */
                throw new InvalidPortalException(dimension2.name() + " is an invalid dimension from " + dimension1.name());

            }
        } else if (dimension1.equals(World.Environment.NETHER)) {
            if (dimension2.equals(World.Environment.NORMAL)) {
                this.portalType = PortalType.NETHER_TO_WORLD;
            } else {
                throw new InvalidPortalException(dimension2.name() + " is an invalid dimension from " + dimension1.name());
            }
        } else if (dimension1.equals(World.Environment.THE_END)) {
            if (dimension2.equals(World.Environment.NORMAL)) {
                this.portalType = PortalType.END_TO_WORLD;
            } else if (dimension2.equals(World.Environment.THE_END)) {
                this.portalType = PortalType.END_TO_END;
            } else {
                throw new InvalidPortalException(dimension2.name() + " is an invalid dimension from " + dimension1.name());
            }
        } else {
            throw new InvalidPortalException(dimension2.name() + " is an invalid dimension from " + dimension1.name());
        }
    }

    // Portal type getter method
    public PortalType getPortalType() {
        return portalType;
    }

    // Portal type setter method
    public void setPortalType(PortalType portalType) {
        this.portalType = portalType;
    }

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
