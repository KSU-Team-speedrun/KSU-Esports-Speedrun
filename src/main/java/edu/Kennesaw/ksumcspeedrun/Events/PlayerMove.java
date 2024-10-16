package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.EnterObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.ObjectiveManager;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Speedrun;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

/* This PlayerMove "Listener" is not a true listener: it is a synchronous repeating tasks that executes every two
   seconds and checks for the location of all players on the server. This is done to conserve resources, as a standard
   PlayerMoveEvent listener is executed upon the slightest player movement. */
public class PlayerMove {

    // Main, Speedrun, and ObjectiveManager (incompleteObjectives) instances from plugin instance passed in constructor
    Main plugin;
    private final Speedrun speedrun;
    private final TeamManager tm;

    // Registered when the speedrun is started
    public PlayerMove(Main plugin) {

        this.plugin = plugin;
        this.speedrun = plugin.getSpeedrun();
        this.tm = speedrun.getTeams();

        /* TODO: SOME OF THIS TASK CAN BE MADE ASYNC, WE ONLY HAVE TO BE ON THE MAIN SERVER THREAD WHEN INTERACTING WITH
           TODO (CONT.): THE WORLD */
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

            // TODO: MAYBE ACTIVATE REPEATING TASK ONLY AFTER SPEEDRUN IS STARTED INSTEAD OF CHECKING IF IT IS STARTED
            // Once an instance of PlayerMove is created, the following is executed every two seconds:
           // if (speedrun.isStarted()) {

                // TODO: MAYBE A LESS INTENSIVE WAY OF DOING THIS INSTEAD OF A NESTED LOOP?
                // Loop through each player, and for every player look through each objective
                for (Player p : Bukkit.getOnlinePlayers()) {

                    Team team = tm.getTeam(p);

                    if (team == null) {
                        continue;
                    }

                    for (Objective o : team.getIncompleteObjectives()) {

                        // If objective type is enter then we cast EnterObjective to Objective
                        if (o.getType().equals(Objective.ObjectiveType.ENTER)) {

                            EnterObjective eo = (EnterObjective) o;

                            // Check if the EnterObjective target is a structure
                            if (eo.getTarget() instanceof SRStructure target) {

                                System.out.println(target.getName());

                                // Get the player's location
                                Location playerLoc = p.getLocation();
                                final Team finalTeam = team;

                                // Check how far the player is from target structure
                                SRStructure.getNearestStructureToLocation(plugin, target, playerLoc, loc -> {

                                    if (loc != null) {

                                        /* If player is within specified radius (from config) of structure, then
                                           objective is complete */
                                        SRStructure.getStructureRadius(plugin, target, radius -> {
                                            if (playerLoc.distance(loc) <= radius) {
                                                eo.setComplete(finalTeam);
                                            }

                                        });

                                    }

                                });

                                // Check if the EnterObjective target is a Biome
                            } else if (eo.getTarget() instanceof Biome biome) {

                                if (p.getWorld().getBiome(p.getLocation()).equals(biome)) {
                                    eo.setComplete(team);
                                }

                            }

                        }

                    }

                }

            //}

        }, 40, 40);

    }

}
