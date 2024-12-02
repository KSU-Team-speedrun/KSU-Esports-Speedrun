package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * This class manages all teams and holds them in a single list which can be adjusted using the provided methods.
 * It keeps track of all teams and provides mappings for:
 *
 * - Players to their respective teams for quick lookups (`playerTeam` map).
 * - Team names to corresponding team objects for easy access (`teamName` map).
 * - Items to their respective `TrueTeam` objects for handling inventory clicks in the UI (`teamItem` map).
 *
 * Key Features:
 * - Adds, removes, and retrieves teams or their associated data.
 * - Manages team size limits and dynamically adjusts teams based on the limit.
 * - Maintains a `TeamInventory` object for the team selection UI, with a configurable cooldown.
 * - Supports resetting all teams and their mappings to clear the state during a game reset.
 * - Differentiates between abstract `Team` objects and concrete `TrueTeam` objects for specific operations.
 */
public class TeamManager {

    private final Main plugin;

    private List<Team> teams;
    private int teamSizeLimit;

    private TeamInventory teamInventory;
    private final int inventoryCooldown;

    // Map a player to a team so we can get a player's team easily
    private Map<Player, Team> playerTeam;

    // Map a team name component to a team so we can get a team easily
    private Map<Component, Team> teamName;

    // Map the team item to the team so we can easily find out corresponding teams from inventory clicks in the UI
    private Map<ItemStack, TrueTeam> teamItem;


    public TeamManager(Main plugin) {
        teams = new ArrayList<>();
        teamSizeLimit = 4;
        this.plugin = plugin;
        playerTeam = new HashMap<>();
        teamName = new HashMap<>();
        teamItem = new HashMap<>();
        teamInventory = new TeamInventory(this, plugin.getSpeedrunConfig()
                .getComponent("teams.inventory.title"));
        inventoryCooldown = plugin.getConfig().getInt("teams.inventory.cooldown");

    }

    // Add a new team - only true teams have corresponding items
    public void addTeam(Team team) {
        teams.add(team);
        teamName.put(team.getName(), team);
        if (team instanceof TrueTeam trueTeam) teamItem.put(trueTeam.getItem(), trueTeam);

    }

    // Remove a team
    public void removeTeam(Team team) {
        teams.remove(team);
        teamName.remove(team.getName());
        if (team instanceof TrueTeam trueTeam) teamItem.remove(trueTeam.getItem());
    }

    // Get all teams
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * @param replaceSpace - Return stripped names including spaces or replacing spaces with underscores?
     */
    public List<String> getStrippedTeamNames(boolean replaceSpace) {
        List<String> strippedTeamNames = new ArrayList<>();
        for (Team team : teams) {
            strippedTeamNames.add(replaceSpace ? team.getStrippedName().replace(' ', '_') :
                    team.getStrippedName());
        }
        return strippedTeamNames;
    }

    // Get team from component name
    public Team getTeam(Component name) {
        if (teamName.containsKey(name)) {
            return teamName.get(name);
        }
        return null;
    }

    // Get the team of a player
    public Team getTeam(Player p) {
        if (playerTeam.containsKey(p)) {
            return playerTeam.get(p);
        }
        return null;
    }

    // Get the team corresponding to an itemstack
    public TrueTeam getTeam(ItemStack item) {
        if (teamItem.containsKey(item)) {
            return teamItem.get(item);
        }
        return null;
    }

    // Update team size limit
    public void setSizeLimit(int limit) {
        this.teamSizeLimit = limit;

        // Always use null for speedrun.createTeams() -> parameters are only used for debugging
        // Create teams depending on the team size limit and participating players
        plugin.getSpeedrun().createTeams(null);
    }

    // Get team size limit
    public int getSizeLimit() {
        return teamSizeLimit;
    }

    // Map a player to a team
    public void setPlayerTeam(Player p, Team team) {
        playerTeam.put(p, team);
    }

    // Remove player - team mapping
    public void removePlayerTeam(Player p) {
        playerTeam.remove(p);
    }

    // Get all players that are in any team
    public List<Player> getAssignedPlayers() {
        return playerTeam.keySet().stream().toList();
    }

    // Get the team UI inventory
    public TeamInventory getTeamInventory() {
        return teamInventory;
    }

    // Add a team - item mapping
    public void addTeamItem(TrueTeam trueTeam, ItemStack item) {
        teamItem.put(item, trueTeam);
    }

    // Reset the team manager & delete all teams & mappings
    public void reset() {
        teams.clear();
        teams = new ArrayList<>();
        teamSizeLimit = 4;
        playerTeam.clear();
        playerTeam = new HashMap<>();
        teamName.clear();
        teamName = new HashMap<>();
        teamItem.clear();
        teamItem = new HashMap<>();
        teamInventory = new TeamInventory(this, plugin.getSpeedrunConfig()
                .getComponent("teams.inventory.title"));
    }

    // TODO: Remove parameters here -> they are unnecessary
    // Get a list of all true teams (rather than abstract)
    public List<TrueTeam> convertAbstractToTeam(List<Team> teams) {
        List<TrueTeam> trueTeams = new ArrayList<>();
        getTeams().forEach(abstractTeam -> {
            if (abstractTeam instanceof TrueTeam) trueTeams.add(((TrueTeam) abstractTeam));
        });
        return trueTeams;
    }

    // Get cooldown for changing teams
    public int getInventoryCooldown() {
        return inventoryCooldown;
    }

}
