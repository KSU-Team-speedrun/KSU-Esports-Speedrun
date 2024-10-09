package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

    private List<Team> completedTeams;
    private String targetName;

    Main plugin;

    // Constructor where weight is not explicitly defined (assumed 1, default)
    public Objective(ObjectiveType type, Main plugin) {
        this.type = type;
        this.weight = 1;
        this.completedTeams = new ArrayList<>();
        this.plugin = plugin;
        System.out.println("Objective added");
    }

    // Constructor where weight is explicitly defined
    public Objective(ObjectiveType type, int weight, Main plugin) {
        this.type = type;
        this.weight = weight;
        this.completedTeams = new ArrayList<>();
        this.plugin = plugin;
    }

    // Below are self-explanatory setters & getters

    public ObjectiveType getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public void setComplete(Team team) {

        this.completedTeams.add(team);
        team.addPoints(weight);

        for (Player p : team.getPlayers()) {
            p.sendMessage(plugin.getSpeedrunConfig().getPrefix().append(Component.text("Objective Complete: " + getType() + " " + targetName)));
        }

    }

    public List<Team> getCompleteTeams() {
        return completedTeams;
    }

    public boolean isComplete(Team team) {
        return completedTeams.contains(team);
    }

    public void setTargetName(String target) {
        this.targetName = target;
    }

    public String getTargetName() {
        return targetName;
    }

}
