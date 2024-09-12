package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeath implements Listener {

    Main plugin;

    private final ObjectiveManager objectives;
    private final Speedrun speedrun;

    public EntityDeath(Main plugin) {

        this.plugin = plugin;
        speedrun = plugin.getSpeedrun();
        objectives = speedrun.getObjectives();

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (speedrun.isStarted()) {
            for (Objective o : objectives.getObjectives()) {
                if (o.getType().equals(Objective.ObjectiveType.KILL)) {
                    KillObjective ko = (KillObjective) o;
                    if (ko.getTarget().equals(e.getEntityType())) {
                        // target entity is killed
                    }
                }
            }
        }
    }
}
