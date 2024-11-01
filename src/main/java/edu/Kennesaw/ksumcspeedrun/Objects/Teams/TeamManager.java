package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.TeamInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TeamManager {

    private final Main plugin;

    private List<Team> teams;
    private int teamSizeLimit;

    private TeamInventory teamInventory;

    private Map<Player, Team> playerTeam;
    private Map<Component, Team> teamName;
    private Map<ItemStack, Team> teamItem;


    public TeamManager(Main plugin) {
        teams = new ArrayList<>();
        teamSizeLimit = 4;
        this.plugin = plugin;
        playerTeam = new HashMap<>();
        teamName = new HashMap<>();
        teamItem = new HashMap<>();
        teamInventory = new TeamInventory(this, plugin.getSpeedrunConfig()
                .getComponent("teams.inventory.title"));

    }

    public void addTeam(Team team) {
        teams.add(team);
        teamName.put(team.getName(), team);
        teamItem.put(team.getItem(), team);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
        teamName.remove(team.getName());
        teamItem.remove(team.getItem());
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getTeam(Component team) {
        if (teamName.containsKey(team)) {
            return teamName.get(team);
        }
        return null;
    }

    public Team getTeam(Player p) {
        if (playerTeam.containsKey(p)) {
            return playerTeam.get(p);
        }
        return null;
    }

    public Team getTeam(ItemStack item) {
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

    public void addTeamItem(Team team, ItemStack item) {
        teamItem.put(item, team);
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

}
