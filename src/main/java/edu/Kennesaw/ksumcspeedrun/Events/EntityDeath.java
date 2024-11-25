package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.KillObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * The EntityDeath class implements the Listener interface and handles the event when an entity dies in the game.
 * It attributes kills to the appropriate teams and updates their objectives accordingly.
 */
public class EntityDeath implements Listener {

    Main plugin;

    private final Speedrun speedrun;
    private final TeamManager teamManager;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public EntityDeath(Main plugin) {

        this.plugin = plugin;
        speedrun = plugin.getSpeedrun();
        teamManager = speedrun.getTeams();
    }

    // Event is triggered every time an entity dies
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {

        // Only listen if the game is started
        if (speedrun.isStarted()) {

            Team team = null;
            DamageSource ds = e.getDamageSource();

            // If the kill is direct, then we know what team the killer is on
            if (ds.getCausingEntity() instanceof Player p) {

                team = speedrun.getTeams().getTeam(p);

            } else {

                UUID uuid = e.getEntity().getUniqueId();

                /* If the kill is indirect caused by a bed explosion, use the bedlog to determine the killer &
                 the team that is attributed the points */
                if (ds.getDamageType().equals(DamageType.BAD_RESPAWN_POINT)) {

                    if (ds.getDamageLocation() != null) {
                        Location loc = ds.getDamageLocation().toBlockLocation();
                        loc.setWorld(e.getEntity().getWorld());

                        if (plugin.getSpeedrun().bedLog.containsKey(loc)) {

                            Player op = plugin.getSpeedrun().bedLog.get(loc);

                            if (op.isOnline()) {

                                team = teamManager.getTeam(op);

                            }
                        }

                    }

                    // If the killed entity is in the combat log, attribute the death to the player mapped to it
                } else if (plugin.getSpeedrun().combatLog.containsKey(uuid)) {

                    Player op = plugin.getSpeedrun().combatLog.getByKey(uuid);

                    if (op.isOnline()) {

                        team = teamManager.getTeam(op);

                    }

                    plugin.getSpeedrun().combatTasks.get(uuid).cancel();
                    plugin.getSpeedrun().combatTasks.remove(uuid);
                    plugin.getSpeedrun().combatLog.removeByKey(uuid);

                }
            }

            // If the team is successfully determined, continue
            if (team != null) {

                // Loop through every incomplete objective
                for (Objective o : team.getIncompleteObjectives()) {

                /* If any specific incomplete objective is a KILL objective, then cast
                   KillObjective to Objective */
                    if (o.getType().equals(Objective.ObjectiveType.KILL)) {

                        System.out.println("Kill Objective found");

                        KillObjective ko = (KillObjective) o;

                    /* If the target of the KillObjective is equal to the entity that was killed,
                       then the objective is complete */
                        if (ko.getTarget().equals(e.getEntityType())) {

                            if (ko.getHasCount()) {

                                ko.incrementTeam(team);

                                if (ko.getCount(team) >= ko.getAmount()) {

                                    ko.setComplete(team);
                                    break;

                                }

                                continue;

                            }

                            ko.setComplete(team);
                            break;

                        }
                    }
                }
            }
        }
    }
}
