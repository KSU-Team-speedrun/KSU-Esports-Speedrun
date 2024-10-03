package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class MineBlock implements Listener {

    Main plugin;
    private final Speedrun speedrun;
    List<Objective> incompleteObjectives;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public MineBlock(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        //incompleteObjectives = speedrun.getObjectives().getIncompleteObjectives();;

    }

    // TODO - ADD LOGIC TO THIS EVENT

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Block Break Event
    }

}
