package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class EntityDeath implements Listener {

    Main plugin;

    private final Speedrun speedrun;

    List<Objective> incompleteObjectives;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public EntityDeath(Main plugin) {

        this.plugin = plugin;
        speedrun = plugin.getSpeedrun();
        ObjectiveManager objectives = speedrun.getObjectives();
        incompleteObjectives = objectives.getIncompleteObjectives();

    }

    // Event is triggered every time an entity dies
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        // Check if the speedrun is started
        if (speedrun.isStarted()) {

            // Loop through every incomplete objective
            for (Objective o : incompleteObjectives) {

                /* If any specific incomplete objective is a KILL objective, then cast
                   KillObjective to Objective */
                if (o.getType().equals(Objective.ObjectiveType.KILL)) {

                    KillObjective ko = (KillObjective) o;

                    /* If the target of the KillObjective is equal to the entity that was killed,
                       then the objective is complete */
                    if (ko.getTarget().equals(e.getEntityType())) {

                        // target entity is killed

                    }
                }
            }
        }
    }
}
