package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.Material;

// Same as other Objective Subclasses, this time a block is a target
public class MineObjective extends Objective {

    private Material blockTarget;

    public MineObjective(Material blockTarget, int weight) {
        super(ObjectiveType.MINE, weight);
        this.blockTarget = blockTarget;
    }

    public MineObjective(Material blockTarget) {
        super(ObjectiveType.MINE);
        this.blockTarget = blockTarget;
    }
}
