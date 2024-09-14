package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.block.Biome;

public class EnterObjective extends Objective {

    private Biome biomeTarget;
    private SRStructure structureTarget;
    private Portal portalType;

    public EnterObjective(Biome biomeTarget, int weight) {
        super(ObjectiveType.ENTER, weight);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(Biome biomeTarget) {
        super(ObjectiveType.ENTER);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(SRStructure structureTarget, int weight) {
        super(ObjectiveType.ENTER, weight);
        this.structureTarget = structureTarget;
    }

    public EnterObjective(SRStructure structureTarget) {
        super(ObjectiveType.ENTER);
        this.structureTarget = structureTarget;
    }

    public EnterObjective(Portal portalType, int weight) {
        super(ObjectiveType.ENTER, weight);
        this.portalType = portalType;
    }

    public EnterObjective(Portal portalType) {
        super(ObjectiveType.ENTER);
        this.portalType = portalType;
    }

    public EnterObjective(Object locationType, int weight) throws InvalidTargetLocationException {
        super(ObjectiveType.ENTER, weight);
        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }
    }

    public EnterObjective(Object locationType) throws InvalidTargetLocationException {
        super(ObjectiveType.ENTER);
        switch (locationType) {
            case Biome biome -> this.biomeTarget = biome;
            case SRStructure structure -> this.structureTarget = structure;
            case Portal portal -> this.portalType = portal;
            case null, default ->
                    throw new InvalidTargetLocationException("Object locationType must be instance of Biome, Structure, or Portal");
        }
    }

    public Object getTarget() {
        if (biomeTarget != null) return biomeTarget;
        else if (structureTarget != null) return structureTarget;
        else if (portalType != null) return portalType;
        else return null;
    }

}
