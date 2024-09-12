package edu.Kennesaw.ksumcspeedrun.Objective;

public abstract class Objective {

    public enum ObjectiveType {
        KILL, MINE, OBTAIN, ENTER
    }

    private ObjectiveType type;
    private int weight;

    public Objective(ObjectiveType type) {
        this.type = type;
        this.weight = 1;
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

}
