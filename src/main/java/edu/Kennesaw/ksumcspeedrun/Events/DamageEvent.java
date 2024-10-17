package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
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

    public DamageEvent(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();

    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {

        if (speedrun.isStarted()) {

            if (e.getEntity() instanceof LivingEntity le) {

                if (e.getDamageSource().getCausingEntity() instanceof Player p) {

                    Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> {

                        System.out.println("Logging player damage");

                        UUID uuid = le.getUniqueId();

                        if (plugin.getSpeedrun().combatTasks.containsKey(uuid)) {
                            plugin.getSpeedrun().combatLog.remove(uuid);
                            plugin.getSpeedrun().combatTasks.get(uuid).cancel();
                            System.out.println("Updating combat log");
                        } else {
                            System.out.println("Adding combat log...");
                        }

                        plugin.getSpeedrun().combatLog.put(uuid, p);

                        ScheduledTask task = Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask1 -> {
                            plugin.getSpeedrun().combatLog.remove(uuid);
                            plugin.getSpeedrun().combatTasks.remove(uuid);
                        }, 10, TimeUnit.SECONDS);

                        plugin.getSpeedrun().combatTasks.put(uuid, task);

                    });
                }
            }
        }
    }
}
