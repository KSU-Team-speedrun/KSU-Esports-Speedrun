package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a SoloTeam, which is a type of Team with a single player.
 * A SoloTeam has points, completed objectives, and other player-specific attributes.
 * Most functions below pass their parameters on to the player, effectively making this class "extend",
 * but only with the functions needed for this plugin as well as some implemented to support teams.
 */
public class SoloTeam extends Team {

    Main plugin;
    private final List<Objective> completedObjectives;
    private final Player player;
    private int points;
    private Location respawnLocation;

    private OfflinePlayer playerBackup;

    public SoloTeam(Main plugin, Player player) {
        super(plugin);
        this.player = player;
        this.plugin = plugin;
        this.completedObjectives = new ArrayList<>();
        points = 0;
        plugin.getSpeedrun().getTeams().setPlayerTeam(player, this);
        playerBackup = player;
    }

    @Override
    public Component getName() {
        return player.displayName();
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

    public void removePoints(int points) {
        this.points -= points;
    }

    public List<Objective> getIncompleteObjectives() {
        return plugin.getSpeedrun().getObjectives().getIncompleteObjectives(this);
    }

    public List<Objective> getCompleteObjectives() {
        return completedObjectives;
    }

    public void addCompleteObjective(Objective o) {
        completedObjectives.add(o);
    }

    @Override
    public String getStrippedName() {
        return player.getName();
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    public Inventory getInventory() {
        return player.getInventory();
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public World getWorld() {
        return player.getWorld();
    }

    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    public void teleport(Location location) {
        player.teleport(location);
    }

    public void setScoreboard(Scoreboard scoreboard) {
        player.setScoreboard(scoreboard);
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location location) {
        this.respawnLocation = location;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayerBackup(OfflinePlayer op) {
        this.playerBackup = op;
    }

    public OfflinePlayer getPlayerBackup() {
        return playerBackup;
    }

}
