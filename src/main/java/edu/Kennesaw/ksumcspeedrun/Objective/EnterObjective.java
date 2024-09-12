package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Portal;
import org.bukkit.block.Biome;
import org.bukkit.generator.structure.StructureType;

public class EnterObjective extends Objective {

    private Biome biomeTarget;
    private StructureType structureTarget;
    private Portal portalType;

    public EnterObjective(Biome biomeTarget, int weight) {
        super(ObjectiveType.ENTER, weight);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(Biome biomeTarget) {
        super(ObjectiveType.ENTER);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(StructureType structureTarget, int weight) {
        super(ObjectiveType.ENTER, weight);
        this.structureTarget = structureTarget;
    }

    public EnterObjective(StructureType structureTarget) {
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
}
