package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.concurrent.TimeUnit;

/**
 * The BedInteract class handles player interactions with beds in the game.
 * It listens for the PlayerBedEnterEvent and asynchronously adds players to a map <bedLocation, player>
 * so that if an entity is killed by a bed explosion, it can be attributed to the player who caused it.
 */
public class BedInteract implements Listener {

    Main plugin;

    public BedInteract(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedInteract(PlayerBedEnterEvent e) {
        plugin.runAsyncTask(() -> {
            if (e.getBedEnterResult().equals(PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE)) {
                Location loc = e.getBed().getLocation().toBlockLocation();
                Player p = e.getPlayer();
                plugin.getSpeedrun().bedLog.put(loc, p);
                plugin.runAsyncDelayed(() -> {
                    plugin.getSpeedrun().bedLog.remove(loc);
                }, 1, TimeUnit.SECONDS);
            }
        });
    }
}
