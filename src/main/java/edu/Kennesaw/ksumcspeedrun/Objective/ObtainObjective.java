package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.Material;

public class ObtainObjective extends Objective {

    private Material item;
    private int amount;

    public ObtainObjective(Material item, ObjectiveType type) {
        super(type);
        this.item = item;
    }

    public ObtainObjective(Material item, ObjectiveType type, int weight) {
        super(type, weight);
        this.item = item;
    }

    public ObtainObjective(Material item, ObjectiveType type, int weight, int amount) {
        super(type, weight);
        this.item = item;
        this.amount = amount;
    }
}
