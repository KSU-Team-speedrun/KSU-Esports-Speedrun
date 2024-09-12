package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.Material;

public class ObtainObjective extends Objective {

    private Material item;
    private int amount;

    public ObtainObjective(Material item) {
        super(ObjectiveType.OBTAIN);
        this.item = item;
    }

    public ObtainObjective(Material item, int weight) {
        super(ObjectiveType.OBTAIN, weight);
        this.item = item;
    }

    public ObtainObjective(Material item, int weight, int amount) {
        super(ObjectiveType.OBTAIN, weight);
        this.item = item;
        this.amount = amount;
    }
}
