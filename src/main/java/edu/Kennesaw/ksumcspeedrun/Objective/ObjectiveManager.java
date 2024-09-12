package edu.Kennesaw.ksumcspeedrun.Objective;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveManager {

    private final List<Objective> objectives;

    public ObjectiveManager() {
        this.objectives = new ArrayList<>();
    }

    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public int getLength() {
        return objectives.size();
    }

    public void removeObjective(int number) {
        objectives.remove(number);
    }

}
