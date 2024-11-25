package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.MineObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * The MineBlock class listens for block break events and updates team objectives.
 * If a player on a team breaks a block that matches an objective, the objective's progress is updated.
 * If the objective requires a specific count, the progress is incremented and checked against the required amount.
 * If the progress meets or exceeds the required amount, the objective is marked as complete.
 * If the objective does not require a count, it is marked as complete immediately.
 */
public class MineBlock implements Listener {

    Main plugin;
    private final Speedrun speedrun;
    private final TeamManager tm;

    /* Constructor takes main plugin instance so that config and Speedrun instance can be accessed
       From Speedrun instance, the ObjectiveManager can be accessed, which has a list of all the objectives */
    public MineBlock(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        this.tm = speedrun.getTeams();
    }

    // TODO - ADD LOGIC TO THIS EVENT

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        // Only listen if the game has started
        if (speedrun.isStarted()) {

            Player p = e.getPlayer();
            Block b = e.getBlock();

            Team team = tm.getTeam(p);

            if (team == null) {
                return;
            }

            // If the player who broke a block is on a team, see if the block type matches any incomplete objectives
            for (Objective o : team.getIncompleteObjectives()) {

                if (o.getType() == Objective.ObjectiveType.MINE) {

                    MineObjective mo = (MineObjective) o;

                    if (mo.getBlockTarget().equals(b.getType())) {

                        // If the event is matched to an objective, see if the amount is specified
                        if (mo.getHasCount()) {

                            // Increment the team amount by one for that objective
                            mo.incrementTeam(team);

                            // If the team surpasses the required amount, set objective as complete
                            if (mo.getCount(team) >= mo.getAmount()) {

                                mo.setComplete(team);
                                break;

                            }

                            continue;

                        }

                        // If the amount is not specified, mark the objective as complete
                        mo.setComplete(team);
                        break;

                    }

                }

            }

        }

    }

}
