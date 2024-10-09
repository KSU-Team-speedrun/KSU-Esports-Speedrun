package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.List;

public class ItemObtain implements Listener {

    Main plugin;

    private final Speedrun speedrun;

    List<Objective> incompleteObjectives;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public ItemObtain(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        //incompleteObjectives = speedrun.getObjectives().getIncompleteObjectives();

    }

    // TODO - ADD LOGIC TO THESE EVENT LISTENERS BELOW

    @EventHandler
    public void pickupItem(EntityPickupItemEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player p) {
            // Player Pickup Item Event
        }
    }

    @EventHandler
    public void craftItem(CraftItemEvent e) {
        // Player Craft Item Event
    }
}
