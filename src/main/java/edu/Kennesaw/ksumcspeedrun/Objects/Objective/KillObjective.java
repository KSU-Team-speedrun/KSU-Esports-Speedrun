package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.entity.EntityType;

/**
 * Represents a kill objective in the game, where the player must kill a specified entity type.
 * This class is a specific type of {@link Objective} aimed at eliminating a certain type of living entity.
 * Attributes:
 * - target: A living entity
 *
 * Constructors:
 * - KillObjective(EntityType target, int weight, Main plugin): Initializes the objective
 *   with a specified target and weight.
 * - KillObjective(EntityType target, int weight, int amount, Main plugin): Initializes the
 *   objective with specified target, weight, and amount.
 * - KillObjective(EntityType target, Main plugin): Initializes the objective with a
 *   specified target and default weight of 1 & amount of 1.
 *
 * Methods:
 * - getTarget(): Returns the target object, which is an entity
 */
// KillObjective is a subclass of Objective
public class KillObjective extends Objective {

    // Only specific attribute is target (EntityType), on top of superclass Objective attributes
    private final EntityType target;

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

    // Same as above, but weight & amount included in constructor
    public KillObjective(EntityType target, int weight, int amount, Main plugin) throws NonLivingEntityException {

        super(ObjectiveType.KILL, weight, amount, plugin);
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");
        this.target = target;

        setTargetName(target.name());

    }

    // Return target EntityType
    public EntityType getTarget() {
        return target;
    }

}
