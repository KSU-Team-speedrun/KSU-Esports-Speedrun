package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.block.Biome;
import org.checkerframework.framework.qual.Unused;
import java.lang.Deprecated;

/**
 * The EnterObjective class extends the {@link Objective}  class and represents an objective
 * where a player enters a specific location. This location can be a Biome, SRStructure,
 * or Portal, but only one of these can be specified at a time. The class provides
 * different constructors to create an EnterObjective with different parameters, such as weight
 * and amount.
 *
 * Attributes:
 * - biomeTarget: The target Biome that the player needs to enter.
 * - structureTarget: The target SRStructure that the player needs to enter.
 * - portalType: The target Portal that the player needs to enter.
 *
 * Constructors:
 * - EnterObjective(Object locationType, int weight, Main plugin): Initializes the objective
 *   with a specified locationType and weight.
 * - EnterObjective(Object locationType, int weight, int amount, Main plugin): Initializes the
 *   objective with specified locationType, weight, and amount.
 * - EnterObjective(Object locationType, Main plugin): Initializes the objective with a
 *   specified locationType and default weight of 1.
 *
 * Methods:
 * - getTarget(): Returns the target object, which can be a Biome, SRStructure, or Portal.
 *
 * Each constructor assigns the locationType attribute based on the provided parameter.
 * If the provided locationType is not an instance of Biome, SRStructure, or Portal,
 * an InvalidTargetLocationException is thrown. The target name is also set based
 * on the provided locationType.
 */
public class EnterObjective extends Objective {

    /* In addition to default attributes of Objective, EnterObjective either has a biomeTarget, structureTarget,
     or portalType target. Only one of these attributes can be assigned, the other two must be null. */
    private Biome biomeTarget;
    private SRStructure structureTarget;
    private Portal portalType;

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

        String target = biomeTarget != null ? ( biomeTarget.name()) : (structureTarget != null ? structureTarget.getName() :
                portalType.portalType().name());

        setTargetName(target);

    }

    @Deprecated
    public EnterObjective(Object locationType, int weight, int amount, Main plugin) throws InvalidTargetLocationException {

        // ObjectiveType.ENTER and weight are passed to abstract Objective class
        super(ObjectiveType.ENTER, weight, amount, plugin);
        /* LocationType attribute is assigned. If locationType object passed is not of type Biome, SRStructure, or
           Portal, then InvalidTargetLocationException is thrown. */
        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }

        String target = biomeTarget != null ? ( biomeTarget.name()) : (structureTarget != null ? structureTarget.getName() :
                portalType.portalType().name());

        setTargetName(target);

    }

    // Same as above, but weight & amount is default (1) & not included in constructor
    public EnterObjective(Object locationType, Main plugin) throws InvalidTargetLocationException {

        super(ObjectiveType.ENTER, plugin);

        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }

        String target = biomeTarget != null ? ( biomeTarget.name()) : (structureTarget != null ? structureTarget.getName() :
                portalType.portalType().name());

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
