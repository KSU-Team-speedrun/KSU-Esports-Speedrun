package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.UUID;

public class EntityDeath implements Listener {

    Main plugin;

    private final Speedrun speedrun;

    List<Objective> incompleteObjectives;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public EntityDeath(Main plugin) {

        this.plugin = plugin;
        speedrun = plugin.getSpeedrun();
    }

    // Event is triggered every time an entity dies
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        System.out.println("Death logged");

        // Check if the speedrun is started
        //if (speedrun.isStarted()) {

            // Loop through every incomplete objective
            for (Objective o : speedrun.getObjectives().getIncompleteObjectives()) {

                /* If any specific incomplete objective is a KILL objective, then cast
                   KillObjective to Objective */
                if (o.getType().equals(Objective.ObjectiveType.KILL)) {

                    System.out.println("Kill Objective found");

                    KillObjective ko = (KillObjective) o;

                    /* If the target of the KillObjective is equal to the entity that was killed,
                       then the objective is complete */
                    if (ko.getTarget().equals(e.getEntityType())) {

                        System.out.println("Target matched");

                        DamageSource ds = e.getDamageSource();

                        if (ds.getCausingEntity() instanceof Player p) {

                            ko.setComplete(p);

                        } else {

                            System.out.println("Causing Entity not Player");

                            UUID uuid = e.getEntity().getUniqueId();

                            if (ds.getDamageType().equals(DamageType.BAD_RESPAWN_POINT)) {

                                if (ds.getDamageLocation() != null) {
                                    Location loc = ds.getDamageLocation().toBlockLocation();
                                    loc.setWorld(e.getEntity().getWorld());

                                    if (plugin.getSpeedrun().bedLog.containsKey(loc)) {

                                        Player op = plugin.getSpeedrun().bedLog.get(loc);

                                        if (op.isOnline()) {

                                            ko.setComplete(op);

                                        }
                                    }

                                }

                            } else if (plugin.getSpeedrun().combatLog.containsKey(uuid)) {

                                Player op = plugin.getSpeedrun().combatLog.get(uuid);

                                if (op.isOnline()) {

                                    ko.setComplete(op);

                                }

                                plugin.getSpeedrun().combatTasks.get(uuid).cancel();
                                plugin.getSpeedrun().combatTasks.remove(uuid);
                                plugin.getSpeedrun().combatLog.remove(uuid);

                            }
                        }
                    }
                }
            }
        //}
    }
}
