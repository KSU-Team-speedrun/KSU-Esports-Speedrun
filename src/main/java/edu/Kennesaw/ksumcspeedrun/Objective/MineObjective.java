package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.Material;

public class MineObjective extends Objective {

    private Material blockTarget;

    public MineObjective(Material blockTarget, ObjectiveType type, int weight) {
        super(type, weight);
        this.blockTarget = blockTarget;
    }

    public MineObjective(Material blockTarget, ObjectiveType type) {
        super(type);
        this.blockTarget = blockTarget;
    }
}
