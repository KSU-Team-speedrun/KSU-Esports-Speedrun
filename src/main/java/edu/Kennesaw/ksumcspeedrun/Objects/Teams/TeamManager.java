package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TeamManager {

    private final Main plugin;

    private List<Team> teams;
    private int teamSizeLimit;

    private TeamInventory teamInventory;
    private int inventoryCooldown;

    private Map<Player, Team> playerTeam;
    private Map<Component, Team> teamName;
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

    public void addTeam(Team team) {
        teams.add(team);
        teamName.put(team.getName(), team);
        if (team instanceof TrueTeam trueTeam) teamItem.put(trueTeam.getItem(), trueTeam);

    }

    public void removeTeam(Team team) {
        teams.remove(team);
        teamName.remove(team.getName());
        if (team instanceof TrueTeam trueTeam) teamItem.remove(trueTeam.getItem());
    }

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

    public Team getTeam(Component name) {
        if (teamName.containsKey(name)) {
            return teamName.get(name);
        }
        return null;
    }

    public Team getTeam(Player p) {
        if (playerTeam.containsKey(p)) {
            return playerTeam.get(p);
        }
        return null;
    }

    public TrueTeam getTeam(ItemStack item) {
        if (teamItem.containsKey(item)) {
            return teamItem.get(item);
        }
        return null;
    }

    public void setSizeLimit(int limit) {
        this.teamSizeLimit = limit;
        plugin.getSpeedrun().createTeams(null);
    }

    public int getSizeLimit() {
        return teamSizeLimit;
    }

    public void setPlayerTeam(Player p, Team team) {
        playerTeam.put(p, team);
    }

    public void removePlayerTeam(Player p) {
        playerTeam.remove(p);
    }

    public List<Player> getAssignedPlayers() {
        return playerTeam.keySet().stream().toList();
    }

    public TeamInventory getTeamInventory() {
        return teamInventory;
    }

    public void addTeamItem(TrueTeam trueTeam, ItemStack item) {
        teamItem.put(item, trueTeam);
    }

    public void reset() {
        teams = new ArrayList<>();
        teamSizeLimit = 4;
        playerTeam = new HashMap<>();
        teamName = new HashMap<>();
        teamItem = new HashMap<>();
        teamInventory = new TeamInventory(this, plugin.getSpeedrunConfig()
                .getComponent("teams.inventory.title"));
    }

    public List<TrueTeam> convertAbstractToTeam(List<Team> teams) {
        List<TrueTeam> trueTeams = new ArrayList<>();
        getTeams().forEach(abstractTeam -> {
            if (abstractTeam instanceof TrueTeam) trueTeams.add(((TrueTeam) abstractTeam));
        });
        return trueTeams;
    }

    public int getInventoryCooldown() {
        return inventoryCooldown;
    }

}
