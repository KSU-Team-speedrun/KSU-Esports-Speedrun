package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.List;

public abstract class Team {

    Main plugin;

    public Team(Main plugin) {
        this.plugin = plugin;
    }

    public abstract Component getName();

    public abstract int getPoints();

    public abstract void addPoints(int points);

    public abstract List<Objective> getIncompleteObjectives();

    public abstract List<Objective> getCompleteObjectives();

    public abstract void addCompleteObjective(Objective o);

    public abstract String getStrippedName();

    public abstract Location getRespawnLocation();

    public abstract void setRespawnLocation(Location location);

}
