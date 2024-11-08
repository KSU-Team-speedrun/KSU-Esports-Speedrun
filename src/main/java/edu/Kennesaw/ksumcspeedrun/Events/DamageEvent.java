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

        if (speedrun.isStarted()) {

            if (e.getEntity() instanceof LivingEntity le) {

                if (e.getDamageSource().getCausingEntity() instanceof Player p) {

                    if (le instanceof Player damaged) {

                        if (!PvP) {
                            e.setCancelled(true);
                            return;
                        }

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

                        if (plugin.getSpeedrun().combatTasks.containsKey(uuid)) {
                            plugin.getSpeedrun().combatLog.removeByKey(uuid);
                            plugin.getSpeedrun().combatTasks.get(uuid).cancel();
                            System.out.println("Updating combat log");
                        } else {
                            System.out.println("Adding combat log...");
                        }

                        plugin.getSpeedrun().combatLog.put(uuid, p);

                        ScheduledTask task = plugin.runAsyncDelayed(() -> {
                            plugin.getSpeedrun().combatLog.removeByKey(uuid);
                            plugin.getSpeedrun().combatTasks.remove(uuid);
                        }, 15, TimeUnit.SECONDS);

                        plugin.getSpeedrun().combatTasks.put(uuid, task);

                    });
                }
            }
        }
    }
}
