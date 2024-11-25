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

/**
 * Abstract class representing an objective that teams or players need to complete.
 * Specific types of objectives extend this class.
 *
 * The Objective class serves as the blueprint for various objectives in the game.
 * Objectives can be defined by their type, weight, count, and completion status.
 * The class manages the state and behaviors related to these attributes,
 * supporting both individual and team-based objectives.
 *
 * Attributes:
 * - type: The nature of the objective (e.g., KILL, MINE, OBTAIN, ENTER)
 * - weight: The importance or points associated with completing the objective
 * - amount: The required amount to achieve the objective (used for cumulative objectives)
 * - teamCount: Tracks the progress of different teams regarding the objective
 * - hasCount: Indicates if the objective requires a count greater than one
 * - completedTeams: List of teams that have accomplished the objective
 * - targetName: Name of the target related to the objective, primarily for messaging
 * - plugin: Reference to the main plugin instance
 * - sendIncrementMessage: Flag indicating if messages should be sent when the objective's count increments
 *
 * Constructors:
 * - Objective(ObjectiveType type, Main plugin): Assumes default weight and amount
 * - Objective(ObjectiveType type, int weight, Main plugin): Specifies weight with a default amount
 * - Objective(ObjectiveType type, int weight, int amount, Main plugin): Clearly defines weight and amount
 *
 * Methods:
 * - getType(): Returns the objective type
 * - getWeight(): Returns the objective's weight
 * - setComplete(Team team): Marks the objective as complete for a specific team and handles the relevant messaging and point awarding
 * - isIncomplete(Team team): Checks if an objective remains incomplete for a given team
 * - setTargetName(String target): Sets the target name used in messages
 * - getTargetName(): Retrieves the target name
 * - getAmount(): Returns the required amount for the objective
 * - addTeam(Team team): Initializes the team's count if the objective tracks increments
 * - incrementTeam(Team team): Increments the team's count for the objective and sends increment messages if enabled
 * - setIncrementNumber(Team team, int number): Directly sets the team's count for the objective
 * - getCount(Team team): Retrieves the current count for a team
 * - getHasCount(): Checks if the objective tracks a count
 *
 * Usage:
 * Objective killObjective = new KillObjective(ObjectiveType.KILL, 10, plugin);
 * Team team = new TrueTeam("TeamName");
 * killObjective.addTeam(team);
 * killObjective.incrementTeam(team);
 *
 * This class serves as a core component in the game's speedrun mode,
 * enabling the creation and management of diverse objectives for players and teams.
 */
public abstract class Objective {

    // Define different ObjectiveTypes
    public enum ObjectiveType {
        KILL, MINE, OBTAIN, ENTER
    }

    // Attributes of Objective: ObjectiveType, Weight, & completedPlayer (if objective is finished)
    private final ObjectiveType type;
    private final int weight;
    private final int amount;

    /* Team count is the increment a specific team is at (e.g., objective: KILL 10 ZOMBIES, teamCount is the number
       of zombies killed at any time) */
    private Map<Team, Integer> teamCount;

    // True if the objective has a set count required for completion that is larger than 1
    private final boolean hasCount;

    // List of teams who have completed the objectives
    private final List<Team> completedTeams;

    // Mostly used for messages
    private String targetName;

    Main plugin;

    // Send all teammates a message when the objective count is incremented?
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

    // Returns the objective type (KILL, MINE, OBTAIN, or ENTER)
    public ObjectiveType getType() {
        return type;
    }

    // Returns the weight of the objective
    public int getWeight() {
        return weight;
    }

    // Set the objective as complete for a specific team & send teammates a message
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
        // Add the team to the list of completed team
        this.completedTeams.add(team);

        // Award the team the points
        team.addPoints(weight);

        // Add this objective to the team's completed objective list
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

    // Objectives only need to hold team information for purposes of counting
    public void addTeam(Team team) {
        if (hasCount) {
            if (teamCount != null) {
                teamCount.put(team, 0);
            }
        }
    }

    // If the objective keeps counts & the team count contains the relevant team, increment the team count
    public void incrementTeam(Team team) {
        if (hasCount && teamCount.containsKey(team)) {
            int currentAmount = teamCount.get(team) + 1;
            teamCount.put(team, currentAmount);

            // If increment messages are enabled, send all players on the team an increment message
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

    // Same as above, but the number can be specified rather than incremented
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

    // Return the relevant team count
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
