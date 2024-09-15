package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.Material;

/* Same as other objective subclasses, Material is target
   For ObtainObjective, there is an additional possible flag: amount
   The number of items needed to be obtained can be specified */

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
