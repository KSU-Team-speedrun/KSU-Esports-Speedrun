package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDrop implements Listener {

    Main plugin;

    public ItemDrop(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!plugin.getSpeedrun().isStarted() && !e.getPlayer().isOp()) e.setCancelled(true);
    }

}
