package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class KillObjective extends Objective {

    private EntityType target;

    public KillObjective(EntityType target) throws NonLivingEntityException {
        super(ObjectiveType.KILL);
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");
        this.target = target;
    }

    public KillObjective(EntityType target, int weight) throws NonLivingEntityException {
        super(ObjectiveType.KILL, weight);
        if (!target.isAlive()) throw new NonLivingEntityException(target.name() + " is not a LivingEntity!");
        this.target = target;
    }

}
