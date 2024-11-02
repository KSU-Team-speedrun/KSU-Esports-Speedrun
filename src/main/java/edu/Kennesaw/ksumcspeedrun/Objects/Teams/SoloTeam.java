package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class SoloTeam {

    Main plugin;
    private final List<Objective> completedObjectives;
    private final Player player;
    private int points;

    public SoloTeam(Main plugin, Player player) {
        this.player = player;
        this.plugin = plugin;
        this.completedObjectives = new ArrayList<>();
        points = 0;
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

    public List<Objective> getIncompleteObjectives() {
        return plugin.getSpeedrun().getObjectives().getIncompleteObjectives(this);
    }

    public List<Objective> getCompleteObjectives() {
        return completedObjectives;
    }

    public void addCompleteObjective(Objective o) {
        completedObjectives.add(o);
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    public Inventory getInventory() {
        return player.getInventory();
    }

    public Component displayName() {
        return player.displayName();
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


}
