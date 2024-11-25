package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Material;

/**
 * Represents an obtain objective in the game, where the player must gather a specified material type.
 * This class is a specific type of {@link Objective} aimed at collecting a certain type of material.
 *
 * Attributes:
 * - item: The material that needs to be obtained
 *
 * Constructors:
 * - ObtainObjective(Material item, Main plugin): Initializes the objective with
 *   a specified item and default weight of 1 and amount of 1.
 * - ObtainObjective(Material item, int weight, Main plugin): Initializes the objective
 *   with a specified item and weight.
 * - ObtainObjective(Material item, int weight, int amount, Main plugin): Initializes the
 *   objective with a specified item, weight, and amount.
 *
 * Methods:
 * - getItem(): Returns the target material.
 */
public class ObtainObjective extends Objective {

    private final Material item;

    public ObtainObjective(Material item, Main plugin) {
        super(ObjectiveType.OBTAIN, plugin);
        this.item = item;
        setTargetName(item.name());
    }

    public ObtainObjective(Material item, int weight, Main plugin) {
        super(ObjectiveType.OBTAIN, weight, plugin);
        this.item = item;
        setTargetName(item.name());
    }

    public ObtainObjective(Material item, int weight, int amount, Main plugin) {
        super(ObjectiveType.OBTAIN, weight, amount, plugin);
        this.item = item;
        setTargetName(item.name());
    }

    public Material getItem() {
        return item;
    }

}
