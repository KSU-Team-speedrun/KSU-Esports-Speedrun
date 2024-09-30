package edu.Kennesaw.ksumcspeedrun.Objective;

import edu.Kennesaw.ksumcspeedrun.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

// Abstract Objective Class, all specific objective classes extend to this class
public abstract class Objective {

    // Define different ObjectiveTypes
    public enum ObjectiveType {
        KILL, MINE, OBTAIN, ENTER
    }

    // Attributes of Objective: ObjectiveType, Weight, & completedPlayer (if objective is finished)
    // TODO: CHANGE "completedPlayer" to "completedTeam" -> CREATE TEAM OBJECT
    private ObjectiveType type;
    private int weight;
    private Player completedPlayer;
    private String targetName;

    Main plugin;

    // Constructor where weight is not explicitly defined (assumed 1, default)
    public Objective(ObjectiveType type, Main plugin) {
        this.type = type;
        this.weight = 1;
        this.completedPlayer = null;
        this.plugin = plugin;
        System.out.println("Objective added");
    }

    // Constructor where weight is explicitly defined
    public Objective(ObjectiveType type, int weight, Main plugin) {
        this.type = type;
        this.weight = weight;
        this.completedPlayer = null;
        this.plugin = plugin;
    }

    // Below are self-explanatory setters & getters

    public ObjectiveType getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public void setComplete(Player p) {
        this.completedPlayer = p;
        p.sendMessage(plugin.getSpeedrunConfig().getPrefix().append(Component.text("Objective Complete: " + getType() + " " + targetName)));

    }

    public Player getCompletePlayer() {
        return completedPlayer;
    }

    public boolean isComplete() {
        return completedPlayer != null;
    }

    public void setTargetName(String target) {
        this.targetName = target;
    }

    public String getTargetName() {
        return targetName;
    }

}
