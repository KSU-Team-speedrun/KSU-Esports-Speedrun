package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objective.EnterObjective;
import edu.Kennesaw.ksumcspeedrun.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class PlayerMove implements Listener {

    Main plugin;
    private final Speedrun speedrun;
    private final ObjectiveManager objectives;

    public PlayerMove(Main plugin) {
        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        objectives = speedrun.getObjectives();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (speedrun.isStarted()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (Objective o : objectives.getIncompleteObjectives()) {
                        if (o.getType().equals(Objective.ObjectiveType.ENTER)) {
                            EnterObjective eo = (EnterObjective) o;
                            if (eo.getTarget() instanceof SRStructure target) {
                                Location playerLoc = p.getLocation();
                                SRStructure.getNearestStructureToLocation(plugin, target, playerLoc, loc -> {
                                    if (loc != null) {
                                        p.sendMessage(loc.toString());
                                        SRStructure.getStructureRadius(plugin, target, radius -> {
                                            if (playerLoc.distance(loc) <= radius) {
                                                eo.setComplete(p);
                                                p.sendMessage("Objective Complete: ENTER " + target.getName());
                                            }
                                        });
                                    }
                                });
                            } else if (eo.getTarget() instanceof Biome) {
                                // do something else
                            } else if (eo.getTarget() instanceof Portal) {
                                // do something else
                            }
                        }
                    }
                }
            }
        }, 40, 40);
    }
}
