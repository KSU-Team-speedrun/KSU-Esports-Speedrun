package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.entity.EntityType;

public class KillObjective extends Objective {

    private EntityType target;

    public KillObjective(EntityType target, ObjectiveType type) {
        super(type);
        this.target = target;
    }

    public KillObjective(EntityType target, ObjectiveType type, int weight) {
        super(type, weight);
        this.target = target;
    }

}
