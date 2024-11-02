package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.MineObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObtainObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.SoloTeam;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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

        if (speedrun.isStarted()) {

            Player p = e.getPlayer();
            Block b = e.getBlock();

            if (!speedrun.getTeamsEnabled()) {

                if (p instanceof SoloTeam soloPlayer && speedrun.getSoloPlayers().contains(p)) {

                    for (Objective o : soloPlayer.getIncompleteObjectives()) {

                        if (o.getType().equals(Objective.ObjectiveType.MINE)) {

                            MineObjective mo = (MineObjective) o;

                            if (mo.getBlockTarget().equals(b.getType())) {

                                mo.setComplete(soloPlayer);
                                break;

                            }

                        }

                    }

                }
            }

            Team team = tm.getTeam(p);

            if (team == null) {
                return;
            }

            for (Objective o : team.getIncompleteObjectives()) {

                if (o.getType() == Objective.ObjectiveType.MINE) {

                    MineObjective mo = (MineObjective) o;

                    if (mo.getBlockTarget().equals(b.getType())) {

                        mo.setComplete(team);
                        break;

                    }

                }

            }

        }

    }

}
