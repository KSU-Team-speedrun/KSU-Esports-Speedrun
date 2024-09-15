package edu.Kennesaw.ksumcspeedrun.Objective;

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

    // Constructor where weight is not explicitly defined (assumed 1, default)
    public Objective(ObjectiveType type) {
        this.type = type;
        this.weight = 1;
        this.completedPlayer = null;
    }

    // Constructor where weight is explicitly defined
    public Objective(ObjectiveType type, int weight) {
        this.type = type;
        this.weight = weight;
        this.completedPlayer = null;
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
    }

    public Player getCompletePlayer() {
        return completedPlayer;
    }

    public boolean isComplete() {
        return completedPlayer != null;
    }
}
