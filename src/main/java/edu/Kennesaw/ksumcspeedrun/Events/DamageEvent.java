package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the event where an entity, typically a player, takes damage.
 * It checks various conditions to determine whether the damage event should proceed or be cancelled.
 * Additionally, it manages combat logging mechanisms, ensuring that player damage events are properly logged and handled.
 */
public class DamageEvent implements Listener {

    Main plugin;
    Speedrun speedrun;
    TeamManager tm;
    boolean teamPvP = false;
    boolean PvP = true;

    public DamageEvent(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        tm = speedrun.getTeams();
        teamPvP = plugin.getSpeedrunConfig().getBoolean("teams.teamPvP");
        PvP = plugin.getSpeedrunConfig().getBoolean("teams.PvP");
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {

        // Player Damage events only listened for when a game is started
        if (speedrun.isStarted()) {

            // If the damaged entity is a living entity and the damage source is a player, continue..
            if (e.getEntity() instanceof LivingEntity le) {

                if (e.getDamageSource().getCausingEntity() instanceof Player p) {

                    if (le instanceof Player damaged) {

                        // If the damaged entity is a player and pvp is disabled, cancel
                        if (!PvP) {
                            e.setCancelled(true);
                            return;
                        }

                        // If the damaged entity is a player on the same team as the damager, cancel unless enabled
                        if (tm.getTeam(p).equals(tm.getTeam(damaged))) {

                            if (!teamPvP) {
                                e.setCancelled(true);
                                return;
                            }

                        }

                    }

                    plugin.runAsyncTask(() -> {

                        System.out.println("Logging player damage");

                        UUID uuid = le.getUniqueId();

                        /* If the combat log already contains the damaged entity, remove it and cancel the task
                           set to remove it after 15 seconds */
                        if (plugin.getSpeedrun().combatTasks.containsKey(uuid)) {
                            plugin.getSpeedrun().combatLog.removeByKey(uuid);
                            plugin.getSpeedrun().combatTasks.get(uuid).cancel();
                            System.out.println("Updating combat log");
                        } else {
                            System.out.println("Adding combat log...");
                        }

                        // Add the entity to the combat log mapped to the attacker
                        plugin.getSpeedrun().combatLog.put(uuid, p);

                        /* After 15 seconds, remove the entity from the combat log & delete the mapping for the
                           remove task */
                        ScheduledTask task = plugin.runAsyncDelayed(() -> {
                            plugin.getSpeedrun().combatLog.removeByKey(uuid);
                            plugin.getSpeedrun().combatTasks.remove(uuid);
                        }, 15, TimeUnit.SECONDS);

                        // Also map the entity to the async task so that it can be canceled if necessary
                        plugin.getSpeedrun().combatTasks.put(uuid, task);

                    });
                }
            }
        }
    }
}
