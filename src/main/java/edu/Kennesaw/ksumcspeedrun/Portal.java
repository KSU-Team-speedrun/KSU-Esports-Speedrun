package edu.Kennesaw.ksumcspeedrun;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidPortalException;
import org.bukkit.World;

public class Portal {

    public enum PortalType {
        WORLD_TO_NETHER, NETHER_TO_WORLD, WORLD_TO_END,
        END_TO_END, END_TO_WORLD
    }

    private PortalType portalType;

    public Portal(PortalType portalType) {
        this.portalType = portalType;
    }

    public Portal(World.Environment dimension1, World.Environment dimension2) throws InvalidPortalException {
        if (dimension1.equals(World.Environment.NORMAL)) {
            if (dimension2.equals(World.Environment.NETHER)) {
                this.portalType = PortalType.WORLD_TO_NETHER;
            } else if (dimension2.equals(World.Environment.THE_END)) {
                this.portalType = PortalType.WORLD_TO_END;
            } else {
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

    public PortalType getPortalType() {
        return portalType;
    }

    public void setPortalType(PortalType portalType) {
        this.portalType = portalType;
    }

}
