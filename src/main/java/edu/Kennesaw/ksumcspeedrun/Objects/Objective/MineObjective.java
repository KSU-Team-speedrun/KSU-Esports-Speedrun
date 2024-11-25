package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Material;

/**
 * The MineObjective class extends the {@link Objective} class and represents an objective
 * where the player must mine a specific type of block. The class provides different
 * constructors to create a MineObjective with different parameters, such as weight
 * and amount.
 *
 * Attributes:
 * - blockTarget: The target block (of type Material) that the player needs to mine.
 *
 * Constructors:
 * - MineObjective(Material blockTarget, Main plugin): Initializes the objective
 *   with a specified blockTarget and default weight and amount.
 * - MineObjective(Material blockTarget, int weight, Main plugin): Initializes the
 *   objective with a specified blockTarget and weight.
 * - MineObjective(Material blockTarget, int weight, int amount, Main plugin): Initializes the
 *   objective with specified blockTarget, weight, and amount.
 *
 * Methods:
 * - getBlockTarget(): Returns the target block that the player needs to mine.
 */
public class MineObjective extends Objective {

    private final Material blockTarget;

    public MineObjective(Material blockTarget, Main plugin) {
        super(ObjectiveType.MINE, plugin);
        this.blockTarget = blockTarget;
        setTargetName(blockTarget.name());
    }

    public MineObjective(Material blockTarget, int weight, Main plugin) {
        super(ObjectiveType.MINE, weight, plugin);
        this.blockTarget = blockTarget;
        setTargetName(blockTarget.name());
    }

    public MineObjective(Material blockTarget, int weight, int amount, Main plugin) {
        super(ObjectiveType.MINE, weight, amount, plugin);
        this.blockTarget = blockTarget;
        setTargetName(blockTarget.name());
    }

    public Material getBlockTarget() {
        return blockTarget;
    }

}
