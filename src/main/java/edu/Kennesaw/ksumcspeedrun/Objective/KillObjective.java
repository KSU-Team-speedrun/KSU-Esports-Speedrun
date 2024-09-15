package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import org.bukkit.entity.EntityType;

// KillObjective is a subclass of Objective
public class KillObjective extends Objective {

    // Only specific attribute is target (EntityType), on top of superclass Objective attributes
    private final EntityType target;

    // KillObjective is initialized with an EntityType target
    public KillObjective(EntityType target) throws NonLivingEntityException {

        // ObjectiveType.KILL passed to Objective Superclass
        super(ObjectiveType.KILL);

        // EntityType must be a Living Entity
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");

        this.target = target;
    }

    // Same as above, but weight included in constructor
    public KillObjective(EntityType target, int weight) throws NonLivingEntityException {

        super(ObjectiveType.KILL, weight);
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");
        this.target = target;

    }

    // Return target EntityType
    public EntityType getTarget() {
        return target;
    }

}
