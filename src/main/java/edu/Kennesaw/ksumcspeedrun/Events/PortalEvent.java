package edu.Kennesaw.ksumcspeedrun.Events;

import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.EnterObjective;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.Objective;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.Team;
import edu.Kennesaw.ksumcspeedrun.Objects.Teams.TeamManager;
import edu.Kennesaw.ksumcspeedrun.Structures.Portal;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Handles portal and teleport related events for players, checking if
 * specific objectives related to entering portals or teleporting are met.
 */
public class PortalEvent implements Listener {

    Main plugin;
    private final TeamManager tm;

    public PortalEvent(Main plugin) {
        this.plugin = plugin;
        this.tm = plugin.getSpeedrun().getTeams();
    }

    // Event triggers when player goes through standard portal
    @EventHandler
    public void onPlayerPortalMove(PlayerPortalEvent e) {

        System.out.println("Portal Event Triggered");

        Player p = e.getPlayer();

        // Get from & to worlds
        World.Environment from = e.getFrom().getWorld().getEnvironment();
        World.Environment to = e.getTo().getWorld().getEnvironment();

        // Ensure player is on a team
        Team team = tm.getTeam(p);

        if (team == null) {
            return;
        }

        // Check if matching objective exists for world to and world from
        for (Objective o : team.getIncompleteObjectives()) {

            if (o.getType().equals(Objective.ObjectiveType.ENTER)) {

                EnterObjective eo = (EnterObjective) o;

                if (eo.getTarget() instanceof Portal portal) {

                    if (from.equals(portal.getFrom()) && to.equals(portal.getTo())) {

                        // If world to & from match, set objective as complete
                        eo.setComplete(team);
                        break;

                    }

                }

            }

        }

    }

    /* This handler does the same as above, but end gateways and end portals are not handled
       the same as nether portals, thus we have to check if a player teleports. The plugin theoretically
       supports future dimensions and portals, assuming they fall under PlayerPortalEvent */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        Player p = e.getPlayer();
        World.Environment from = e.getFrom().getWorld().getEnvironment();
        World.Environment to = e.getTo().getWorld().getEnvironment();

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_GATEWAY)
                || e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {

            if (from.equals(World.Environment.THE_END) && to.equals(World.Environment.THE_END)) {

                Team team = tm.getTeam(p);

                if (team == null) {
                    return;
                }

                for (Objective o : team.getIncompleteObjectives()) {

                    if (o.getType().equals(Objective.ObjectiveType.ENTER)) {

                        EnterObjective eo = (EnterObjective) o;

                        if (eo.getTarget() instanceof Portal portal) {

                            if (portal.portalType().equals(Portal.PortalType.END_TO_END)
                                    && e.getTo().distance(e.getFrom()) >= 800) {

                                eo.setComplete(team);
                                return;

                            }

                        }

                    }

                }

            }

        } else if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)
                    || e.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {

            Team team = tm.getTeam(p);

            if (team == null) {
                return;
            }

            if (from.equals(World.Environment.THE_END) && to.equals(World.Environment.NORMAL)) {

                for (Objective o : team.getIncompleteObjectives()) {

                    if (o.getType().equals(Objective.ObjectiveType.ENTER)) {

                        EnterObjective eo = (EnterObjective) o;

                        if (eo.getTarget() instanceof Portal portal) {

                            if (portal.portalType().equals(Portal.PortalType.END_TO_WORLD)) {

                                eo.setComplete(team);
                                break;

                            }

                        }

                    }

                }

            }

        }

    }

}
