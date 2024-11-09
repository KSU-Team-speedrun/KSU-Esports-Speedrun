package edu.Kennesaw.ksumcspeedrun.Objects.Objective;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TrueTeam;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Abstract Objective Class, all specific objective classes extend to this class
public abstract class Objective {

    // Define different ObjectiveTypes
    public enum ObjectiveType {
        KILL, MINE, OBTAIN, ENTER
    }

    // Attributes of Objective: ObjectiveType, Weight, & completedPlayer (if objective is finished)
    // TODO: CHANGE "completedPlayer" to "completedTeam" -> CREATE TEAM OBJECT
    private final ObjectiveType type;
    private final int weight;
    private final int amount;

    private Map<Team, Integer> teamCount;
    private boolean hasCount;

    private final List<Team> completedTeams;
    private String targetName;

    Main plugin;

    private boolean sendIncrementMessage = true;

    // Constructor where weight is not explicitly defined (assumed 1, default)
    public Objective(ObjectiveType type, Main plugin) {
        this.type = type;
        this.weight = 1;
        this.amount = 1;
        hasCount = false;
        this.completedTeams = new ArrayList<>();
        sendIncrementMessage = plugin.getSpeedrunConfig().getBoolean("teams.objectiveIncrement");
        this.plugin = plugin;
        System.out.println("Objective added");
    }

    // Constructor where weight is explicitly defined
    public Objective(ObjectiveType type, int weight, Main plugin) {
        this.type = type;
        this.weight = weight;
        this.amount = 1;
        hasCount = false;
        this.completedTeams = new ArrayList<>();
        this.plugin = plugin;
    }

    // Constructor where weight is explicitly defined
    public Objective(ObjectiveType type, int weight, int amount, Main plugin) {
        this.type = type;
        this.weight = weight;
        this.amount = amount;
        this.teamCount = new HashMap<>();
        hasCount = true;
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
        if (team instanceof TrueTeam trueTeam && plugin.getSpeedrun().getTeamsEnabled()) {
            if (amount > 1) {
                for (Player p : trueTeam.getPlayers()) {
                    p.sendMessage(plugin.getMessages().getObjectiveCompleteNumber(type.toString(),
                            targetName, amount, weight));
                }
            } else {
                for (Player p : trueTeam.getPlayers()) {
                    p.sendMessage(plugin.getMessages().getObjectiveComplete(type.toString(), targetName, weight));
                }
            }
        } else if (team instanceof SoloTeam soloTeam && !plugin.getSpeedrun().getTeamsEnabled()) {
            if (amount > 1) {
                soloTeam.sendMessage(plugin.getMessages().getObjectiveCompleteNumber(type.toString(),
                        targetName, amount, weight));
            } else {
                soloTeam.sendMessage(plugin.getMessages().getObjectiveComplete(type.toString(), targetName, weight));
            }
        }
        this.completedTeams.add(team);
        team.addPoints(weight);
        team.addCompleteObjective(this);
    }

    public boolean isIncomplete(Team team) {
        return !completedTeams.contains(team);
    }

    public void setTargetName(String target) {
        this.targetName = target;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getAmount() {
        return amount;
    }

    public void addTeam(Team team) {
        if (hasCount) {
            if (teamCount != null) {
                teamCount.put(team, 0);
            }
        }
    }

    public void incrementTeam(Team team) {
        if (hasCount && teamCount.containsKey(team)) {
            int currentAmount = teamCount.get(team) + 1;
            teamCount.put(team, currentAmount);
            if (sendIncrementMessage) {
                if (currentAmount != amount) {
                    if (team instanceof TrueTeam trueTeam && plugin.getSpeedrun().getTeamsEnabled()) {
                        for (Player p : trueTeam.getPlayers()) {
                            p.sendMessage(plugin.getMessages().getObjectiveIncrement(type.toString(),
                                    targetName, currentAmount, amount, weight));
                        }
                    } else if (team instanceof SoloTeam soloTeam && !plugin.getSpeedrun().getTeamsEnabled()) {
                        soloTeam.sendMessage(plugin.getMessages().getObjectiveIncrement(type.toString(),
                                targetName, currentAmount, amount, weight));
                    }
                }
            }
        }
    }

    public void setIncrementNumber(Team team, int number) {
        if (hasCount && teamCount.containsKey(team)) {
            teamCount.put(team, number);
            if (sendIncrementMessage) {
                if (number != amount) {
                    if (team instanceof TrueTeam trueTeam && plugin.getSpeedrun().getTeamsEnabled()) {
                        for (Player p : trueTeam.getPlayers()) {
                            p.sendMessage(plugin.getMessages().getObjectiveIncrement(type.toString(),
                                    targetName, number, amount, weight));
                        }
                    } else if (team instanceof SoloTeam soloTeam && !plugin.getSpeedrun().getTeamsEnabled()) {
                        soloTeam.sendMessage(plugin.getMessages().getObjectiveIncrement(type.toString(),
                                targetName, number, amount, weight));
                    }
                }
            }
        }
    }

    public int getCount(Team team) {
        if (hasCount && teamCount.containsKey(team)) {
            return teamCount.get(team);
        }
        return -1;
    }

    public boolean getHasCount() {
        return hasCount;
    }

}
