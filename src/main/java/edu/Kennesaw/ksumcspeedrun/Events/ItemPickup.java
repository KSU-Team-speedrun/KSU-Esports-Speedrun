package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

/**
 * The ItemDrop class prevents players from picking up any dropped items before the game starts.
 */
public class ItemPickup implements Listener {

    Main plugin;

    public ItemPickup(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemPickup(PlayerAttemptPickupItemEvent e) {
        if (!plugin.getSpeedrun().isStarted() && !e.getPlayer().isOp()) e.setCancelled(true);
    }

}
