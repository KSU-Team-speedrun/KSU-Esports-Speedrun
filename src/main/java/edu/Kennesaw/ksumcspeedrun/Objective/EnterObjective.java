package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Portal;
import org.bukkit.block.Biome;
import org.bukkit.generator.structure.StructureType;

public class EnterObjective extends Objective {

    private Biome biomeTarget;
    private StructureType structureTarget;
    private Portal portalType;

    public EnterObjective(Biome biomeTarget, ObjectiveType type, int weight) {
        super(type, weight);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(Biome biomeTarget, ObjectiveType type) {
        super(type);
        this.biomeTarget = biomeTarget;
    }

    public EnterObjective(StructureType structureTarget, ObjectiveType type, int weight) {
        super(type, weight);
        this.structureTarget = structureTarget;
    }

    public EnterObjective(StructureType structureTarget, ObjectiveType type) {
        super(type);
        this.structureTarget = structureTarget;
    }

    public EnterObjective(Portal portalType, ObjectiveType type, int weight) {
        super(type, weight);
        this.portalType = portalType;
    }

    public EnterObjective(Portal portalType, ObjectiveType type) {
        super(type);
        this.portalType = portalType;
    }
}
