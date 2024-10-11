package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Team {

    Main plugin;

    private final String name;
    private int points = 0;
    private final List<Player> players = new ArrayList<>();


    public Team(Main plugin, String teamName) {
        this.plugin = plugin;
        this.name = teamName;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }

    public void changePlayer(Player player, Team team) {
        removePlayer(player);
        team.addPlayer(player);
    }

    public void changeLastPlayer(Team team) {
        Player last = players.getLast();
        changePlayer(last, team);
    }

    public String getName() {
        return this.name;
    }

    public int getPoints() {
        return this.points;
    }

    public void addPoints(int points) {
        this.points += points;
        if (this.points >= plugin.getSpeedrun().getTotalWeight()) {
            plugin.getSpeedrun().endGame(this);
        }
    }

    public List<Objective> getCompletedObjectives() {
        List<Objective> completedObjectives = new ArrayList<>();
        for (Objective o : plugin.getSpeedrun().getObjectives().getObjectives()) {
            if (o.getCompleteTeams().contains(this)) {
                completedObjectives.add(o);
            }
        }
        return completedObjectives;
    }

    public List<Objective> getIncompleteObjectives() {
        return plugin.getSpeedrun().getObjectives().getIncompleteObjectives(this);
    }

}
