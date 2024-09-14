package edu.Kennesaw.ksumcspeedrun.Objective;

import org.bukkit.entity.Player;

public abstract class Objective {

    public enum ObjectiveType {
        KILL, MINE, OBTAIN, ENTER
    }

    private ObjectiveType type;
    private int weight;
    private Player completedPlayer;

    public Objective(ObjectiveType type) {
        this.type = type;
        this.weight = 1;
        this.completedPlayer = null;
    }

    public Objective(ObjectiveType type, int weight) {
        this.type = type;
        this.weight = weight;
    }

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
