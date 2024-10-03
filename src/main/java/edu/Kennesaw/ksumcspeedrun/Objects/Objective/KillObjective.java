package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.entity.EntityType;

// KillObjective is a subclass of Objective
public class KillObjective extends Objective {

    // Only specific attribute is target (EntityType), on top of superclass Objective attributes
    private final EntityType target;

    Main plugin;

    // KillObjective is initialized with an EntityType target
    public KillObjective(EntityType target, Main plugin) throws NonLivingEntityException {

        // ObjectiveType.KILL passed to Objective Superclass
        super(ObjectiveType.KILL, plugin);

        // EntityType must be a Living Entity
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");

        this.target = target;

        setTargetName(target.name());

    }

    // Same as above, but weight included in constructor
    public KillObjective(EntityType target, int weight, Main plugin) throws NonLivingEntityException {

        super(ObjectiveType.KILL, weight, plugin);
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");
        this.target = target;

        setTargetName(target.name());

    }

    // Return target EntityType
    public EntityType getTarget() {
        return target;
    }

}
