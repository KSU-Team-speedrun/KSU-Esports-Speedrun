package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.block.Biome;

// EnterObjective is a subclass of Objective
public class EnterObjective extends Objective {

    /* In addition to default attributes of Objective, EnterObjective either has a biomeTarget, structureTarget,
     or portalType target. Only one of these attributes can be assigned, the other two must be null. */
    private Biome biomeTarget;
    private SRStructure structureTarget;
    private Portal portalType;

    Main plugin;


    // Constructor that includes weight
    public EnterObjective(Object locationType, int weight, Main plugin) throws InvalidTargetLocationException {

        // ObjectiveType.ENTER and weight are passed to abstract Objective class
        super(ObjectiveType.ENTER, weight, plugin);
        /* LocationType attribute is assigned. If locationType object passed is not of type Biome, SRStructure, or
           Portal, then InvalidTargetLocationException is thrown. */
        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }

        String target = biomeTarget != null ? (biomeTarget.name()) : (structureTarget != null ? structureTarget.getName()
                : portalType.toString());

        setTargetName(target);

    }

    // Same as above, but weight is default (1) & not included in constructor
    public EnterObjective(Object locationType, Main plugin) throws InvalidTargetLocationException {

        super(ObjectiveType.ENTER, plugin);

        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }

        String target = biomeTarget != null ? (biomeTarget.name()) : (structureTarget != null ? structureTarget.getName() : portalType.toString());

        setTargetName(target);

    }

    // Returns the target, either a Biome, SRStructure, or Portal
    public Object getTarget() {

        if (biomeTarget != null) return biomeTarget;
        else if (structureTarget != null) return structureTarget;
        else if (portalType != null) return portalType;
        else return null;

    }

}
