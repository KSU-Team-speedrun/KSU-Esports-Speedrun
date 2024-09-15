package edu.Kennesaw.ksumcspeedrun.Objective;

import java.util.ArrayList;
import java.util.List;

/* This class manages all Objective and holds them in a single list which can be adjusted using the methods
   included below */
public class ObjectiveManager {

    // An instance of ObjectiveManager holds a list of Objectives
    private final List<Objective> objectives;

    // Class is initialized with an empty constructor, an empty list of Objectives is made
    public ObjectiveManager() {
        this.objectives = new ArrayList<>();
    }

    // Add an objective to the objective list
    public void addObjective(Objective objective) {
        objectives.add(objective);
    }

    // Returns the list of objectives
    public List<Objective> getObjectives() {
        return objectives;
    }

    // Returns the list of incomplete objectives
    public List<Objective> getIncompleteObjectives() {
        List<Objective> incompleteObjectives = new ArrayList<>();
        for (Objective o : objectives) {
            if (!o.isComplete()) {
                incompleteObjectives.add(o);
            }
        }
        return incompleteObjectives;
    }

    // Returns the length of the list of objectives
    public int getLength() {
        return objectives.size();
    }

    // Remove objective by the number that it appears in the list
    public void removeObjective(int number) {
        objectives.remove(number);
    }

}
